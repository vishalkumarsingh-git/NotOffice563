package com.programmingblitz.notoffice563.domain.model

data class DocxParagraph(
    val text: String,
    val isHeading: Boolean = false,
    val headingLevel: Int = 0,
    val isBullet: Boolean = false,
    val isBold: Boolean = false,
    val isItalic: Boolean = false
)

data class DocxTable(
    val rows: List<List<String>>
)

data class ParsedDocxDocument(
    val title: String,
    val creator: String = "",
    val lastModified: String = "",
    val paragraphs: List<DocxParagraph> = emptyList(),
    val wordCount: Int = 0,
    val rawText: String = ""
)

data class SpreadsheetCell(
    val reference: String, // e.g. "A1"
    val column: Int,       // 0-indexed
    val row: Int,          // 0-indexed
    val value: String
)

data class SpreadsheetSheet(
    val name: String,
    val rows: List<List<String>>, // row-major grid
    val maxColumns: Int,
    val maxRows: Int
)

data class ParsedSpreadsheet(
    val sheets: List<SpreadsheetSheet> = emptyList()
)

data class PresentationSlide(
    val slideNumber: Int,
    val title: String,
    val bodyBullets: List<String>,
    val notes: String = ""
)

data class ParsedPresentation(
    val slides: List<PresentationSlide> = emptyList()
)
