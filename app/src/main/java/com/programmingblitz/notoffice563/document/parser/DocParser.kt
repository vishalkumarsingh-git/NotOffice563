package com.programmingblitz.notoffice563.document.parser

import com.programmingblitz.notoffice563.domain.model.DocxAlignment
import com.programmingblitz.notoffice563.domain.model.DocxParagraph
import com.programmingblitz.notoffice563.domain.model.DocxRun
import com.programmingblitz.notoffice563.domain.model.ParsedDocxDocument
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Parser for legacy Word formats:
 * - Word 97-2003 Binary (.doc) via OLE2 and FIB Piece Table
 * - Rich Text Format (.rtf)
 * - Plain text fallback (.txt)
 */
object DocParser {

    fun parse(inputStream: InputStream, defaultTitle: String): ParsedDocxDocument {
        val bytes = inputStream.readBytes()
        return parseBytes(bytes, defaultTitle)
    }

    fun parseBytes(bytes: ByteArray, defaultTitle: String): ParsedDocxDocument {
        // 1. Check if RTF
        if (isRtf(bytes)) {
            return parseRtf(bytes, defaultTitle)
        }

        // 2. Check if OLE2 Compound Document (.doc)
        if (Ole2Container.isOle2(bytes)) {
            return parseBinaryDoc(bytes, defaultTitle)
        }

        // 3. Fallback to Plain Text
        return parsePlainText(bytes, defaultTitle)
    }

    /**
     * Parses Microsoft Word 97-2003 (.doc) binary format from an OLE2 container.
     */
    fun parseBinaryDoc(bytes: ByteArray, defaultTitle: String): ParsedDocxDocument {
        val container = Ole2Container.parse(bytes)
        val wordDocBytes = container.streams["WordDocument"]
            ?: throw IllegalArgumentException("WordDocument stream not found in legacy .doc file.")

        if (wordDocBytes.size < 512) {
            throw IllegalArgumentException("WordDocument stream too small to contain a valid FIB.")
        }

        val buf = ByteBuffer.wrap(wordDocBytes).order(ByteOrder.LITTLE_ENDIAN)

        // Read File Information Block (FIB)
        val wIdent = buf.getShort(0).toInt() and 0xFFFF
        val flags = buf.getShort(0x000A).toInt() and 0xFFFF
        val isTable1 = (flags and (1 shl 9)) != 0
        val tableStreamName = if (isTable1) "1Table" else "0Table"
        val tableBytes = container.streams[tableStreamName]

        var extractedText: String? = null

        // Try reading piece table from Table stream if available
        if (tableBytes != null && wordDocBytes.size >= 0x01AA) {
            try {
                val fcClx = buf.getInt(0x01A2)
                val lcbClx = buf.getInt(0x01A6)

                if (fcClx >= 0 && lcbClx > 0 && fcClx + lcbClx <= tableBytes.size) {
                    extractedText = parsePieceTable(wordDocBytes, tableBytes, fcClx, lcbClx)
                }
            } catch (_: Exception) {
                // Fallback to stream scanning
            }
        }

        // If piece table extraction was not available or yielded empty, scan WordDocument stream
        if (extractedText.isNullOrBlank()) {
            extractedText = extractTextFromWordStream(wordDocBytes)
        }

        val cleaned = cleanWordText(extractedText)
        val paragraphs = buildParagraphsFromText(cleaned)

        val title = paragraphs.firstOrNull { it.text.isNotBlank() }?.text?.take(60) ?: defaultTitle
        val wordCount = cleaned.split("\\s+".toRegex()).count { it.isNotBlank() }

        return ParsedDocxDocument(
            title = title,
            creator = "Legacy Word 97-2003",
            lastModified = "",
            paragraphs = paragraphs,
            wordCount = wordCount,
            rawText = cleaned
        )
    }

