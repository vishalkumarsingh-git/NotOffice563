package com.programmingblitz.notoffice563.document.parser

import android.content.Context
import android.net.Uri
import android.util.Xml
import com.programmingblitz.notoffice563.domain.model.DocxAlignment
import com.programmingblitz.notoffice563.domain.model.DocxParagraph
import com.programmingblitz.notoffice563.domain.model.DocxRun
import com.programmingblitz.notoffice563.domain.model.ParsedDocxDocument
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object DocxParser {

    /**
     * Parses a .docx document from a stream.
     * Extracts document properties, clean paragraphs, headings, bullet points, runs, and rich styling.
     */
    fun parse(inputStream: InputStream, defaultTitle: String): ParsedDocxDocument {
        var documentXmlBytes: ByteArray? = null
        var coreXmlBytes: ByteArray? = null

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when (entry.name) {
                    "word/document.xml" -> {
                        documentXmlBytes = zip.readBytes()
                    }
                    "docProps/core.xml" -> {
                        coreXmlBytes = zip.readBytes()
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        if (documentXmlBytes == null) {
            throw IllegalArgumentException("Invalid DOCX archive: word/document.xml not found.")
        }

        // Parse core metadata if available
        var title = defaultTitle
        var creator = ""
        var modified = ""
        if (coreXmlBytes != null) {
            runCatching {
                val parser = Xml.newPullParser()
                parser.setInput(ByteArrayInputStream(coreXmlBytes), "UTF-8")
                var eventType = parser.eventType
                var currentTag = ""
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> currentTag = parser.name
                        XmlPullParser.TEXT -> {
                            val text = parser.text.trim()
                            if (text.isNotEmpty()) {
                                when (currentTag) {
                                    "title" -> if (title == defaultTitle) title = text
                                    "creator" -> creator = text
                                    "modified" -> modified = text
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
        }

        // Parse word/document.xml
        val paragraphs = mutableListOf<DocxParagraph>()
        val fullTextBuilder = StringBuilder()

        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(documentXmlBytes), "UTF-8")

        var inParagraph = false
        var currentPRuns = mutableListOf<DocxRun>()
        var isHeading = false
        var headingLevel = 0
        var isBullet = false
        var isNumbered = false
        var paragraphAlignment = com.programmingblitz.notoffice563.domain.model.DocxAlignment.LEFT

        // Run level state
        var inRun = false
        var runBold = false
        var runItalic = false
        var runUnderline = false
        var runFontSize: Float? = null
        var currentRunText = StringBuilder()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "p" -> {
                            inParagraph = true
                            currentPRuns = mutableListOf()
                            isHeading = false
                            headingLevel = 0
                            isBullet = false
                            isNumbered = false
                            paragraphAlignment = com.programmingblitz.notoffice563.domain.model.DocxAlignment.LEFT
                        }
                        "pStyle" -> {
                            val styleVal = parser.getAttributeValue(null, "val") ?: ""
                            if (styleVal.startsWith("Heading", ignoreCase = true) || styleVal.startsWith("Title", ignoreCase = true)) {
                                isHeading = true
                                headingLevel = styleVal.filter { it.isDigit() }.toIntOrNull() ?: 1
                            } else if (styleVal.contains("ListBullet", ignoreCase = true)) {
                                isBullet = true
                            } else if (styleVal.contains("ListNumber", ignoreCase = true)) {
                                isNumbered = true
                            }
                        }
                        "jc" -> {
                            val jcVal = parser.getAttributeValue(null, "val") ?: ""
                            paragraphAlignment = when (jcVal.lowercase()) {
                                "center" -> com.programmingblitz.notoffice563.domain.model.DocxAlignment.CENTER
                                "right" -> com.programmingblitz.notoffice563.domain.model.DocxAlignment.RIGHT
                                "both", "justify" -> com.programmingblitz.notoffice563.domain.model.DocxAlignment.JUSTIFY
                                else -> com.programmingblitz.notoffice563.domain.model.DocxAlignment.LEFT
                            }
                        }
                        "numPr" -> {
                            isBullet = true
                        }
                        "r" -> {
                            inRun = true
                            runBold = false
                            runItalic = false
                            runUnderline = false
                            runFontSize = null
                            currentRunText = StringBuilder()
                        }
                        "b" -> {
                            val valAttr = parser.getAttributeValue(null, "val")
                            val active = valAttr == null || valAttr == "1" || valAttr.equals("true", ignoreCase = true)
                            if (inRun) runBold = active
                        }
                        "i" -> {
                            val valAttr = parser.getAttributeValue(null, "val")
                            val active = valAttr == null || valAttr == "1" || valAttr.equals("true", ignoreCase = true)
                            if (inRun) runItalic = active
                        }
                        "u" -> {
                            val valAttr = parser.getAttributeValue(null, "val")
                            val active = valAttr != "none"
                            if (inRun) runUnderline = active
                        }
                        "sz" -> {
                            val valAttr = parser.getAttributeValue(null, "val")
                            val halfPoints = valAttr?.toFloatOrNull()
                            if (halfPoints != null && inRun) {
                                runFontSize = halfPoints / 2f
                            }
                        }
                        "t" -> {
                            val text = parser.nextText()
                            if (inRun) {
                                currentRunText.append(text)
                            } else {
                                currentPRuns.add(DocxRun(text = text))
                            }
                            fullTextBuilder.append(text)
                        }
                        "tab" -> {
                            if (inRun) currentRunText.append("    ") else fullTextBuilder.append("    ")
                        }
                        "br", "cr" -> {
                            if (inRun) currentRunText.append("\n") else fullTextBuilder.append("\n")
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "r" -> {
                            if (inRun) {
                                val rText = currentRunText.toString()
                                if (rText.isNotEmpty()) {
                                    currentPRuns.add(
                                        DocxRun(
                                            text = rText,
                                            isBold = runBold,
                                            isItalic = runItalic,
                                            isUnderline = runUnderline,
                                            fontSizeSp = runFontSize
                                        )
                                    )
                                }
                                inRun = false
                            }
                        }
                        "p" -> {
                            val pText = currentPRuns.joinToString("") { it.text }.trim()
                            if (pText.isNotEmpty() || currentPRuns.isNotEmpty()) {
                                paragraphs.add(
                                    DocxParagraph(
                                        text = pText,
                                        runs = currentPRuns,
                                        isHeading = isHeading,
                                        headingLevel = headingLevel,
                                        isBullet = isBullet,
                                        isNumbered = isNumbered,
                                        isBold = currentPRuns.any { it.isBold },
                                        isItalic = currentPRuns.any { it.isItalic },
                                        isUnderline = currentPRuns.any { it.isUnderline },
                                        alignment = paragraphAlignment
                                    )
                                )
                            }
                            fullTextBuilder.append("\n\n")
                            inParagraph = false
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        val rawText = fullTextBuilder.toString().trim()
        val words = if (rawText.isEmpty()) 0 else rawText.split("\\s+".toRegex()).size

        return ParsedDocxDocument(
            title = title,
            creator = creator,
            lastModified = modified,
            paragraphs = paragraphs,
            wordCount = words,
            rawText = rawText
        )
    }

    /**
     * Generates a valid OOXML .docx archive containing formatted paragraphs, runs, alignments, and titles.
     */
    fun createRichDocx(title: String, paragraphs: List<DocxParagraph>): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            // 1. [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
    <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
</Types>"""
            zos.write(contentTypes.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 2. _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            val rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
</Relationships>"""
            zos.write(rels.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 3. docProps/core.xml
            zos.putNextEntry(ZipEntry("docProps/core.xml"))
            val coreProps = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
    xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:title>${escapeXml(title)}</dc:title>
    <dc:creator>NotOffice563</dc:creator>
</cp:coreProperties>"""
            zos.write(coreProps.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 4. word/document.xml
            zos.putNextEntry(ZipEntry("word/document.xml"))
            val docBuilder = StringBuilder()
            docBuilder.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
    <w:body>
""")
            // Title paragraph if title present
            if (title.isNotBlank()) {
                docBuilder.append("""
        <w:p>
            <w:pPr>
                <w:pStyle w:val="Heading1"/>
                <w:jc w:val="left"/>
            </w:pPr>
            <w:r>
                <w:rPr><w:b/><w:sz w:val="36"/></w:rPr>
                <w:t xml:space="preserve">${escapeXml(title)}</w:t>
            </w:r>
        </w:p>
""")
            }

            // Paragraphs
            for (p in paragraphs) {
                val jcVal = when (p.alignment) {
                    com.programmingblitz.notoffice563.domain.model.DocxAlignment.CENTER -> "center"
                    com.programmingblitz.notoffice563.domain.model.DocxAlignment.RIGHT -> "right"
                    com.programmingblitz.notoffice563.domain.model.DocxAlignment.JUSTIFY -> "both"
                    else -> "left"
                }

                docBuilder.append("        <w:p>\n")
                docBuilder.append("            <w:pPr>\n")
                if (p.isHeading) {
                    docBuilder.append("                <w:pStyle w:val=\"Heading1\"/>\n")
                }
                docBuilder.append("                <w:jc w:val=\"$jcVal\"/>\n")
                if (p.isBullet) {
                    docBuilder.append("                <w:ind w:left=\"360\"/>\n")
                }
                docBuilder.append("            </w:pPr>\n")

                if (p.runs.isNotEmpty()) {
                    for (run in p.runs) {
                        docBuilder.append("            <w:r>\n")
                        docBuilder.append("                <w:rPr>\n")
                        if (run.isBold) docBuilder.append("                    <w:b/>\n")
                        if (run.isItalic) docBuilder.append("                    <w:i/>\n")
                        if (run.isUnderline) docBuilder.append("                    <w:u w:val=\"single\"/>\n")
                        if (run.fontSizeSp != null) {
                            val szHalf = (run.fontSizeSp * 2f).toInt()
                            docBuilder.append("                    <w:sz w:val=\"$szHalf\"/>\n")
                        }
                        docBuilder.append("                </w:rPr>\n")
                        docBuilder.append("                <w:t xml:space=\"preserve\">${escapeXml(run.text)}</w:t>\n")
                        docBuilder.append("            </w:r>\n")
                    }
                } else if (p.fullText.isNotEmpty()) {
                    docBuilder.append("            <w:r>\n")
                    docBuilder.append("                <w:rPr>\n")
                    if (p.isBold) docBuilder.append("                    <w:b/>\n")
                    if (p.isItalic) docBuilder.append("                    <w:i/>\n")
                    if (p.isUnderline) docBuilder.append("                    <w:u w:val=\"single\"/>\n")
                    docBuilder.append("                </w:rPr>\n")
                    docBuilder.append("                <w:t xml:space=\"preserve\">${escapeXml(p.fullText)}</w:t>\n")
                    docBuilder.append("            </w:r>\n")
                }

                docBuilder.append("        </w:p>\n")
            }

            docBuilder.append("""
        <w:sectPr/>
    </w:body>
</w:document>""")
            zos.write(docBuilder.toString().toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }
        return bos.toByteArray()
    }

    /**
     * Generates a basic valid .docx archive containing the modified paragraphs and title.
     */
    fun createBasicDocx(title: String, plainContent: String): ByteArray {
        val paragraphs = plainContent.split("\n\n", "\n")
            .filter { it.isNotBlank() }
            .map { text ->
                val isBullet = text.startsWith("• ") || text.startsWith("- ")
                val cleanText = if (isBullet) text.substring(2) else text
                DocxParagraph(
                    text = cleanText,
                    runs = listOf(DocxRun(text = cleanText)),
                    isBullet = isBullet
                )
            }
        return createRichDocx(title, paragraphs)
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
