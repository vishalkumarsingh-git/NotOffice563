package com.programmingblitz.notoffice563.document.generator

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DocxGenerator {

    val templates = listOf(
        DocumentStarterTemplate(
            id = "docx_blank",
            title = "Blank Document",
            description = "Start with a clean, empty page",
            format = DocumentFormatCategory.DOCX,
            defaultFileName = "New Document"
        ),
        DocumentStarterTemplate(
            id = "docx_meeting",
            title = "Meeting Notes Template",
            description = "Structure agenda, attendees, and action items",
            format = DocumentFormatCategory.DOCX,
            defaultFileName = "Meeting Notes"
        ),
        DocumentStarterTemplate(
            id = "docx_proposal",
            title = "Project Proposal Template",
            description = "Outline objectives, scope, and timeline",
            format = DocumentFormatCategory.DOCX,
            defaultFileName = "Project Proposal"
        )
    )

    fun generate(templateId: String, title: String, extraContent: String = ""): ByteArray {
        val dateStr = SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date())

        val (docTitle, paragraphs) = when (templateId) {
            "docx_meeting" -> {
                val t = title.ifBlank { "Meeting Notes" }
                val p = listOf(
                    "Date: $dateStr",
                    "1. Agenda Items",
                    "• ",
                    "2. Action Items",
                    "• "
                )
                Pair(t, p)
            }
            "docx_proposal" -> {
                val t = title.ifBlank { "Project Proposal" }
                val p = listOf(
                    "Date: $dateStr",
                    "1. Project Objectives",
                    "2. Scope of Work",
                    "3. Timeline & Milestones"
                )
                Pair(t, p)
            }
            else -> {
                // Blank Document: Zero prewritten text/paragraphs injected!
                val t = title
                val p = if (extraContent.isNotBlank()) {
                    extraContent.split("\n\n", "\n").filter { it.isNotBlank() }
                } else {
                    emptyList()
                }
                Pair(t, p)
            }
        }

        return buildDocxZip(docTitle, paragraphs)
    }

    private fun buildDocxZip(title: String, paragraphs: List<String>): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            // 1. [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
    <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
    <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
    <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>"""
            zos.write(contentTypes.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 2. _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            val rootRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
    <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>"""
            zos.write(rootRels.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 3. word/_rels/document.xml.rels
            zos.putNextEntry(ZipEntry("word/_rels/document.xml.rels"))
            val docRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
            zos.write(docRels.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 4. word/styles.xml
            zos.putNextEntry(ZipEntry("word/styles.xml"))
            val stylesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
    <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
        <w:name w:val="Normal"/>
        <w:rPr>
            <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
            <w:sz w:val="24"/>
        </w:rPr>
    </w:style>
    <w:style w:type="paragraph" w:styleId="Heading1">
        <w:name w:val="heading 1"/>
        <w:rPr>
            <w:b/>
            <w:sz w:val="36"/>
            <w:color w:val="0061A4"/>
        </w:rPr>
    </w:style>
</w:styles>"""
            zos.write(stylesXml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 5. docProps/core.xml
            zos.putNextEntry(ZipEntry("docProps/core.xml"))
            val coreProps = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
    xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:title>${escapeXml(title)}</dc:title>
    <dc:creator>NotOffice563</dc:creator>
    <cp:lastModifiedBy>NotOffice563</cp:lastModifiedBy>
</cp:coreProperties>"""
            zos.write(coreProps.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 6. docProps/app.xml
            zos.putNextEntry(ZipEntry("docProps/app.xml"))
            val appProps = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">
    <Application>NotOffice563</Application>
</Properties>"""
            zos.write(appProps.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 7. word/document.xml
            zos.putNextEntry(ZipEntry("word/document.xml"))
            val docBuilder = StringBuilder()
            docBuilder.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
    <w:body>
""")
            // Title
            if (title.isNotBlank()) {
                docBuilder.append("""
        <w:p>
            <w:pPr>
                <w:pStyle w:val="Heading1"/>
                <w:spacing w:after="240"/>
            </w:pPr>
            <w:r>
                <w:rPr><w:b/><w:sz w:val="40"/></w:rPr>
                <w:t>${escapeXml(title)}</w:t>
            </w:r>
        </w:p>
""")
            }

            // Paragraphs
            for (p in paragraphs) {
                if (p.isNotBlank()) {
                    val isHeading = p.matches("^[0-9]+\\..*".toRegex())
                    val isBullet = p.startsWith("•") || p.startsWith("-")
                    val cleanText = if (isBullet) p.trimStart('•', '-', ' ') else p

                    docBuilder.append("""
        <w:p>
            <w:pPr>
                ${if (isHeading) "<w:pStyle w:val=\"Heading1\"/><w:spacing w:before=\"200\" w:after=\"100\"/>" else "<w:spacing w:after=\"160\"/>"}
                ${if (isBullet) "<w:ind w:left=\"360\"/>" else ""}
            </w:pPr>
            <w:r>
                <w:rPr>
                    ${if (isHeading) "<w:b/><w:sz w:val=\"30\"/><w:color w:val=\"0061A4\"/>" else "<w:sz w:val=\"24\"/>"}
                </w:rPr>
                <w:t>${if (isBullet) "• " else ""}${escapeXml(cleanText)}</w:t>
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
