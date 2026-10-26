package com.programmingblitz.notoffice563.document.parser

import android.util.Xml
import com.programmingblitz.notoffice563.domain.model.ParsedSpreadsheet
import com.programmingblitz.notoffice563.domain.model.SpreadsheetSheet
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object XlsxParser {

    fun parse(inputStream: InputStream): ParsedSpreadsheet {
        val sheetEntries = mutableMapOf<String, ByteArray>()
        var sharedStringsBytes: ByteArray? = null
        var workbookBytes: ByteArray? = null

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val name = entry.name
                if (name == "xl/workbook.xml") {
                    workbookBytes = zip.readBytes()
                } else if (name == "xl/sharedStrings.xml") {
                    sharedStringsBytes = zip.readBytes()
                } else if (name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml")) {
                    sheetEntries[name] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        // 1. Parse shared strings table
        val sharedStrings = mutableListOf<String>()
        if (sharedStringsBytes != null) {
            runCatching {
                val parser = Xml.newPullParser()
                parser.setInput(ByteArrayInputStream(sharedStringsBytes), "UTF-8")
                var eventType = parser.eventType
                var inSi = false
                var inT = false
                var currentStr = StringBuilder()

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "si") {
                                inSi = true
                                currentStr = StringBuilder()
                            } else if (parser.name == "t" && inSi) {
                                inT = true
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (inT) {
                                currentStr.append(parser.text)
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "t") {
                                inT = false
                            } else if (parser.name == "si") {
                                sharedStrings.add(currentStr.toString())
                                inSi = false
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
        }

        // 2. Parse sheet names from workbook.xml
        val sheetNames = mutableListOf<String>()
        if (workbookBytes != null) {
            runCatching {
                val parser = Xml.newPullParser()
                parser.setInput(ByteArrayInputStream(workbookBytes), "UTF-8")
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "sheet") {
                        val name = parser.getAttributeValue(null, "name")
                        if (!name.isNullOrBlank()) {
                            sheetNames.add(name)
                        }
                    }
                    eventType = parser.next()
                }
            }
        }

        // 3. Parse each sheet
        val parsedSheets = mutableListOf<SpreadsheetSheet>()
        val sortedKeys = sheetEntries.keys.sorted()

        for ((index, key) in sortedKeys.withIndex()) {
            val bytes = sheetEntries[key] ?: continue
            val sheetName = sheetNames.getOrNull(index) ?: "Sheet ${index + 1}"

            val rowDataMap = mutableMapOf<Int, MutableMap<Int, String>>()
            var maxCol = 0
            var maxRow = 0

            runCatching {
                val parser = Xml.newPullParser()
                parser.setInput(ByteArrayInputStream(bytes), "UTF-8")
                var eventType = parser.eventType

                var currentRowIndex = 0
                var currentCellRef = ""
                var cellType = ""
                var cellValue = ""
                var inValue = false
                var inInlineT = false

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "row" -> {
                                    val rAttr = parser.getAttributeValue(null, "r")
                                    currentRowIndex = (rAttr?.toIntOrNull() ?: (currentRowIndex + 1)) - 1
                                    if (currentRowIndex > maxRow) maxRow = currentRowIndex
                                }
                                "c" -> {
                                    currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                                    cellType = parser.getAttributeValue(null, "t") ?: ""
                                    cellValue = ""
                                }
                                "v" -> inValue = true
                                "t" -> if (cellType == "inlineStr") inInlineT = true
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (inValue) {
                                cellValue += parser.text
                            } else if (inInlineT) {
                                cellValue += parser.text
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            when (parser.name) {
                                "v" -> inValue = false
                                "t" -> inInlineT = false
                                "c" -> {
                                    val colIdx = if (currentCellRef.isNotEmpty()) {
                                        columnLetterToIndex(currentCellRef.filter { it.isLetter() })
                                    } else 0

                                    if (colIdx > maxCol) maxCol = colIdx

                                    val resolvedText = when (cellType) {
                                        "s" -> {
                                            val sIndex = cellValue.trim().toIntOrNull()
                                            if (sIndex != null && sIndex in sharedStrings.indices) {
                                                sharedStrings[sIndex]
                                            } else cellValue
                                        }
                                        "b" -> if (cellValue == "1") "TRUE" else "FALSE"
                                        else -> cellValue.trim()
                                    }

                                    rowDataMap.getOrPut(currentRowIndex) { mutableMapOf() }[colIdx] = resolvedText
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }

            // Convert to 2D list
            val finalRowCount = (maxRow + 1).coerceAtMost(200)
            val finalColCount = (maxCol + 1).coerceAtMost(30)
            val rows = mutableListOf<List<String>>()

            for (r in 0 until finalRowCount) {
                val rowCells = mutableListOf<String>()
                val rowMap = rowDataMap[r] ?: emptyMap()
                for (c in 0 until finalColCount) {
                    rowCells.add(rowMap[c] ?: "")
                }
                rows.add(rowCells)
            }

            parsedSheets.add(
                SpreadsheetSheet(
                    name = sheetName,
                    rows = rows,
                    maxColumns = finalColCount,
                    maxRows = finalRowCount
                )
            )
        }

        return ParsedSpreadsheet(
            sheets = if (parsedSheets.isNotEmpty()) parsedSheets else listOf(
                SpreadsheetSheet("Sheet1", listOf(listOf("Empty sheet")), 1, 1)
            )
        )
    }

    private fun columnLetterToIndex(colLetters: String): Int {
        var result = 0
        for (char in colLetters.uppercase()) {
            if (char in 'A'..'Z') {
                result = result * 26 + (char - 'A' + 1)
            }
        }
        return (result - 1).coerceAtLeast(0)
    }

    fun indexToColumnLetter(index: Int): String {
        var num = index + 1
        val result = StringBuilder()
        while (num > 0) {
            val rem = (num - 1) % 26
            result.insert(0, ('A' + rem))
            num = (num - 1) / 26
        }
        return result.toString()
    }
}
