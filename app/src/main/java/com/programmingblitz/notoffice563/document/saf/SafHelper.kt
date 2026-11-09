package com.programmingblitz.notoffice563.document.saf

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.programmingblitz.notoffice563.domain.model.DocumentType
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

data class DocumentMetadata(
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val extension: String,
    val documentType: DocumentType,
    val fileSize: Long,
    val lastModified: Long,
    val hasPermission: Boolean,
    val isWritable: Boolean = false
)

/**
 * Central document access and Storage Access Framework (SAF) manager for NotOffice563.
 * Handles content:// and file:// streams, persistable URI permissions, metadata queries,
 * and reliable read/write operations without assuming local filesystem paths.
 */
object SafHelper {

    /**
     * Persists URI read/write permissions granted by Android's document picker or caller apps.
     * Respects granted flags and avoids throwing SecurityException when persistable permission
     * is not granted by the caller.
     */
    fun takePersistableUriPermission(contentResolver: ContentResolver, uri: Uri, intentFlags: Int? = null) {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            try {
                if (intentFlags != null) {
                    val isPersistable = (intentFlags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) != 0
                    if (isPersistable) {
                        val takeFlags = intentFlags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        if (takeFlags != 0) {
                            contentResolver.takePersistableUriPermission(uri, takeFlags)
                            return
                        }
                    }
                }

                // If intentFlags was not passed or provider supports taking persistable permission:
                val fullFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                try {
                    contentResolver.takePersistableUriPermission(uri, fullFlags)
                } catch (_: SecurityException) {
                    try {
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (_: Exception) {
                        // Caller granted temporary read-only access (e.g. standard file manager ACTION_VIEW)
                    }
                }
            } catch (_: Exception) {
                // Graceful fallback for non-persistable providers
            }
        }
    }

