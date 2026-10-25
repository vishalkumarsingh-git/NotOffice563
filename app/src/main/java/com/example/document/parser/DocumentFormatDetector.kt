package com.example.document.parser

import android.content.Context
import android.net.Uri
import com.example.domain.model.DocumentType
import java.io.BufferedInputStream
import java.util.zip.ZipInputStream

sealed class DocumentFormatValidation {
    data class Valid(val type: DocumentType) : DocumentFormatValidation()
    data class LegacyFormat(val type: DocumentType, val message: String) : DocumentFormatValidation()
    data class Corrupted(val message: String) : DocumentFormatValidation()
    data class Unsupported(val detectedMime: String?, val extension: String?) : DocumentFormatValidation()
}

object DocumentFormatDetector {

    /**
     * Inspects magic numbers and content streams to determine the true document format.
     */
    fun detectAndValidate(context: Context, uri: Uri, fallbackType: DocumentType): DocumentFormatValidation {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return DocumentFormatValidation.Corrupted("Cannot open file stream. Permission may be revoked or file was moved.")

            val buffered = BufferedInputStream(inputStream)
            buffered.mark(1024)
            val header = ByteArray(8)
            val bytesRead = buffered.read(header)
            buffered.reset()

            if (bytesRead < 4) {
                buffered.close()
                return DocumentFormatValidation.Corrupted("File appears to be empty or corrupted.")
            }

            // 1. PDF: %PDF- (0x25 0x50 0x44 0x46)
            if (header[0] == 0x25.toByte() && header[1] == 0x50.toByte() &&
                header[2] == 0x44.toByte() && header[3] == 0x46.toByte()
            ) {
                buffered.close()
                return DocumentFormatValidation.Valid(DocumentType.PDF)
            }

            // 2. OpenXML ZIP: PK\x03\x04 (0x50 0x4B 0x03 0x04)
            if (header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() &&
                header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
            ) {
                // Inspect ZIP entries to find specific Office XML
                var detectedType: DocumentType? = null
                try {
                    ZipInputStream(buffered).use { zip ->
                        var entry = zip.nextEntry
                        var entriesScanned = 0
                        while (entry != null && entriesScanned < 30) {
                            val name = entry.name
                            if (name.startsWith("word/")) {
                                detectedType = DocumentType.WORD_DOCX
                                break
                            } else if (name.startsWith("xl/")) {
                                detectedType = DocumentType.EXCEL_XLSX
                                break
                            } else if (name.startsWith("ppt/")) {
                                detectedType = DocumentType.PPT_PPTX
                                break
                            }
                            zip.closeEntry()
                            entry = zip.nextEntry
                            entriesScanned++
                        }
                    }
                } catch (_: Exception) {
                    // Ignored, use fallback
                }

                if (detectedType != null) {
                    return DocumentFormatValidation.Valid(detectedType)
                }
                // If it's a valid ZIP and fallback is an OpenXML type, trust fallback
                if (fallbackType in listOf(DocumentType.WORD_DOCX, DocumentType.EXCEL_XLSX, DocumentType.PPT_PPTX)) {
                    return DocumentFormatValidation.Valid(fallbackType)
                }
            }

            // 3. Legacy Microsoft Compound Document (OLE2): 0xD0 0xCF 0x11 0xE0 0xA1 0xB1 0x1A 0xE1
            if (header[0] == 0xD0.toByte() && header[1] == 0xCF.toByte() &&
                header[2] == 0x11.toByte() && header[3] == 0xE0.toByte()
            ) {
                buffered.close()
                return when (fallbackType) {
                    DocumentType.WORD_DOC -> DocumentFormatValidation.LegacyFormat(
                        DocumentType.WORD_DOC,
                        "This legacy Word format cannot currently be fully rendered by NotOffice563."
                    )
                    DocumentType.EXCEL_XLS -> DocumentFormatValidation.LegacyFormat(
                        DocumentType.EXCEL_XLS,
                        "This legacy Excel format cannot currently be fully rendered by NotOffice563."
                    )
                    DocumentType.PPT_PPT -> DocumentFormatValidation.LegacyFormat(
                        DocumentType.PPT_PPT,
                        "This legacy PowerPoint format cannot currently be fully rendered by NotOffice563."
                    )
                    else -> DocumentFormatValidation.LegacyFormat(
                        fallbackType,
                        "This legacy Office format is supported via fallback viewer."
                    )
                }
            }

            buffered.close()

            // If none of the magic numbers matched, check fallback
            return if (fallbackType != DocumentType.UNKNOWN) {
                if (fallbackType.isLegacy) {
                    DocumentFormatValidation.LegacyFormat(
                        fallbackType,
                        "This legacy format cannot currently be fully rendered by NotOffice563."
                    )
                } else {
                    DocumentFormatValidation.Valid(fallbackType)
                }
            } else {
                DocumentFormatValidation.Unsupported(null, null)
            }
        } catch (e: Exception) {
            return DocumentFormatValidation.Corrupted(e.localizedMessage ?: "Failed to read file.")
        }
    }
}