    /**
     * Decodes the FIB Piece Table (Plcfpcd) to reconstruct the exact text in document order.
     */
    private fun parsePieceTable(
        wordDoc: ByteArray,
        table: ByteArray,
        fcClx: Int,
        lcbClx: Int
    ): String? {
        val tableBuf = ByteBuffer.wrap(table).order(ByteOrder.LITTLE_ENDIAN)
        var pos = fcClx
        val end = fcClx + lcbClx

        // Iterate through CLX records until we find clxtPcdt (type 2)
        while (pos < end) {
            val clxt = table[pos].toInt() and 0xFF
            pos++
            if (clxt == 1) { // clxtGrpprl
                if (pos + 2 > end) break
                val cb = tableBuf.getShort(pos).toInt() and 0xFFFF
                pos += 2 + cb
            } else if (clxt == 2) { // clxtPcdt
                if (pos + 4 > end) break
                val lcb = tableBuf.getInt(pos)
                pos += 4
                // Plcfpcd: (n + 1) * 4 bytes of CP, followed by n * 8 bytes of PCD
                // lcb = (n + 1) * 4 + n * 8 = 12n + 4 => n = (lcb - 4) / 12
                val n = (lcb - 4) / 12
                if (n <= 0) break

                val cps = IntArray(n + 1)
                for (i in 0..n) {
                    cps[i] = tableBuf.getInt(pos + i * 4)
                }
                val pcdStart = pos + (n + 1) * 4

                val sb = StringBuilder()
                val cp1252 = Charset.forName("windows-1252")

                for (i in 0 until n) {
                    val pcdOffset = pcdStart + i * 8
                    if (pcdOffset + 8 > table.size) break
                    val fc = tableBuf.getInt(pcdOffset + 2)
                    val isCompressed = (fc and 0x40000000) != 0
                    val cpLength = cps[i + 1] - cps[i]
                    if (cpLength <= 0) continue

                    if (isCompressed) {
                        val fileOffset = (fc and 0x3FFFFFFF) / 2
                        if (fileOffset + cpLength <= wordDoc.size) {
                            val chunk = String(wordDoc, fileOffset, cpLength, cp1252)
                            sb.append(chunk)
                        }
                    } else {
                        val fileOffset = fc
                        val byteLength = cpLength * 2
                        if (fileOffset + byteLength <= wordDoc.size) {
                            val chunk = String(wordDoc, fileOffset, byteLength, Charsets.UTF_16LE)
                            sb.append(chunk)
                        }
                    }
                }
                return sb.toString()
            } else {
                break
            }
        }
        return null
    }

    /**
     * Fallback extractor that scans readable UTF-16LE and ASCII text blocks directly
     * from the WordDocument stream.
     */
    private fun extractTextFromWordStream(bytes: ByteArray): String {
        val sb = StringBuilder()
        var i = 512 // Skip FIB header
        val cp1252 = Charset.forName("windows-1252")

        while (i < bytes.size - 1) {
            // Check for UTF-16LE sequence (alternating printable byte and 0x00)
            if (bytes[i] in 0x20..0x7E && bytes[i + 1] == 0.toByte()) {
                val start = i
                while (i + 1 < bytes.size && (bytes[i] in 0x09..0x7E || bytes[i].toInt() == 0x0D || bytes[i].toInt() == 0x0A) && bytes[i + 1] == 0.toByte()) {
                    i += 2
                }
                val length = i - start
                if (length >= 4) {
                    val str = String(bytes, start, length, Charsets.UTF_16LE)
                    sb.append(str).append("\n")
                }
            } else if (bytes[i] in 0x20..0x7E) {
                // Check ASCII run
                val start = i
                while (i < bytes.size && (bytes[i] in 0x20..0x7E || bytes[i] == 0x0D.toByte() || bytes[i] == 0x0A.toByte() || bytes[i] == 0x09.toByte())) {
                    i++
                }
                val length = i - start
                if (length >= 4) {
                    val str = String(bytes, start, length, cp1252)
                    sb.append(str).append("\n")
                }
            } else {
                i++
            }
        }
        return sb.toString()
    }

