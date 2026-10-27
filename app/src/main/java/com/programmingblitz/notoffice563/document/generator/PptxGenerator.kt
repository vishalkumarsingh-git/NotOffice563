package com.programmingblitz.notoffice563.document.generator

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PptxGenerator {

    val templates = listOf(
        DocumentStarterTemplate(
            id = "pptx_blank",
            title = "Blank Presentation",
            description = "Clean presentation canvas ready for your slides",
            format = DocumentFormatCategory.PPTX,
            defaultFileName = "New Presentation"
        ),
        DocumentStarterTemplate(
            id = "pptx_pitch",
            title = "Pitch Deck Template",
            description = "Slide layout for problem, vision, market, and roadmap",
            format = DocumentFormatCategory.PPTX,
            defaultFileName = "Pitch Deck"
        ),
        DocumentStarterTemplate(
            id = "pptx_status",
            title = "Status Report Template",
            description = "Slide layout for summary, deliverables, and next priorities",
            format = DocumentFormatCategory.PPTX,
            defaultFileName = "Status Report"
        )
    )

    data class SlideData(
        val title: String,
        val bullets: List<String>
    )

    fun generate(templateId: String, presentationTitle: String = ""): ByteArray {
        val slides: List<SlideData> = when (templateId) {
            "pptx_pitch" -> listOf(
                SlideData(
                    title = presentationTitle.ifBlank { "Project Overview" },
                    bullets = emptyList()
                ),
                SlideData(
                    title = "Market Opportunity",
                    bullets = emptyList()
                )
            )
            "pptx_status" -> listOf(
                SlideData(
                    title = presentationTitle.ifBlank { "Sprint Status Update" },
                    bullets = emptyList()
                )
            )
            else -> listOf(
                SlideData(
                    title = presentationTitle,
                    bullets = emptyList()
                )
            )
        }

        return buildPptxZip(slides)
    }

    fun buildPptxFromPresentation(parsedPresentation: com.programmingblitz.notoffice563.domain.model.ParsedPresentation): ByteArray {
        val slideDataList = parsedPresentation.slides.map { s ->
            SlideData(title = s.title, bullets = s.bodyBullets)
        }
        return buildPptxZip(if (slideDataList.isNotEmpty()) slideDataList else listOf(SlideData("Untitled Slide", emptyList())))
    }

    private fun buildPptxZip(slides: List<SlideData>): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            // 1. [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            val contentTypes = StringBuilder()
            contentTypes.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
    <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
""")
            for (i in 1..slides.size) {
                contentTypes.append("    <Override PartName=\"/ppt/slides/slide$i.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slide+xml\"/>\n")
            }
            contentTypes.append("</Types>")
            zos.write(contentTypes.toString().toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 2. _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            val rootRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
</Relationships>"""
            zos.write(rootRels.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 3. ppt/_rels/presentation.xml.rels
            zos.putNextEntry(ZipEntry("ppt/_rels/presentation.xml.rels"))
            val presRels = StringBuilder()
            presRels.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
""")
            for (i in 1..slides.size) {
                presRels.append("    <Relationship Id=\"rId$i\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide\" Target=\"slides/slide$i.xml\"/>\n")
            }
            presRels.append("</Relationships>")
            zos.write(presRels.toString().toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 4. ppt/presentation.xml
            zos.putNextEntry(ZipEntry("ppt/presentation.xml"))
            val presBuilder = StringBuilder()
            presBuilder.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <p:sldIdLst>
""")
            for (i in 1..slides.size) {
                val sldId = 255 + i
                presBuilder.append("        <p:sldId id=\"$sldId\" r:id=\"rId$i\"/>\n")
            }
            presBuilder.append("""    </p:sldIdLst>
    <p:sldSz cx="9144000" cy="5143500"/>
</p:presentation>""")
            zos.write(presBuilder.toString().toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 5. docProps/core.xml
            zos.putNextEntry(ZipEntry("docProps/core.xml"))
            val coreProps = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
    xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:title>${escapeXml(slides.firstOrNull()?.title ?: "Presentation")}</dc:title>
    <dc:creator>NotOffice563</dc:creator>
</cp:coreProperties>"""
            zos.write(coreProps.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 6. ppt/slides/slide{i}.xml
            for ((index, slide) in slides.withIndex()) {
                val slideNum = index + 1
                zos.putNextEntry(ZipEntry("ppt/slides/slide$slideNum.xml"))
                val slideXml = buildSlideXml(slide)
                zos.write(slideXml.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()
            }
        }
        return bos.toByteArray()
    }

    private fun buildSlideXml(slide: SlideData): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
    <p:cSld>
        <p:spTree>
            <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
            <p:grpSpPr/>
            
            <!-- Slide Title Shape -->
            <p:sp>
                <p:nvSpPr><p:cNvPr id="2" name="Title"/><p:cNvSpPr><a:spLocks noGrp="1"/></p:cNvSpPr><p:nvPr/></p:nvSpPr>
                <p:spPr>
                    <a:xfrm><a:off x="685800" y="457200"/><a:ext cx="7772400" cy="914400"/></a:xfrm>
                </p:spPr>
                <p:txBody>
                    <a:bodyPr/>
                    <a:p>
                        <a:r>
                            <a:rPr lang="en-US" sz="3600" b="1"><a:solidFill><a:srgbClr val="0061A4"/></a:solidFill></a:rPr>
                            <a:t>${escapeXml(slide.title)}</a:t>
                        </a:r>
                    </a:p>
                </p:txBody>
            </p:sp>

            <!-- Slide Content Shape -->
            <p:sp>
                <p:nvSpPr><p:cNvPr id="3" name="Content"/><p:cNvSpPr><a:spLocks noGrp="1"/></p:cNvSpPr><p:nvPr/></p:nvSpPr>
                <p:spPr>
                    <a:xfrm><a:off x="685800" y="1600200"/><a:ext cx="7772400" cy="3000000"/></a:xfrm>
                </p:spPr>
                <p:txBody>
                    <a:bodyPr/>
""")
        for (bullet in slide.bullets) {
            sb.append("""
                    <a:p>
                        <a:pPr lvl="0"/>
                        <a:r>
                            <a:rPr lang="en-US" sz="2000"/>
                            <a:t>${escapeXml(bullet)}</a:t>
                        </a:r>
                    </a:p>
""")
        }
        sb.append("""
                </p:txBody>
            </p:sp>
        </p:spTree>
    </p:cSld>
</p:sld>""")
        return sb.toString()
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
