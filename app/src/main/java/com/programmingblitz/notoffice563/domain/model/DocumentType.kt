package com.programmingblitz.notoffice563.domain.model

enum class DocumentCategory(val displayName: String) {
    ALL("All Files"),
    DOCUMENTS("Documents"),
    SPREADSHEETS("Spreadsheets"),
    PRESENTATIONS("Presentations"),
    PDFS("PDFs")
}

enum class DocumentType(
    val extension: String,
    val category: DocumentCategory,
    val friendlyName: String,
    val isLegacy: Boolean = false
) {
    WORD_DOCX("docx", DocumentCategory.DOCUMENTS, "Word Document (.docx)", false),
    WORD_DOC("doc", DocumentCategory.DOCUMENTS, "Legacy Word (.doc)", true),
    EXCEL_XLSX("xlsx", DocumentCategory.SPREADSHEETS, "Excel Spreadsheet (.xlsx)", false),
    EXCEL_XLS("xls", DocumentCategory.SPREADSHEETS, "Legacy Excel (.xls)", true),
    PPT_PPTX("pptx", DocumentCategory.PRESENTATIONS, "PowerPoint Presentation (.pptx)", false),
    PPT_PPT("ppt", DocumentCategory.PRESENTATIONS, "Legacy PowerPoint (.ppt)", true),
    PDF("pdf", DocumentCategory.PDFS, "Portable Document Format (.pdf)", false),
    UNKNOWN("bin", DocumentCategory.ALL, "Unknown Document", false);

    companion object {
        fun fromExtension(ext: String?): DocumentType {
            val cleanExt = ext?.lowercase()?.trim()?.removePrefix(".") ?: ""
            return when (cleanExt) {
                "docx" -> WORD_DOCX
                "doc" -> WORD_DOC
                "xlsx" -> EXCEL_XLSX
                "xls" -> EXCEL_XLS
                "pptx" -> PPT_PPTX
                "ppt" -> PPT_PPT
                "pdf" -> PDF
                else -> UNKNOWN
            }
        }

        fun fromMimeOrFilename(mime: String?, filename: String?): DocumentType {
            val byMime = when (mime?.lowercase()) {
                "application/pdf" -> PDF
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> WORD_DOCX
                "application/msword" -> WORD_DOC
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> EXCEL_XLSX
                "application/vnd.ms-excel" -> EXCEL_XLS
                "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> PPT_PPTX
                "application/vnd.ms-powerpoint" -> PPT_PPT
                else -> null
            }
            if (byMime != null) return byMime

            val ext = filename?.substringAfterLast('.', "")
            return fromExtension(ext)
        }
    }
}