    /**
     * Cleans internal Word control codes:
     * - 0x0D -> \n
     * - 0x07 -> table cell delimiter (tab)
     * - 0x0C -> page break
     * - Strips field tags (0x13, 0x14, 0x15)
     */
    private fun cleanWordText(raw: String): String {
        val sb = StringBuilder()
        var insideFieldInstruction = false

        for (ch in raw) {
            when (ch.code) {
                0x13 -> insideFieldInstruction = true // Field begin
                0x14 -> insideFieldInstruction = false // Field separator -> results begin
                0x15 -> insideFieldInstruction = false // Field end
                0x0D -> sb.append('\n')
                0x07 -> sb.append('\t')
                0x0C -> sb.append("\n\n")
                0x0B -> sb.append('\n')
                else -> {
                    if (!insideFieldInstruction && (ch >= ' ' || ch == '\t' || ch == '\n')) {
                        sb.append(ch)
                    }
                }
            }
        }
        return sb.toString().replace("\r\n", "\n").replace("\r", "\n")
    }

    /**
     * Converts raw text into structured paragraphs with heading, bullet, and alignment detection.
     */
    private fun buildParagraphsFromText(text: String): List<DocxParagraph> {
        val lines = text.split('\n')
        val paragraphs = mutableListOf<DocxParagraph>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val isBullet = trimmed.startsWith("•") || trimmed.startsWith("- ") || trimmed.startsWith("* ")
            val isNumbered = trimmed.matches("^\\d+[.)]\\s+.*".toRegex())
            val contentText = if (isBullet) {
                trimmed.removePrefix("•").removePrefix("-").removePrefix("*").trim()
            } else if (isNumbered) {
                trimmed.replaceFirst("^\\d+[.)]\\s+".toRegex(), "").trim()
            } else {
                trimmed
            }

            val isShortHeading = !isBullet && !isNumbered && trimmed.length in 3..60 &&
                    (trimmed.all { it.isUpperCase() || !it.isLetter() } || !trimmed.endsWith("."))

            val runs = listOf(
                DocxRun(
                    text = contentText,
                    isBold = isShortHeading,
                    isItalic = false
                )
            )

            paragraphs.add(
                DocxParagraph(
                    text = contentText,
                    runs = runs,
                    isHeading = isShortHeading,
                    headingLevel = if (isShortHeading) 2 else 0,
                    isBullet = isBullet,
                    isNumbered = isNumbered,
                    isBold = isShortHeading,
                    alignment = DocxAlignment.LEFT
                )
            )
        }

        if (paragraphs.isEmpty()) {
            paragraphs.add(DocxParagraph(text = "Empty Document"))
        }

