package com.programmingblitz.notoffice563.document.parser

import android.content.Context
import android.net.Uri
import android.util.Xml
import com.programmingblitz.notoffice563.domain.model.DocxParagraph
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
     * Extracts document properties, clean paragraphs, headings, bullet points, and basic styling.
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
        var currentPText = StringBuilder()
        var isHeading = false
        var headingLevel = 0
        var isBullet = false
        var isBold = false
        var isItalic = false

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "p" -> {
                            inParagraph = true
                            currentPText = StringBuilder()
                            isHeading = false
                            headingLevel = 0
                            isBullet = false
                            isBold = false
                            isItalic = false
                        }
                        "pStyle" -> {
                            val styleVal = parser.getAttributeValue(null, "val") ?: ""
                            if (styleVal.startsWith("Heading", ignoreCase = true) || styleVal.startsWith("Title", ignoreCase = true)) {
                                isHeading = true
                                headingLevel = styleVal.filter { it.isDigit() }.toIntOrNull() ?: 1
                            }
                        }
                        "numPr" -> {
                            isBullet = true
                        }
                        "b" -> {
                            val valAttr = parser.getAttributeValue(null, "val")
                            isBold = valAttr == null || valAttr == "1" || valAttr.equals("true", ignoreCase = true)
                        }
                        "i" -> {
                            val valAttr = parser.getAttributeValue(null, "val")
                            isItalic = valAttr == null || valAttr == "1" || valAttr.equals("true", ignoreCase = true)
                        }
                        "t" -> {
                            val text = parser.nextText()
                            currentPText.append(text)
                            fullTextBuilder.append(text)
                        }
                        "tab" -> {
                            currentPText.append("    ")
                            fullTextBuilder.append("    ")
                        }
                        "br", "cr" -> {
                            currentPText.append("\n")
                            fullTextBuilder.append("\n")
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "p") {
                        val text = currentPText.toString().trim()
                        if (text.isNotEmpty()) {
                            paragraphs.add(
                                DocxParagraph(
                                    text = text,
                                    isHeading = isHeading,
                                    headingLevel = headingLevel,
                                    isBullet = isBullet,
                                    isBold = isBold,
                                    isItalic = isItalic
                                )
                            )
                        }
                        fullTextBuilder.append("\n\n")
                        inParagraph = false
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
     * Generates a basic valid .docx archive containing the modified paragraphs and title.
     * Allows users to edit text and save a newly compiled .docx file cleanly.
     */
    fun createBasicDocx(title: String, plainContent: String): ByteArray {
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
    <dc:creator>NotOffice563 User</dc:creator>
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
            // Title paragraph
            if (title.isNotBlank()) {
                docBuilder.append("""
        <w:p>
            <w:pPr>
                <w:pStyle w:val="Heading1"/>
            </w:pPr>
            <w:r>
                <w:rPr><w:b/><w:sz w:val="36"/></w:rPr>
                <w:t>${escapeXml(title)}</w:t>
            </w:r>
        </w:p>
""")
            }

            // Body paragraphs
            val paragraphs = plainContent.split("\n\n", "\n")
            for (p in paragraphs) {
                if (p.isNotBlank()) {
                    docBuilder.append("""
        <w:p>
            <w:r>
                <w:t>${escapeXml(p)}</w:t>
            </w:r>
        </w:p>
""")
                }
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

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