    /**
     * Opens a reliable InputStream from any ContentResolver URI or file URI.
     */
    fun openInputStream(context: Context, uri: Uri): InputStream? {
        return try {
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null) {
                    val file = File(path)
                    if (file.exists() && file.canRead()) {
                        return FileInputStream(file)
                    }
                }
            }
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Opens a reliable OutputStream for saving modifications back to the original URI.
     * Tries "rwt" (read-write-truncate) first, then falls back to "wt" (write-truncate).
     */
    fun openOutputStream(context: Context, uri: Uri, mode: String = "rwt"): OutputStream? {
        return try {
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null) {
                    val file = File(path)
                    return FileOutputStream(file)
                }
            }
            val contentResolver = context.contentResolver
            contentResolver.openOutputStream(uri, mode)
                ?: contentResolver.openOutputStream(uri, "wt")
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Checks if the app currently holds permission to read this URI.
     */
    fun canAccessUri(context: Context, uri: Uri): Boolean {
        return try {
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null) {
                    val file = File(path)
                    if (file.exists() && file.canRead()) return true
                }
            }
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks if the app currently holds write permission to this URI.
     */
    fun isUriWritable(context: Context, uri: Uri): Boolean {
        return try {
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null) {
                    val file = File(path)
                    if (file.exists() && file.canWrite()) return true
                }
            }
            // Check DocumentContract flags if available
            val projection = arrayOf(DocumentsContract.Document.COLUMN_FLAGS)
            var hasWriteFlag = false
            try {
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val flagIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS)
                        if (flagIdx != -1) {
                            val flags = cursor.getInt(flagIdx)
                            hasWriteFlag = (flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0
                        }
                    }
                }
            } catch (_: Exception) {
            }

            hasWriteFlag || canAccessUri(context, uri)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Queries display name, size, mimeType, and last modified date from ContentResolver.
     */
    fun queryMetadata(context: Context, uri: Uri): DocumentMetadata {
        var displayName: String? = null
        var fileSize = 0L
        var lastModified = 0L
        val contentResolver = context.contentResolver

        var mimeType: String? = runCatching { contentResolver.getType(uri) }.getOrNull()

        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val projection = arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
            )
            runCatching {
                contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            displayName = cursor.getString(nameIndex)
                        }
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIndex != -1) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }
            }

            // Attempt to query DocumentsContract columns if applicable
            runCatching {
                val docProjection = arrayOf(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                contentResolver.query(uri, docProjection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val modIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                        if (modIndex != -1) {
                            lastModified = cursor.getLong(modIndex)
                        }
                    }
                }
            }
        } else if (uri.scheme == "file") {
            runCatching {
                val f = uri.path?.let { File(it) }
                if (f != null && f.exists()) {
                    displayName = f.name
                    fileSize = f.length()
                    lastModified = f.lastModified()
                }
            }
        }

        val resolvedName = displayName ?: uri.lastPathSegment ?: "Unknown Document"
        val ext = resolvedName.substringAfterLast('.', "").lowercase()
        val docType = DocumentType.fromMimeOrFilename(mimeType, resolvedName)

        if (mimeType == null || mimeType == "application/octet-stream" || mimeType == "*/*") {
            mimeType = when (docType) {
                DocumentType.PDF -> "application/pdf"
                DocumentType.WORD_DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                DocumentType.WORD_DOC -> "application/msword"
                DocumentType.EXCEL_XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                DocumentType.EXCEL_XLS -> "application/vnd.ms-excel"
                DocumentType.PPT_PPTX -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                DocumentType.PPT_PPT -> "application/vnd.ms-powerpoint"
                DocumentType.RTF -> "application/rtf"
                DocumentType.CSV -> "text/csv"
                DocumentType.TSV -> "text/tab-separated-values"
                DocumentType.TXT -> "text/plain"
                DocumentType.UNKNOWN -> "application/octet-stream"
            }
        }

        val accessible = canAccessUri(context, uri)
        val writable = isUriWritable(context, uri)

        return DocumentMetadata(
            uri = uri,
            displayName = resolvedName,
            mimeType = mimeType,
            extension = ext,
            documentType = docType,
            fileSize = fileSize,
            lastModified = if (lastModified > 0) lastModified else System.currentTimeMillis(),
            hasPermission = accessible,
            isWritable = writable
        )
    }

    /**
     * Copies a temporary document stream to app-private cache/documents storage so it can be
     * safely re-opened later if the caller application's temporary grant expires.
     */
    fun copyDocumentToAppStorage(context: Context, sourceUri: Uri, preferredName: String): Uri? {
        return try {
            val docsDir = File(context.filesDir, "imported_docs").apply { mkdirs() }
            val sanitizedName = preferredName.replace("[^a-zA-Z0-9._ -]".toRegex(), "_")
            val targetFile = File(docsDir, sanitizedName)

            val inputStream = openInputStream(context, sourceUri) ?: return null
            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Attempts to rename document using DocumentsContract.
     * Returns the new Uri if successful, or null if unsupported.
     */
    fun renameDocument(context: Context, uri: Uri, newDisplayName: String): Uri? {
        return try {
            DocumentsContract.renameDocument(context.contentResolver, uri, newDisplayName)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Attempts to delete document using DocumentsContract.
     */
    fun deleteDocument(context: Context, uri: Uri): Boolean {
        return try {
            DocumentsContract.deleteDocument(context.contentResolver, uri)
        } catch (_: Exception) {
            false
        }
    }

    private fun getShareableUri(context: Context, uri: Uri): Uri {
        if (uri.scheme == "file") {
            val f = uri.path?.let { File(it) }
            if (f != null && f.exists()) {
                return try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        f
                    )
                } catch (_: Exception) {
                    uri
                }
            }
        }
        return uri
    }

    /**
     * Launches external app with ACTION_VIEW.
     */
    fun openWithAnotherApp(context: Context, uri: Uri, mimeType: String): Boolean {
        return try {
            val safeUri = getShareableUri(context, uri)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(safeUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Standard Android share sheet.
     */
    fun shareDocument(context: Context, uri: Uri, mimeType: String, title: String): Boolean {
        return try {
            val safeUri = getShareableUri(context, uri)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, safeUri)
                putExtra(Intent.EXTRA_TITLE, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Document").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        } catch (_: Exception) {
            false
        }
    }
}