        return paragraphs
    }

    // =========================================================================
    // RTF (Rich Text Format) Parser
    // =========================================================================

    fun isRtf(bytes: ByteArray): Boolean {
        if (bytes.size < 5) return false
        val header = String(bytes, 0, minOf(bytes.size, 10), Charsets.US_ASCII)
        return header.startsWith("{\\rtf")
    }

    fun parseRtf(bytes: ByteArray, defaultTitle: String): ParsedDocxDocument {
        val rtf = String(bytes, Charsets.US_ASCII)
        val sb = StringBuilder()
        var i = 0
        val len = rtf.length
        var groupDepth = 0
        var skipGroup = false
        var skipDepth = -1

        var isBold = false
        var isItalic = false
        var isUnderline = false

        data class StyledRun(val text: String, val bold: Boolean, val italic: Boolean, val underline: Boolean)
        val currentParagraphRuns = mutableListOf<StyledRun>()
        val paragraphs = mutableListOf<DocxParagraph>()

        fun flushParagraph() {
            if (currentParagraphRuns.isEmpty()) return
            val fullText = currentParagraphRuns.joinToString("") { it.text }.trim()
            if (fullText.isNotEmpty()) {
                val runs = currentParagraphRuns.map {
                    DocxRun(
                        text = it.text,
                        isBold = it.bold,
                        isItalic = it.italic,
                        isUnderline = it.underline
                    )
                }
                paragraphs.add(
                    DocxParagraph(
                        text = fullText,
                        runs = runs,
                        isBold = runs.all { it.isBold },
                        isItalic = runs.all { it.isItalic }
                    )
                )
            }
            currentParagraphRuns.clear()
        }

        while (i < len) {
            val c = rtf[i]
            if (c == '{') {
                groupDepth++
                i++
                // Check destination skip: e.g. {\* \generator ...} or {\fonttbl ...} or {\colortbl ...}
                if (i < len && rtf[i] == '\\') {
                    val peekEnd = (i + 15).coerceAtMost(len)
                    val peek = rtf.substring(i, peekEnd)
                    if (peek.startsWith("\\fonttbl") || peek.startsWith("\\colortbl") ||
                        peek.startsWith("\\stylesheet") || peek.startsWith("\\info") ||
                        peek.startsWith("\\*")
                    ) {
                        skipGroup = true
                        skipDepth = groupDepth
                    }
                }
            } else if (c == '}') {
                if (skipGroup && groupDepth == skipDepth) {
                    skipGroup = false
                    skipDepth = -1
                }
                groupDepth--
                i++
            } else if (skipGroup) {
                i++
            } else if (c == '\\') {
                i++
                if (i >= len) break
                val next = rtf[i]
                if (next == '\\' || next == '{' || next == '}') {
                    currentParagraphRuns.add(StyledRun(next.toString(), isBold, isItalic, isUnderline))
                    i++
                } else if (next == '\'') { // Hex character \'xx
                    i++
                    if (i + 2 <= len) {
                        val hex = rtf.substring(i, i + 2)
                        val code = hex.toIntOrNull(16) ?: 32
                        currentParagraphRuns.add(StyledRun(code.toChar().toString(), isBold, isItalic, isUnderline))
                        i += 2
                    }
                } else if (next.isLetter()) {
                    // Control word
                    val wordStart = i
                    while (i < len && rtf[i].isLetter()) {
                        i++
                    }
                    val word = rtf.substring(wordStart, i)
                    // Optional numeric parameter
                    var numStr = ""
                    if (i < len && (rtf[i] == '-' || rtf[i].isDigit())) {
                        val numStart = i
                        if (rtf[i] == '-') i++
                        while (i < len && rtf[i].isDigit()) {
                            i++
                        }
                        numStr = rtf.substring(numStart, i)
                    }
                    // Optional delimiter space
                    if (i < len && rtf[i] == ' ') {
                        i++
                    }

                    when (word) {
                        "par", "line" -> flushParagraph()
                        "tab" -> currentParagraphRuns.add(StyledRun("\t", isBold, isItalic, isUnderline))
                        "b" -> isBold = numStr != "0"
                        "i" -> isItalic = numStr != "0"
                        "ul" -> isUnderline = numStr != "0"
                        "ulnone" -> isUnderline = false
                        "bullet" -> currentParagraphRuns.add(StyledRun("• ", isBold, isItalic, isUnderline))
                        "u" -> {
                            val code = numStr.toIntOrNull()
                            if (code != null) {
                                val char = if (code < 0) (code + 65536).toChar() else code.toChar()
                                currentParagraphRuns.add(StyledRun(char.toString(), isBold, isItalic, isUnderline))
                            }
                        }
                    }
                } else {
                    i++
                }
            } else if (c == '\r' || c == '\n') {
                i++
            } else {
                currentParagraphRuns.add(StyledRun(c.toString(), isBold, isItalic, isUnderline))
                i++
            }
        }

        flushParagraph()

        val fullRaw = paragraphs.joinToString("\n\n") { it.text }
        val title = paragraphs.firstOrNull()?.text?.take(60) ?: defaultTitle

        return ParsedDocxDocument(
            title = title,
            creator = "RTF Document",
            lastModified = "",
            paragraphs = if (paragraphs.isNotEmpty()) paragraphs else listOf(DocxParagraph(text = "Empty Document")),
            wordCount = fullRaw.split("\\s+".toRegex()).count { it.isNotBlank() },
            rawText = fullRaw
        )
    }

    // =========================================================================
    // Plain Text Parser
    // =========================================================================

    fun parsePlainText(bytes: ByteArray, defaultTitle: String): ParsedDocxDocument {
        val text = String(bytes, Charsets.UTF_8).replace("\r\n", "\n").replace("\r", "\n")
        val paragraphs = buildParagraphsFromText(text)
        val title = paragraphs.firstOrNull()?.text?.take(60) ?: defaultTitle
        val wordCount = text.split("\\s+".toRegex()).count { it.isNotBlank() }

        return ParsedDocxDocument(
            title = title,
            creator = "Plain Text Document",
            lastModified = "",
            paragraphs = paragraphs,
            wordCount = wordCount,
            rawText = text
        )
    }
}
