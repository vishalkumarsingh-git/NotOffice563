package com.programmingblitz.notoffice563.document.parser

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Robust pure-Kotlin parser for Microsoft Compound Document Files (OLE2 / CFBF).
 * Used by legacy Microsoft Office formats:
 * - Word 97-2003 (.doc) -> streams "WordDocument", "1Table", "0Table", "Data"
 * - Excel 97-2003 (.xls) -> streams "Workbook", "Book"
 * - PowerPoint 97-2003 (.ppt) -> streams "PowerPoint Document", "Current User"
 */
class Ole2Container private constructor(
    private val data: ByteArray,
    private val sectorSize: Int,
    private val miniSectorSize: Int,
    private val miniStreamCutoff: Int,
    private val fat: IntArray,
    private val miniFat: IntArray,
    private val miniStreamData: ByteArray,
    val streams: Map<String, ByteArray>
) {

    companion object {
        private val OLE2_MAGIC = byteArrayOf(
            0xD0.toByte(), 0xCF.toByte(), 0x11.toByte(), 0xE0.toByte(),
            0xA1.toByte(), 0xB1.toByte(), 0x1A.toByte(), 0xE1.toByte()
        )

        const val END_OF_CHAIN = -2 // 0xFFFFFFFE as signed Int
        const val FAT_SECT = -3     // 0xFFFFFFFD
        const val DIF_SECT = -4     // 0xFFFFFFFC
        const val FREE_SECT = -1    // 0xFFFFFFFF

        fun isOle2(bytes: ByteArray): Boolean {
            if (bytes.size < 8) return false
            for (i in 0 until 8) {
                if (bytes[i] != OLE2_MAGIC[i]) return false
            }
            return true
        }

        fun parse(inputStream: InputStream): Ole2Container {
            val bytes = inputStream.readBytes()
            return parse(bytes)
        }

        fun parse(data: ByteArray): Ole2Container {
            if (!isOle2(data)) {
                throw IllegalArgumentException("Not a valid OLE2 compound file: magic signature mismatch.")
            }

            val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

            // Read header
            val sectorShift = buf.getShort(28).toInt() and 0xFFFF
            val miniSectorShift = buf.getShort(30).toInt() and 0xFFFF
            val sectorSize = 1 shl sectorShift
            val miniSectorSize = 1 shl miniSectorShift

            val numFatSectors = buf.getInt(44)
            val firstDirSector = buf.getInt(48)
            val miniStreamCutoff = buf.getInt(56)
            val firstMiniFatSector = buf.getInt(60)
            val numMiniFatSectors = buf.getInt(64)
            val firstDifatSector = buf.getInt(68)
            val numDifatSectors = buf.getInt(72)

            // 1. Read DIFAT (Directory FAT) to get list of all FAT sectors
            val fatSectorIndices = mutableListOf<Int>()
            // First 109 entries in header (offset 76..511)
            for (i in 0 until 109) {
                val sect = buf.getInt(76 + i * 4)
                if (sect >= 0 && fatSectorIndices.size < numFatSectors) {
                    fatSectorIndices.add(sect)
                }
            }

            // Additional DIFAT sectors if numFatSectors > 109
            var currentDifat = firstDifatSector
            var difatCount = 0
            val intsPerSector = sectorSize / 4
            while (currentDifat >= 0 && difatCount < numDifatSectors) {
                val offset = (currentDifat + 1) * sectorSize
                if (offset + sectorSize > data.size) break
                for (i in 0 until (intsPerSector - 1)) {
                    val sect = buf.getInt(offset + i * 4)
                    if (sect >= 0 && fatSectorIndices.size < numFatSectors) {
                        fatSectorIndices.add(sect)
                    }
                }
                currentDifat = buf.getInt(offset + (intsPerSector - 1) * 4)
                difatCount++
            }

            // 2. Build FAT
            val totalFatEntries = fatSectorIndices.size * intsPerSector
            val fat = IntArray(totalFatEntries)
            var fatIdx = 0
            for (fatSec in fatSectorIndices) {
                val offset = (fatSec + 1) * sectorSize
                if (offset + sectorSize <= data.size) {
                    for (i in 0 until intsPerSector) {
                        fat[fatIdx++] = buf.getInt(offset + i * 4)
                    }
                }
            }

            // Helper to read a regular FAT chain
            fun readChain(startSector: Int, size: Long): ByteArray {
                if (startSector < 0 || size <= 0) return ByteArray(0)
                val out = java.io.ByteArrayOutputStream(size.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                var curr = startSector
                var bytesRemaining = size
                var safety = 0
                while (curr >= 0 && curr < fat.size && curr != END_OF_CHAIN && bytesRemaining > 0 && safety < 100000) {
                    val offset = (curr + 1) * sectorSize
                    val toRead = minOf(bytesRemaining, sectorSize.toLong()).toInt()
                    if (offset + toRead <= data.size) {
                        out.write(data, offset, toRead)
                    } else if (offset < data.size) {
                        out.write(data, offset, data.size - offset)
                    }
                    bytesRemaining -= toRead
                    curr = fat[curr]
                    safety++
                }
                return out.toByteArray()
            }

            // 3. Read MiniFAT
            val miniFatData = readChain(firstMiniFatSector, numMiniFatSectors.toLong() * sectorSize)
            val miniFatEntries = miniFatData.size / 4
            val miniFat = IntArray(miniFatEntries)
            val miniFatBuf = ByteBuffer.wrap(miniFatData).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until miniFatEntries) {
                miniFat[i] = miniFatBuf.getInt(i * 4)
            }

            // 4. Read Directory Sectors
            // Directory stream size can be arbitrarily large, read chain until END_OF_CHAIN
            val dirData = readChain(firstDirSector, Long.MAX_VALUE)
            val dirBuf = ByteBuffer.wrap(dirData).order(ByteOrder.LITTLE_ENDIAN)
            val dirEntryCount = dirData.size / 128

            data class DirEntry(
                val name: String,
                val type: Int,
                val startSector: Int,
                val size: Long
            )

            val dirEntries = mutableListOf<DirEntry>()
            for (i in 0 until dirEntryCount) {
                val offset = i * 128
                val nameLen = dirBuf.getShort(offset + 64).toInt() and 0xFFFF
                val type = dirBuf.get(offset + 66).toInt() and 0xFF
                val startSector = dirBuf.getInt(offset + 116)
                val size = dirBuf.getLong(offset + 120)

                val name = if (nameLen > 2) {
                    val rawChars = ByteArray(nameLen - 2) // exclude null terminator
                    System.arraycopy(dirData, offset, rawChars, 0, rawChars.size)
                    String(rawChars, Charsets.UTF_16LE)
                } else ""

                dirEntries.add(DirEntry(name, type, startSector, size))
            }

            // Root entry (index 0) contains the mini stream!
            val rootEntry = dirEntries.firstOrNull()
            val miniStreamData = if (rootEntry != null && rootEntry.startSector >= 0) {
                readChain(rootEntry.startSector, rootEntry.size)
            } else {
                ByteArray(0)
            }

            // Helper to read mini stream chain
            fun readMiniChain(startMiniSector: Int, size: Long): ByteArray {
                if (startMiniSector < 0 || size <= 0 || miniStreamData.isEmpty()) return ByteArray(0)
                val out = java.io.ByteArrayOutputStream(size.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                var curr = startMiniSector
                var bytesRemaining = size
                var safety = 0
                while (curr >= 0 && curr < miniFat.size && curr != END_OF_CHAIN && bytesRemaining > 0 && safety < 100000) {
                    val offset = curr * miniSectorSize
                    val toRead = minOf(bytesRemaining, miniSectorSize.toLong()).toInt()
                    if (offset + toRead <= miniStreamData.size) {
                        out.write(miniStreamData, offset, toRead)
                    } else if (offset < miniStreamData.size) {
                        out.write(miniStreamData, offset, miniStreamData.size - offset)
                    }
                    bytesRemaining -= toRead
                    curr = miniFat[curr]
                    safety++
                }
                return out.toByteArray()
            }

            // 5. Extract all user streams
            val streams = mutableMapOf<String, ByteArray>()
            for (entry in dirEntries) {
                if (entry.type == 2 && entry.name.isNotEmpty()) { // 2 = Stream object
                    val streamBytes = if (entry.size < miniStreamCutoff) {
                        readMiniChain(entry.startSector, entry.size)
                    } else {
                        readChain(entry.startSector, entry.size)
                    }
                    streams[entry.name] = streamBytes
                }
            }

            return Ole2Container(
                data = data,
                sectorSize = sectorSize,
                miniSectorSize = miniSectorSize,
                miniStreamCutoff = miniStreamCutoff,
                fat = fat,
                miniFat = miniFat,
                miniStreamData = miniStreamData,
                streams = streams
            )
        }
    }
}
