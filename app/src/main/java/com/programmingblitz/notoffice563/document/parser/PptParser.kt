package com.programmingblitz.notoffice563.document.parser

import com.programmingblitz.notoffice563.domain.model.ParsedPresentation
import com.programmingblitz.notoffice563.domain.model.PresentationSlide
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Parser for legacy PowerPoint formats:
 * - PowerPoint 97-2003 Binary (.ppt) via OLE2 container and PowerPoint Document stream records
 */
object PptParser {

    fun parse(inputStream: InputStream): ParsedPresentation {
        val bytes = inputStream.readBytes()
        return parseBytes(bytes)
    }

    fun parseBytes(bytes: ByteArray): ParsedPresentation {
        if (!Ole2Container.isOle2(bytes)) {
            throw IllegalArgumentException("Not a valid legacy PowerPoint file (OLE2 signature missing).")
        }

        val container = Ole2Container.parse(bytes)
        val pptBytes = container.streams["PowerPoint Document"]
            ?: throw IllegalArgumentException("PowerPoint Document stream not found in legacy .ppt file.")

        return parsePptStream(pptBytes)
    }

    /**
     * Parses the PowerPoint Document record stream.
     * Traverses record structures to extract slides, slide titles, and bullet paragraphs.
     */
    fun parsePptStream(stream: ByteArray): ParsedPresentation {
        val buf = ByteBuffer.wrap(stream).order(ByteOrder.LITTLE_ENDIAN)
        val len = stream.size
        var pos = 0

        // Extracted slides
        data class SlideData(
            var title: String = "",
            val bullets: MutableList<String> = mutableListOf(),
            var notes: String = ""
        )

        val slides = mutableListOf<SlideData>()
        var currentSlide: SlideData? = null

        val cp1252 = Charset.forName("windows-1252")

        fun ensureSlide(): SlideData {
            if (currentSlide == null) {
                val s = SlideData()
                slides.add(s)
                currentSlide = s
            }
            return currentSlide!!
        }

        fun addTextToSlide(text: String) {
            val cleaned = text.replace("\r\n", "\n").replace('\r', '\n').trim()
            if (cleaned.isEmpty()) return

            val lines = cleaned.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) return

            val target = ensureSlide()
            for (line in lines) {
                if (target.title.isEmpty() && line.length in 2..80 && !line.startsWith("•") && !line.startsWith("-")) {
                    target.title = line
                } else {
                    val bullet = line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
                    if (bullet.isNotEmpty()) {
                        target.bullets.add(bullet)
                    }
                }
            }
        }

        // Sequential record scanner
        while (pos + 8 <= len) {
            val verInst = buf.getShort(pos).toInt() and 0xFFFF
            val recType = buf.getShort(pos + 2).toInt() and 0xFFFF
            val recLen = buf.getInt(pos + 4)
            val dataPos = pos + 8
            pos += 8 + recLen
            if (recLen < 0 || dataPos + recLen > len) {
                // Advance safely if recLen is malformed
                pos = dataPos
                continue
            }

            when (recType) {
                // 1006 = RT_Slide, 4000 = RT_SlidePersistAtom
                1006, 4000 -> {
                    // New slide boundary detected
                    val s = SlideData()
                    slides.add(s)
                    currentSlide = s
                }

                // 4009 = RT_TextCharsAtom (UTF-16LE text string)
                4009 -> {
                    if (recLen >= 2) {
                        val str = String(stream, dataPos, recLen, Charsets.UTF_16LE)
                        addTextToSlide(str)
                    }
                }

                // 4010 = RT_TextBytesAtom (Latin-1 / ASCII text string)
                4010 -> {
                    if (recLen >= 1) {
                        val str = String(stream, dataPos, recLen, cp1252)
                        addTextToSlide(str)
                    }
                }

                // 4026 = RT_CString (UTF-16LE text)
                4026 -> {
                    if (recLen >= 2) {
                        val str = String(stream, dataPos, recLen, Charsets.UTF_16LE)
                        addTextToSlide(str)
                    }
                }

                // 1008 = RT_Notes
                1008 -> {
                    // Notes container
                }
            }
        }

        // Fallback: If no structured slides were detected, scan printable text chunks
        if (slides.isEmpty() || slides.all { it.title.isEmpty() && it.bullets.isEmpty() }) {
            slides.clear()
            val extractedTexts = extractReadableTexts(stream)
            var currentChunk = SlideData()
            for (txt in extractedTexts) {
                if (currentChunk.title.isEmpty()) {
                    currentChunk.title = txt
                } else {
                    currentChunk.bullets.add(txt)
                    if (currentChunk.bullets.size >= 5) {
                        slides.add(currentChunk)
                        currentChunk = SlideData()
                    }
                }
            }
            if (currentChunk.title.isNotEmpty() || currentChunk.bullets.isNotEmpty()) {
                slides.add(currentChunk)
            }
        }

        // Ensure at least one slide exists
        if (slides.isEmpty()) {
            slides.add(
                SlideData(
                    title = "Presentation",
                    bullets = mutableListOf("Slide content")
                )
            )
        }

        val presentationSlides = slides.mapIndexed { idx, s ->
            PresentationSlide(
                slideNumber = idx + 1,
                title = if (s.title.isNotBlank()) s.title else "Slide ${idx + 1}",
                bodyBullets = if (s.bullets.isNotEmpty()) s.bullets else listOf("Content for slide ${idx + 1}"),
                notes = s.notes
            )
        }

        return ParsedPresentation(slides = presentationSlides)
    }

    private fun extractReadableTexts(stream: ByteArray): List<String> {
        val results = mutableListOf<String>()
        var i = 0
        while (i < stream.size - 1) {
            if (stream[i] in 0x20..0x7E && stream[i + 1] == 0.toByte()) {
                val start = i
                while (i + 1 < stream.size && (stream[i] in 0x20..0x7E || stream[i] == 0x0D.toByte() || stream[i] == 0x0A.toByte()) && stream[i + 1] == 0.toByte()) {
                    i += 2
                }
                val length = i - start
                if (length >= 6) {
                    val str = String(stream, start, length, Charsets.UTF_16LE).trim()
                    if (str.length >= 3 && !str.startsWith("<") && !str.startsWith("{")) {
                        results.add(str)
                    }
                }
            } else {
                i++
            }
        }
        return results
    }
}
