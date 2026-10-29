package com.programmingblitz.notoffice563.document.parser

import com.programmingblitz.notoffice563.domain.model.ParsedSpreadsheet
import com.programmingblitz.notoffice563.domain.model.SpreadsheetSheet
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Parser for legacy Excel formats:
 * - Excel 97-2003 Binary (.xls, BIFF8/BIFF5) via OLE2 container
 * - Comma Separated Values (.csv)
 * - Tab Separated Values (.tsv)
 */
object XlsParser {

    fun parse(inputStream: InputStream): ParsedSpreadsheet {
        val bytes = inputStream.readBytes()
        return parseBytes(bytes)
    }

    fun parseBytes(bytes: ByteArray): ParsedSpreadsheet {
        // 1. Check if OLE2 Compound Document
        if (Ole2Container.isOle2(bytes)) {
            return parseOle2Xls(bytes)
        }

        // 2. Check if raw BIFF stream (starts with BOF 0x0809)
        if (bytes.size >= 4 && bytes[0] == 0x09.toByte() && bytes[1] == 0x08.toByte()) {
            return parseBiffStream(bytes)
        }

        // 3. Fallback: Parse as CSV
        return parseCsvBytes(bytes, ",")
    }

    private fun parseOle2Xls(bytes: ByteArray): ParsedSpreadsheet {
        val container = Ole2Container.parse(bytes)
        val workbookBytes = container.streams["Workbook"] ?: container.streams["Book"]
            ?: throw IllegalArgumentException("Workbook stream not found in legacy .xls file.")
        return parseBiffStream(workbookBytes)
    }

