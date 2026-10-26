package com.programmingblitz.notoffice563.document.saf

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.programmingblitz.notoffice563.domain.model.DocumentType
import com.programmingblitz.notoffice563.util.FileUtils
import java.io.File

data class DocumentMetadata(
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val extension: String,
    val documentType: DocumentType,
    val fileSize: Long,
    val lastModified: Long,
    val hasPermission: Boolean
)

object SafHelper {

    /**
     * Persists URI read/write permissions granted by the system Document Picker.
     * This allows NotOffice563 to reopen the document on subsequent app launches
     * without repeatedly prompting the user.
     */
    fun takePersistableUriPermission(contentResolver: ContentResolver, uri: Uri) {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: SecurityException) {
                // Some providers only grant read permission
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Ignored if provider does not support persistable permissions
                }
            } catch (_: Exception) {
                // Graceful fallback for non-persistable providers
            }
        }
    }

    /**
     * Checks if the app currently holds persisted URI permission or can read from this URI.
     */
    fun canAccessUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
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

        var mimeType: String? = contentResolver.getType(uri)

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

        if (mimeType == null || mimeType == "application/octet-stream") {
            mimeType = when (docType) {
                DocumentType.PDF -> "application/pdf"
                DocumentType.WORD_DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                DocumentType.WORD_DOC -> "application/msword"
                DocumentType.EXCEL_XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                DocumentType.EXCEL_XLS -> "application/vnd.ms-excel"
                DocumentType.PPT_PPTX -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                DocumentType.PPT_PPT -> "application/vnd.ms-powerpoint"
                DocumentType.UNKNOWN -> "application/octet-stream"
            }
        }

        val accessible = canAccessUri(context, uri)

        return DocumentMetadata(
            uri = uri,
            displayName = resolvedName,
            mimeType = mimeType,
            extension = ext,
            documentType = docType,
            fileSize = fileSize,
            lastModified = if (lastModified > 0) lastModified else System.currentTimeMillis(),
            hasPermission = accessible
        )
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
