package com.programmingblitz.notoffice563.document.generator

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object XlsxGenerator {

    val templates = listOf(
        DocumentStarterTemplate(
            id = "xlsx_blank",
            title = "Blank Spreadsheet",
            description = "Empty grid ready for custom tables and formulas",
            format = DocumentFormatCategory.XLSX,
            defaultFileName = "New Spreadsheet"
        ),
        DocumentStarterTemplate(
            id = "xlsx_budget",
            title = "Monthly Budget Template",
            description = "Expense categories, planned allocations, and actual spending",
            format = DocumentFormatCategory.XLSX,
            defaultFileName = "Monthly Budget"
        ),
        DocumentStarterTemplate(
            id = "xlsx_tracker",
            title = "Project Tracker Template",
            description = "Task list with assignees, status, priorities, and deadlines",
            format = DocumentFormatCategory.XLSX,
            defaultFileName = "Project Tracker"
        )
    )

    fun generate(templateId: String, sheetName: String = "Sheet1"): ByteArray {
        val rows: List<List<String>> = when (templateId) {
            "xlsx_budget" -> listOf(
                listOf("Category", "Budgeted ($)", "Actual ($)", "Difference ($)", "Status")
            )
            "xlsx_tracker" -> listOf(
                listOf("Task ID", "Task Title", "Assignee", "Priority", "Status", "Due Date")
            )
            else -> emptyList() // Blank spreadsheet has NO prewritten rows or sample data!
        }

        return buildXlsxZip(sheetName, rows)
    }

    fun buildXlsxFromSpreadsheet(parsedSpreadsheet: com.programmingblitz.notoffice563.domain.model.ParsedSpreadsheet): ByteArray {
        val bos = ByteArrayOutputStream()
        val sheets = if (parsedSpreadsheet.sheets.isNotEmpty()) parsedSpreadsheet.sheets else listOf(
            com.programmingblitz.notoffice563.domain.model.SpreadsheetSheet("Sheet1", emptyList(), 1, 1)
        )
        ZipOutputStream(bos).use { zos ->
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            val sheetOverrides = sheets.mapIndexed { idx, _ ->
                "    <Override PartName=\"/xl/worksheets/sheet${idx + 1}.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
            }.joinToString("\n")

            val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
$sheetOverrides
    <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
    <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
</Types>"""
            zos.write(contentTypes.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("_rels/.rels"))
            val rootRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
</Relationships>"""
            zos.write(rootRels.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            val wbSheetRels = sheets.mapIndexed { idx, _ ->
                "    <Relationship Id=\"rId${idx + 1}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet${idx + 1}.xml\"/>"
            }.joinToString("\n")
            val stylesRelId = "rId${sheets.size + 1}"
            val wbRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
$wbSheetRels
    <Relationship Id="$stylesRelId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
            zos.write(wbRels.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("xl/workbook.xml"))
            val sheetsXml = sheets.mapIndexed { idx, s ->
                "        <sheet name=\"${escapeXml(s.name)}\" sheetId=\"${idx + 1}\" r:id=\"rId${idx + 1}\"/>"
            }.joinToString("\n")
            val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <sheets>
$sheetsXml
    </sheets>
</workbook>"""
            zos.write(workbookXml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("xl/styles.xml"))
            val stylesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <fonts count="2">
        <font><sz val="11"/><name val="Calibri"/></font>
        <font><b/><sz val="11"/><name val="Calibri"/></font>
    </fonts>
    <fills count="2">
        <fill><patternFill patternType="none"/></fill>
        <fill><patternFill patternType="gray125"/></fill>
    </fills>
    <borders count="1">
        <border><left/><right/><top/><bottom/><diagonal/></border>
    </borders>
    <cellStyleXfs count="1">
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
    </cellStyleXfs>
    <cellXfs count="2">
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
        <xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
    </cellXfs>
</styleSheet>"""
            zos.write(stylesXml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("docProps/core.xml"))
            val coreProps = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
    xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:title>${escapeXml(sheets.firstOrNull()?.name ?: "Workbook")}</dc:title>
    <dc:creator>NotOffice563</dc:creator>
</cp:coreProperties>"""
            zos.write(coreProps.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            sheets.forEachIndexed { idx, sheet ->
                zos.putNextEntry(ZipEntry("xl/worksheets/sheet${idx + 1}.xml"))
                val sheetBuilder = StringBuilder()
                sheetBuilder.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <sheetData>
""")
                for ((rowIndex, row) in sheet.rows.withIndex()) {
                    val rNum = rowIndex + 1
                    val isHeader = rowIndex == 0
                    val styleAttr = if (isHeader) " s=\"1\"" else ""

                    sheetBuilder.append("        <row r=\"$rNum\">\n")
                    for ((colIndex, cellVal) in row.withIndex()) {
                        if (cellVal.isEmpty()) continue
                        val colLetter = getColumnLetter(colIndex)
                        val cellRef = "$colLetter$rNum"
                        val isFormula = cellVal.startsWith("=")
                        val isNumber = cellVal.toDoubleOrNull() != null && !isHeader

                        if (isFormula) {
                            val formulaBody = cellVal.substring(1)
                            val calculated = com.programmingblitz.notoffice563.document.evaluator.SpreadsheetFormulaEvaluator.evaluate(cellVal, sheet.rows)
                            sheetBuilder.append("            <c r=\"$cellRef\"$styleAttr><f>${escapeXml(formulaBody)}</f><v>${escapeXml(calculated)}</v></c>\n")
                        } else if (isNumber) {
                            sheetBuilder.append("            <c r=\"$cellRef\"$styleAttr><v>$cellVal</v></c>\n")
                        } else {
                            sheetBuilder.append("            <c r=\"$cellRef\" t=\"inlineStr\"$styleAttr><is><t>${escapeXml(cellVal)}</t></is></c>\n")
                        }
                    }
                    sheetBuilder.append("        </row>\n")
                }
                sheetBuilder.append("""    </sheetData>
</worksheet>""")
                zos.write(sheetBuilder.toString().toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()
            }
        }
        return bos.toByteArray()
    }

    private fun buildXlsxZip(sheetName: String, rows: List<List<String>>): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            // 1. [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
    <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
</Types>"""
            zos.write(contentTypes.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 2. _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            val rootRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
</Relationships>"""
            zos.write(rootRels.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 3. xl/_rels/workbook.xml.rels
            zos.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            val wbRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
            zos.write(wbRels.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 4. xl/workbook.xml
            zos.putNextEntry(ZipEntry("xl/workbook.xml"))
            val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <sheets>
        <sheet name="${escapeXml(sheetName)}" sheetId="1" r:id="rId1"/>
    </sheets>
</workbook>"""
            zos.write(workbookXml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 5. xl/styles.xml
            zos.putNextEntry(ZipEntry("xl/styles.xml"))
            val stylesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <fonts count="2">
        <font><sz val="11"/><name val="Calibri"/></font>
        <font><b/><sz val="11"/><name val="Calibri"/></font>
    </fonts>
    <fills count="2">
        <fill><patternFill patternType="none"/></fill>
        <fill><patternFill patternType="gray125"/></fill>
    </fills>
    <borders count="1">
        <border><left/><right/><top/><bottom/><diagonal/></border>
    </borders>
    <cellStyleXfs count="1">
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
    </cellStyleXfs>
    <cellXfs count="2">
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
        <xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
    </cellXfs>
</styleSheet>"""
            zos.write(stylesXml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 6. docProps/core.xml
            zos.putNextEntry(ZipEntry("docProps/core.xml"))
            val coreProps = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
    xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:title>${escapeXml(sheetName)}</dc:title>
    <dc:creator>NotOffice563</dc:creator>
</cp:coreProperties>"""
            zos.write(coreProps.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // 7. xl/worksheets/sheet1.xml
            zos.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            val sheetBuilder = StringBuilder()
            sheetBuilder.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <sheetData>
""")
            for ((rowIndex, row) in rows.withIndex()) {
                val rNum = rowIndex + 1
                val isHeader = rowIndex == 0
                val styleAttr = if (isHeader) " s=\"1\"" else ""

                sheetBuilder.append("        <row r=\"$rNum\">\n")
                for ((colIndex, cellVal) in row.withIndex()) {
                    val colLetter = getColumnLetter(colIndex)
                    val cellRef = "$colLetter$rNum"
                    val isFormula = cellVal.startsWith("=")
                    val isNumber = cellVal.toDoubleOrNull() != null && !isHeader

                    if (isFormula) {
                        val formulaBody = cellVal.substring(1)
                        val calculated = com.programmingblitz.notoffice563.document.evaluator.SpreadsheetFormulaEvaluator.evaluate(cellVal, rows)
                        sheetBuilder.append("            <c r=\"$cellRef\"$styleAttr><f>${escapeXml(formulaBody)}</f><v>${escapeXml(calculated)}</v></c>\n")
                    } else if (isNumber) {
                        sheetBuilder.append("            <c r=\"$cellRef\"$styleAttr><v>$cellVal</v></c>\n")
                    } else {
                        sheetBuilder.append("            <c r=\"$cellRef\" t=\"inlineStr\"$styleAttr><is><t>${escapeXml(cellVal)}</t></is></c>\n")
                    }
                }
                sheetBuilder.append("        </row>\n")
            }

            sheetBuilder.append("""    </sheetData>
</worksheet>""")
            zos.write(sheetBuilder.toString().toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }
        return bos.toByteArray()
    }

    private fun getColumnLetter(index: Int): String {
        var num = index + 1
        val result = StringBuilder()
        while (num > 0) {
            val rem = (num - 1) % 26
            result.insert(0, ('A' + rem))
            num = (num - 1) / 26
        }
        return result.toString()
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
