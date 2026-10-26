package com.programmingblitz.notoffice563.document.generator

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    val templates = listOf(
        DocumentStarterTemplate(
            id = "pdf_blank",
            title = "Blank PDF Document",
            description = "Standard A4 vector PDF document with header and custom text",
            format = DocumentFormatCategory.PDF,
            defaultFileName = "New Document"
        ),
        DocumentStarterTemplate(
            id = "pdf_letter",
            title = "Formal Letter",
            description = "Professional letterhead, date, recipient block, and sign-off",
            format = DocumentFormatCategory.PDF,
            defaultFileName = "Formal Letter"
        ),
        DocumentStarterTemplate(
            id = "pdf_summary",
            title = "Project Summary Note",
            description = "Executive briefing, deliverables summary, and status badges",
            format = DocumentFormatCategory.PDF,
            defaultFileName = "Project Summary"
        )
    )

    fun generate(templateId: String, title: String): ByteArray {
        val dateStr = SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date())
        val pdfDocument = PdfDocument()

        // Standard A4 dimensions at 72 DPI: 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val primaryBlue = Color.rgb(0, 97, 164)
        val textPrimary = Color.rgb(20, 25, 35)
        val textSecondary = Color.rgb(100, 116, 139)
        val dividerColor = Color.rgb(226, 232, 240)

        var yPos = 60f
        val leftMargin = 50f
        val rightMargin = 545f
        val contentWidth = rightMargin - leftMargin

        // 1. Top Decorative Brand Accent Bar
        paint.color = primaryBlue
        paint.style = Paint.Style.FILL
        canvas.drawRect(leftMargin, yPos, leftMargin + 60f, yPos + 6f, paint)
        yPos += 30f

        when (templateId) {
            "pdf_letter" -> {
                // Header / Letterhead
                paint.color = primaryBlue
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                paint.textSize = 20f
                canvas.drawText("NOTOFFICE563 WORKSPACE", leftMargin, yPos, paint)
                yPos += 18f

                paint.color = textSecondary
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                paint.textSize = 10f
                canvas.drawText("Independent Document Workspace | Generated on $dateStr", leftMargin, yPos, paint)
                yPos += 20f

                // Divider
                paint.color = dividerColor
                paint.strokeWidth = 1f
                canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
                yPos += 35f

                // Recipient block
                paint.color = textPrimary
                paint.textSize = 11f
                canvas.drawText("Date: $dateStr", leftMargin, yPos, paint); yPos += 16f
                canvas.drawText("To: Recipient Name / Organization", leftMargin, yPos, paint); yPos += 16f
                canvas.drawText("Subject: ${if (title.isNotBlank()) title else "Formal Communication"}", leftMargin, yPos, paint)
                yPos += 30f

                // Salutation
                canvas.drawText("Dear Colleague,", leftMargin, yPos, paint)
                yPos += 24f

                val letterParagraphs = listOf(
                    "I am writing to share the current project updates and documentation generated via NotOffice563. Our team has completed the transition to modern open document standards, ensuring seamless compatibility with all modern desktop and mobile productivity tools.",
                    "All local records, files, and annotations are managed with strict zero-telemetry guarantees, preserving full user privacy and data ownership on the local storage partition.",
                    "Please review the attached notes and let us know if additional specifications are needed.",
                    "Sincerely,",
                    "NotOffice563 Project Lead"
                )

                for (p in letterParagraphs) {
                    yPos = drawWrappedText(canvas, paint, p, leftMargin, yPos, contentWidth, 16f)
                    yPos += 12f
                }
            }
            "pdf_summary" -> {
                // Summary Header
                paint.color = primaryBlue
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                paint.textSize = 22f
                canvas.drawText(if (title.isNotBlank()) title else "Executive Project Summary", leftMargin, yPos, paint)
                yPos += 20f

                paint.color = textSecondary
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                paint.textSize = 10f
                canvas.drawText("Project Briefing & Operational Status • $dateStr", leftMargin, yPos, paint)
                yPos += 20f

                paint.color = dividerColor
                paint.strokeWidth = 1.5f
                canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
                yPos += 30f

                val sections = listOf(
                    "1. Executive Overview" to "This summary outlines key product deliverables, including the iOS 26 Liquid Glass-inspired UI system, full OOXML document creation engines, and honest authentication workflows.",
                    "2. Supported Formats & File Integrity" to "NotOffice563 produces authentic, fully compliant OOXML (.docx, .xlsx, .pptx) files and native vector PDFs that conform to ISO specifications and open flawlessly in desktop suites.",
                    "3. Security & Architecture" to "Features zero-trust Firestore security rules, genuine Android Credential Manager sign-in, and offline-first storage fallback."
                )

                for ((secTitle, secBody) in sections) {
                    paint.color = primaryBlue
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    paint.textSize = 13f
                    canvas.drawText(secTitle, leftMargin, yPos, paint)
                    yPos += 18f

                    paint.color = textPrimary
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    paint.textSize = 10.5f
                    yPos = drawWrappedText(canvas, paint, secBody, leftMargin, yPos, contentWidth, 15f)
                    yPos += 16f
                }
            }
            else -> {
                // Blank Document
                paint.color = primaryBlue
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                paint.textSize = 24f
                val docTitle = if (title.isNotBlank()) title else "New PDF Document"
                canvas.drawText(docTitle, leftMargin, yPos, paint)
                yPos += 22f

                paint.color = textSecondary
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                paint.textSize = 10f
                canvas.drawText("Created on $dateStr with NotOffice563", leftMargin, yPos, paint)
                yPos += 20f

                paint.color = dividerColor
                paint.strokeWidth = 1.5f
                canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
                yPos += 35f

                paint.color = textPrimary
                paint.textSize = 11f
                val defaultContent = "This is a genuine vector PDF document created directly on your Android device using NotOffice563. It can be printed, exported, and viewed in any PDF reader."
                yPos = drawWrappedText(canvas, paint, defaultContent, leftMargin, yPos, contentWidth, 16f)
            }
        }

        // Footer
        paint.color = textSecondary
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Page 1 of 1 • NotOffice563 Android Workspace", 595f / 2f, 810f, paint)

        pdfDocument.finishPage(page)

        val bos = ByteArrayOutputStream()
        pdfDocument.writeTo(bos)
        pdfDocument.close()
        return bos.toByteArray()
    }

    private fun drawWrappedText(
        canvas: android.graphics.Canvas,
        paint: Paint,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float
    ): Float {
        var y = startY
        val words = text.split(" ")
        var currentLine = StringBuilder()

        for (word in words) {
            val potentialLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(potentialLine)
            if (width > maxWidth) {
                canvas.drawText(currentLine.toString(), x, y, paint)
                y += lineHeight
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(potentialLine)
            }
        }

        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), x, y, paint)
            y += lineHeight
        }

        return y
    }
}
