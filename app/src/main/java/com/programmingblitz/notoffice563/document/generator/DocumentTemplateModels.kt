package com.programmingblitz.notoffice563.document.generator

import com.programmingblitz.notoffice563.domain.model.DocumentType

enum class DocumentFormatCategory(
    val title: String,
    val extension: String,
    val mimeType: String,
    val documentType: DocumentType
) {
    DOCX(
        title = "Word Document",
        extension = "docx",
        mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        documentType = DocumentType.WORD_DOCX
    ),
    XLSX(
        title = "Spreadsheet",
        extension = "xlsx",
        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        documentType = DocumentType.EXCEL_XLSX
    ),
    PPTX(
        title = "Presentation",
        extension = "pptx",
        mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        documentType = DocumentType.PPT_PPTX
    ),
    PDF(
        title = "PDF Document",
        extension = "pdf",
        mimeType = "application/pdf",
        documentType = DocumentType.PDF
    )
}

data class DocumentStarterTemplate(
    val id: String,
    val title: String,
    val description: String,
    val format: DocumentFormatCategory,
    val defaultFileName: String
)