    /**
     * Parses a BIFF8/BIFF5 binary record stream.
     */
    fun parseBiffStream(stream: ByteArray): ParsedSpreadsheet {
        val buf = ByteBuffer.wrap(stream).order(ByteOrder.LITTLE_ENDIAN)
        var pos = 0
        val len = stream.size

        data class BoundSheet(val name: String, val streamOffset: Int)
        val boundSheets = mutableListOf<BoundSheet>()
        val sst = mutableListOf<String>()

        // 1. First pass: Collect BoundSheets and Shared String Table (SST)
        while (pos + 4 <= len) {
            val opcode = buf.getShort(pos).toInt() and 0xFFFF
            val recLen = buf.getShort(pos + 2).toInt() and 0xFFFF
            val recDataPos = pos + 4
            pos += 4 + recLen
            if (recDataPos + recLen > len) break

            when (opcode) {
                // BOUNDSHEET: sheet name and stream offset
                0x0085 -> {
                    if (recLen >= 8) {
                        val streamOffset = buf.getInt(recDataPos)
                        val cch = buf.get(recDataPos + 6).toInt() and 0xFF
                        val flags = buf.get(recDataPos + 7).toInt() and 0xFF
                        val isUtf16 = (flags and 1) != 0
                        val name = if (isUtf16) {
                            val byteCount = minOf(cch * 2, recLen - 8)
                            String(stream, recDataPos + 8, byteCount, Charsets.UTF_16LE)
                        } else {
                            val byteCount = minOf(cch, recLen - 8)
                            String(stream, recDataPos + 8, byteCount, Charset.forName("windows-1252"))
                        }
                        boundSheets.add(BoundSheet(name.ifBlank { "Sheet ${boundSheets.size + 1}" }, streamOffset))
                    }
                }

                // SST: Shared String Table
                0x00FC -> {
                    parseSstRecord(stream, recDataPos, recLen, sst)
                }
            }
        }

        // 2. Second pass: Parse sheet cell contents
        val parsedSheets = mutableListOf<SpreadsheetSheet>()

        // Helper to parse cells within a sheet block (from BOF to EOF)
        fun parseSheetAt(sheetName: String, startOffset: Int): SpreadsheetSheet {
            var curPos = startOffset.coerceAtLeast(0).coerceAtMost(len)
            // Map of (row, col) to cell string value
            val cells = mutableMapOf<Pair<Int, Int>, String>()
            var maxRow = 0
            var maxCol = 0

            var insideWorksheet = false

            while (curPos + 4 <= len) {
                val opcode = buf.getShort(curPos).toInt() and 0xFFFF
                val recLen = buf.getShort(curPos + 2).toInt() and 0xFFFF
                val dataPos = curPos + 4
                curPos += 4 + recLen
                if (dataPos + recLen > len) break

                when (opcode) {
                    0x0809 -> { // BOF
                        if (recLen >= 4) {
                            val type = buf.getShort(dataPos + 2).toInt() and 0xFFFF
                            if (type == 0x0010) { // 0x0010 = Worksheet
                                insideWorksheet = true
                            }
                        }
                    }

                    // LABELSST: cell string referencing SST
                    0x00FD -> {
                        if (insideWorksheet && recLen >= 10) {
                            val row = buf.getShort(dataPos).toInt() and 0xFFFF
                            val col = buf.getShort(dataPos + 2).toInt() and 0xFFFF
                            val sstIndex = buf.getInt(dataPos + 6)
                            val str = sst.getOrElse(sstIndex) { "" }
                            cells[Pair(row, col)] = str
                            maxRow = maxOf(maxRow, row + 1)
                            maxCol = maxOf(maxCol, col + 1)
                        }
                    }

                    // LABEL: inline string
                    0x0204 -> {
                        if (insideWorksheet && recLen >= 8) {
                            val row = buf.getShort(dataPos).toInt() and 0xFFFF
                            val col = buf.getShort(dataPos + 2).toInt() and 0xFFFF
                            val cch = buf.getShort(dataPos + 6).toInt() and 0xFFFF
                            val str = if (recLen >= 8 + cch) {
                                String(stream, dataPos + 8, cch, Charset.forName("windows-1252"))
                            } else ""
                            cells[Pair(row, col)] = str
                            maxRow = maxOf(maxRow, row + 1)
                            maxCol = maxOf(maxCol, col + 1)
                        }
                    }

                    // NUMBER: IEEE 754 64-bit double
                    0x0203 -> {
                        if (insideWorksheet && recLen >= 14) {
                            val row = buf.getShort(dataPos).toInt() and 0xFFFF
                            val col = buf.getShort(dataPos + 2).toInt() and 0xFFFF
                            val num = buf.getDouble(dataPos + 6)
                            cells[Pair(row, col)] = formatNumber(num)
                            maxRow = maxOf(maxRow, row + 1)
                            maxCol = maxOf(maxCol, col + 1)
                        }
                    }

                    // RK: compressed 32-bit number
                    0x027E -> {
                        if (insideWorksheet && recLen >= 10) {
                            val row = buf.getShort(dataPos).toInt() and 0xFFFF
                            val col = buf.getShort(dataPos + 2).toInt() and 0xFFFF
                            val rk = buf.getInt(dataPos + 6)
                            cells[Pair(row, col)] = decodeRk(rk)
                            maxRow = maxOf(maxRow, row + 1)
                            maxCol = maxOf(maxCol, col + 1)
                        }
                    }

                    // MULRK: multiple RK values in a single row
                    0x00BD -> {
                        if (insideWorksheet && recLen >= 6) {
                            val row = buf.getShort(dataPos).toInt() and 0xFFFF
                            val firstCol = buf.getShort(dataPos + 2).toInt() and 0xFFFF
                            val lastCol = buf.getShort(dataPos + recLen - 2).toInt() and 0xFFFF
                            var rkOffset = dataPos + 4
                            var c = firstCol
                            while (c <= lastCol && rkOffset + 6 <= dataPos + recLen) {
                                val rk = buf.getInt(rkOffset + 2)
                                cells[Pair(row, c)] = decodeRk(rk)
                                maxRow = maxOf(maxRow, row + 1)
                                maxCol = maxOf(maxCol, c + 1)
                                rkOffset += 6
                                c++
                            }
                        }
                    }

                    // FORMULA: formula cell
                    0x0006 -> {
                        if (insideWorksheet && recLen >= 14) {
                            val row = buf.getShort(dataPos).toInt() and 0xFFFF
                            val col = buf.getShort(dataPos + 2).toInt() and 0xFFFF
                            val b6 = stream[dataPos + 12].toInt() and 0xFF
                            val b7 = stream[dataPos + 13].toInt() and 0xFF
                            val isSpecial = (b6 == 0xFF && b7 == 0xFF)

                            if (!isSpecial) {
                                val num = buf.getDouble(dataPos + 6)
                                cells[Pair(row, col)] = formatNumber(num)
                            } else {
                                val typeCode = stream[dataPos + 6].toInt() and 0xFF
                                when (typeCode) {
                                    1 -> cells[Pair(row, col)] = if (stream[dataPos + 8] != 0.toByte()) "TRUE" else "FALSE"
                                    2 -> cells[Pair(row, col)] = "#ERROR"
                                }
                            }
                            maxRow = maxOf(maxRow, row + 1)
                            maxCol = maxOf(maxCol, col + 1)
                        }
                    }

                    // STRING: formula string result
                    0x00D7 -> {
                        // String result following a formula
                    }

                    0x000A -> { // EOF
                        if (insideWorksheet) {
                            break // Sheet finished
                        }
                    }
                }
            }

            // Convert cells map into standard 2D grid
            val rowCount = maxOf(maxRow, 1)
            val colCount = maxOf(maxCol, 1)
            val grid = ArrayList<List<String>>(rowCount)

            for (r in 0 until rowCount) {
                val rowCells = ArrayList<String>(colCount)
                for (c in 0 until colCount) {
                    rowCells.add(cells[Pair(r, c)] ?: "")
                }
                grid.add(rowCells)
            }

            return SpreadsheetSheet(
                name = sheetName,
                rows = grid,
                maxColumns = colCount,
                maxRows = rowCount
            )
        }

        if (boundSheets.isNotEmpty()) {
            for (bs in boundSheets) {
                parsedSheets.add(parseSheetAt(bs.name, bs.streamOffset))
            }
        } else {
            // No BoundSheets found, parse entire stream as single sheet
            parsedSheets.add(parseSheetAt("Sheet 1", 0))
        }

        return ParsedSpreadsheet(
            sheets = if (parsedSheets.isNotEmpty()) parsedSheets else listOf(
                SpreadsheetSheet("Sheet 1", listOf(listOf("")), 1, 1)
            )
        )
    }

