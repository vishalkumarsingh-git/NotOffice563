package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.document.parser.DocxParser
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SampleDocumentCreator {

    private fun getSafeUri(context: Context, file: File): Uri {
        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            Uri.fromFile(file)
        }
    }

    fun createSamplePdf(context: Context): Uri {
        val file = File(context.filesDir, "Sample_Research_Notes.pdf")
        if (!file.exists()) {
            var written = false
            try {
                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                val paint = Paint().apply {
                    isAntiAlias = true
                }

                // Header background
                paint.color = 0xFF2563EB.toInt()
                canvas.drawRect(0f, 0f, 595f, 100f, paint)

                paint.color = Color.WHITE
                paint.textSize = 22f
                paint.isFakeBoldText = true
                canvas.drawText("NotOffice563 • Sample PDF Document", 40f, 60f, paint)

                paint.color = 0xFF0F172A.toInt()
                paint.textSize = 14f
                paint.isFakeBoldText = false

                var y = 140f
                canvas.drawText("Welcome to NotOffice563 Local Workspace", 40f, y, paint)
                y += 30f
                canvas.drawText("This PDF document demonstrates the high-performance local rendering pipeline.", 40f, y, paint)
                y += 24f
                canvas.drawText("Key Features in NotOffice563:", 40f, y, paint)
                y += 24f
                canvas.drawText("• Native PDF rendering via Android PdfRenderer", 60f, y, paint)
                y += 20f
                canvas.drawText("• Smooth zooming, vertical paging, and jump-to-page navigation", 60f, y, paint)
                y += 20f
                canvas.drawText("• Local private text notes linked to each page", 60f, y, paint)
                y += 20f
                canvas.drawText("• Storage Access Framework integration without dangerous permissions", 60f, y, paint)
                y += 40f
                canvas.drawText("Annotations and notes are stored strictly locally in Room Database.", 40f, y, paint)

                document.finishPage(page)
                FileOutputStream(file).use { out -> document.writeTo(out) }
                document.close()
                written = true
            } catch (_: Throwable) {
                written = false
            }

            if (!written || file.length() == 0L) {
                FileOutputStream(file).use { it.write(generateFallbackPdfBytes()) }
            }
        }
        return getSafeUri(context, file)
    }

    private fun generateFallbackPdfBytes(): ByteArray {
        val pdf = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n" +
                "4 0 obj\n<< /Length 125 >>\nstream\nBT\n/F1 18 Tf\n50 780 Td\n(NotOffice563 - Sample PDF Document) Tj\nET\nBT\n/F1 12 Tf\n50 740 Td\n(Welcome to NotOffice563 local document workspace.) Tj\nET\nendstream\nendobj\n" +
                "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n" +
                "xref\n0 6\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n0000000115 00000 n \n0000000229 00000 n \n0000000405 00000 n \ntrailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n474\n%%EOF\n"
        return pdf.toByteArray(StandardCharsets.ISO_8859_1)
    }

    fun createSampleDocx(context: Context): Uri {
        val file = File(context.filesDir, "Sample_Project_Proposal.docx")
        if (!file.exists()) {
            val content = """Executive Summary
NotOffice563 provides an independent, unified mobile document reading and management workspace on Android.

Objective & Scope
The application centralizes access to Word, Excel, PowerPoint, and PDF files. Users can quickly organize, favorite, search, and inspect file metadata without reliance on external proprietary suites.

Core Architecture
The solution uses MVVM with Room Database for metadata, DataStore for preferences, and Android Storage Access Framework for privacy-conscious file operations."""
            val bytes = DocxParser.createBasicDocx("Project Proposal & Technical Brief", content)
            FileOutputStream(file).use { it.write(bytes) }
        }
        return getSafeUri(context, file)
    }

    fun createSampleXlsx(context: Context): Uri {
        val file = File(context.filesDir, "Sample_Quarterly_Budget.xlsx")
        if (!file.exists()) {
            val bos = FileOutputStream(file)
            ZipOutputStream(bos).use { zos ->
                // [Content_Types].xml
                zos.putNextEntry(ZipEntry("[Content_Types].xml"))
                zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
</Types>""".toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()

                // _rels/.rels
                zos.putNextEntry(ZipEntry("_rels/.rels"))
                zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()

                // xl/workbook.xml
                zos.putNextEntry(ZipEntry("xl/workbook.xml"))
                zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <sheets>
        <sheet name="Budget 2026" sheetId="1" r:id="rId1"/>
    </sheets>
</workbook>""".toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()

                // xl/sharedStrings.xml
                val strings = listOf(
                    "Department", "Q1 Budget", "Q2 Budget", "Q3 Budget", "Q4 Budget",
                    "Engineering", "Design & UX", "Operations", "Marketing", "Security"
                )
                val sst = StringBuilder()
                sst.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="${strings.size}" uniqueCount="${strings.size}">
""")
                for (s in strings) {
                    sst.append("<si><t>$s</t></si>")
                }
                sst.append("</sst>")

                zos.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
                zos.write(sst.toString().toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()

                // xl/worksheets/sheet1.xml
                zos.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
                val sheetData = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <sheetData>
        <row r="1">
            <c r="A1" t="s"><v>0</v></c>
            <c r="B1" t="s"><v>1</v></c>
            <c r="C1" t="s"><v>2</v></c>
            <c r="D1" t="s"><v>3</v></c>
            <c r="E1" t="s"><v>4</v></c>
        </row>
        <row r="2">
            <c r="A2" t="s"><v>5</v></c>
            <c r="B2"><v>125000</v></c>
            <c r="C2"><v>140000</v></c>
            <c r="D2"><v>135000</v></c>
            <c r="E2"><v>150000</v></c>
        </row>
        <row r="3">
            <c r="A3" t="s"><v>6</v></c>
            <c r="B3"><v>45000</v></c>
            <c r="C3"><v>48000</v></c>
            <c r="D3"><v>52000</v></c>
            <c r="E3"><v>55000</v></c>
        </row>
        <row r="4">
            <c r="A4" t="s"><v>7</v></c>
            <c r="B4"><v>30000</v></c>
            <c r="C4"><v>32000</v></c>
            <c r="D4"><v>31000</v></c>
            <c r="E4"><v>33000</v></c>
        </row>
    </sheetData>
</worksheet>"""
                zos.write(sheetData.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()
            }
        }
        return getSafeUri(context, file)
    }

    fun createSamplePptx(context: Context): Uri {
        val file = File(context.filesDir, "Sample_Company_Overview.pptx")
        if (!file.exists()) {
            val bos = FileOutputStream(file)
            ZipOutputStream(bos).use { zos ->
                // [Content_Types].xml
                zos.putNextEntry(ZipEntry("[Content_Types].xml"))
                zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
    <Override PartName="/ppt/slides/slide1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
    <Override PartName="/ppt/slides/slide2.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
</Types>""".toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()

                // ppt/slides/slide1.xml
                zos.putNextEntry(ZipEntry("ppt/slides/slide1.xml"))
                zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
    <p:cSld>
        <p:spTree>
            <p:sp>
                <p:txBody>
                    <a:p><a:r><a:t>NotOffice563 Overview</a:t></a:r></a:p>
                    <a:p><a:r><a:t>Independent document workspace built for Android 10+</a:t></a:r></a:p>
                    <a:p><a:r><a:t>Native viewing for Word, Excel, PowerPoint, and PDF files</a:t></a:r></a:p>
                </p:txBody>
            </p:sp>
        </p:spTree>
    </p:cSld>
</p:sld>""".toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()

                // ppt/slides/slide2.xml
                zos.putNextEntry(ZipEntry("ppt/slides/slide2.xml"))
                zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
    <p:cSld>
        <p:spTree>
            <p:sp>
                <p:txBody>
                    <a:p><a:r><a:t>Key Architectural Principles</a:t></a:r></a:p>
                    <a:p><a:r><a:t>No external cloud dependencies or forced logins</a:t></a:r></a:p>
                    <a:p><a:r><a:t>Storage Access Framework content URI persistence</a:t></a:r></a:p>
                    <a:p><a:r><a:t>Reactive Room metadata caching and Jetpack Compose UI</a:t></a:r></a:p>
                </p:txBody>
            </p:sp>
        </p:spTree>
    </p:cSld>
</p:sld>""".toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()
            }
        }
        return getSafeUri(context, file)
    }
}