    /**
     * Parses the Shared String Table (SST) record, including Unicode and 8-bit strings.
     */
    private fun parseSstRecord(
        stream: ByteArray,
        dataPos: Int,
        recLen: Int,
        outSst: MutableList<String>
    ) {
        val buf = ByteBuffer.wrap(stream).order(ByteOrder.LITTLE_ENDIAN)
        if (recLen < 8) return

        val totalStrings = buf.getInt(dataPos)
        val uniqueStrings = buf.getInt(dataPos + 4)
        var p = dataPos + 8
        val end = dataPos + recLen
        val cp1252 = Charset.forName("windows-1252")

        var strCount = 0
        while (p < end && strCount < uniqueStrings) {
            if (p + 3 > stream.size) break
            val cch = buf.getShort(p).toInt() and 0xFFFF
            val flags = stream[p + 2].toInt() and 0xFF
            p += 3

            val isUtf16 = (flags and 1) != 0
            val hasAsian = (flags and 4) != 0
            val hasRich = (flags and 8) != 0

            var crun = 0
            var cbExtRst = 0
            if (hasRich && p + 2 <= stream.size) {
                crun = buf.getShort(p).toInt() and 0xFFFF
                p += 2
            }
            if (hasAsian && p + 4 <= stream.size) {
                cbExtRst = buf.getInt(p)
                p += 4
            }

            val str = if (isUtf16) {
                val byteLen = minOf(cch * 2, (stream.size - p).coerceAtLeast(0))
                val s = if (byteLen > 0) String(stream, p, byteLen, Charsets.UTF_16LE) else ""
                p += byteLen
                s
            } else {
                val byteLen = minOf(cch, (stream.size - p).coerceAtLeast(0))
                val s = if (byteLen > 0) String(stream, p, byteLen, cp1252) else ""
                p += byteLen
                s
            }

            // Skip rich text and Asian formatting data
            if (hasRich) p += (crun * 4).coerceAtMost(stream.size - p)
            if (hasAsian) p += cbExtRst.coerceAtMost(stream.size - p)

            outSst.add(str)
            strCount++
        }
    }

    /**
     * Decodes an RK 32-bit compressed integer or IEEE-754 double value.
     */
    private fun decodeRk(rk: Int): String {
        val isDiv100 = (rk and 1) != 0
        val isInt = (rk and 2) != 0

        return if (isInt) {
            var v = (rk shr 2).toDouble()
            if (isDiv100) v /= 100.0
            formatNumber(v)
        } else {
            val high30 = (rk.toLong() and 0xFFFFFFFC.toLong()) shl 32
            var v = java.lang.Double.longBitsToDouble(high30)
            if (isDiv100) v /= 100.0
            formatNumber(v)
        }
    }

    private fun formatNumber(num: Double): String {
        if (num.isNaN() || num.isInfinite()) return num.toString()
        return if (num == Math.floor(num) && num in -1e15..1e15) {
            num.toLong().toString()
        } else {
            // Trim excessive floating precision
            val s = String.format(java.util.Locale.US, "%.4f", num).trimEnd('0')
            if (s.endsWith(".")) s.dropLast(1) else s
        }
    }

    // =========================================================================
    // CSV / TSV Parser
    // =========================================================================

    fun parseCsv(inputStream: InputStream, delimiter: String = ","): ParsedSpreadsheet {
        val bytes = inputStream.readBytes()
        return parseCsvBytes(bytes, delimiter)
    }

    fun parseCsvBytes(bytes: ByteArray, delimiter: String = ","): ParsedSpreadsheet {
        val text = String(bytes, Charsets.UTF_8).replace("\r\n", "\n").replace("\r", "\n")
        val lines = text.split('\n')
        val rows = mutableListOf<List<String>>()
        var maxCols = 0

        for (line in lines) {
            if (line.isEmpty() && rows.isEmpty()) continue
            val tokens = parseCsvLine(line, delimiter[0])
            rows.add(tokens)
            maxCols = maxOf(maxCols, tokens.size)
        }

        if (rows.isEmpty()) {
            rows.add(listOf(""))
            maxCols = 1
        }

        // Pad rows to uniform maxCols
        val uniformRows = rows.map { row ->
            if (row.size < maxCols) row + List(maxCols - row.size) { "" } else row
        }

        return ParsedSpreadsheet(
            sheets = listOf(
                SpreadsheetSheet(
                    name = "Data",
                    rows = uniformRows,
                    maxColumns = maxCols,
                    maxRows = uniformRows.size
                )
            )
        )
    }

    private fun parseCsvLine(line: String, sep: Char): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var insideQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (insideQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++
                    } else {
                        insideQuotes = !insideQuotes
                    }
                }
                c == sep && !insideQuotes -> {
                    result.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        result.add(sb.toString().trim())
        return result
    }
}
