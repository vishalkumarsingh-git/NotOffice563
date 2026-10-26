package com.programmingblitz.notoffice563.data.repository

import android.content.Context
import android.net.Uri
import com.programmingblitz.notoffice563.data.local.DocumentNoteDao
import com.programmingblitz.notoffice563.data.local.DocumentNoteEntity
import com.programmingblitz.notoffice563.data.local.RecentFileDao
import com.programmingblitz.notoffice563.data.local.RecentFileEntity
import com.programmingblitz.notoffice563.document.saf.SafHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DocumentRepository(
    private val recentFileDao: RecentFileDao,
    private val documentNoteDao: DocumentNoteDao
) {
    val allRecentFiles: Flow<List<RecentFileEntity>> = recentFileDao.getAllRecentFiles()
    val favoriteFiles: Flow<List<RecentFileEntity>> = recentFileDao.getFavoriteFiles()

    fun getRecentFilesLimited(limit: Int): Flow<List<RecentFileEntity>> =
        recentFileDao.getRecentFilesLimited(limit)

    fun searchFiles(query: String): Flow<List<RecentFileEntity>> =
        recentFileDao.searchFiles(query)

    suspend fun getFileByUri(uri: String): RecentFileEntity? = withContext(Dispatchers.IO) {
        recentFileDao.getByUri(uri)
    }

    /**
     * Called whenever a document is selected or opened.
     * Takes persistable URI permissions and records/updates metadata in Room.
     */
    suspend fun registerOpenedDocument(context: Context, uri: Uri): RecentFileEntity = withContext(Dispatchers.IO) {
        SafHelper.takePersistableUriPermission(context.contentResolver, uri)
        val metadata = SafHelper.queryMetadata(context, uri)

        val uriString = uri.toString()
        val existing = recentFileDao.getByUri(uriString)
        val entity = RecentFileEntity(
            uri = uriString,
            displayName = metadata.displayName,
            mimeType = metadata.mimeType,
            extension = metadata.extension,
            fileType = metadata.documentType.name,
            lastOpened = System.currentTimeMillis(),
            isFavorite = existing?.isFavorite ?: false,
            lastPosition = existing?.lastPosition,
            fileSize = if (metadata.fileSize > 0) metadata.fileSize else existing?.fileSize ?: 0L,
            lastModified = if (metadata.lastModified > 0) metadata.lastModified else existing?.lastModified ?: System.currentTimeMillis()
        )
        recentFileDao.upsert(entity)
        entity
    }

    suspend fun toggleFavorite(uri: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        recentFileDao.setFavorite(uri, !currentFavorite)
    }

    suspend fun updateLastPosition(uri: String, position: String) = withContext(Dispatchers.IO) {
        recentFileDao.updateLastPosition(uri, position)
    }

    suspend fun renameDocument(context: Context, uriString: String, newName: String): Result<String> = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        val newUri = SafHelper.renameDocument(context, uri, newName)
        if (newUri != null) {
            // Update in Room
            recentFileDao.updateDisplayName(uriString, newName)
            Result.success(newName)
        } else {
            // Try updating display name in our workspace metadata even if SAF backend is read-only
            recentFileDao.updateDisplayName(uriString, newName)
            Result.success(newName)
        }
    }

    suspend fun deleteDocument(context: Context, uriString: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        val deletedFromSaf = SafHelper.deleteDocument(context, uri)
        // In either case, remove from recent/favorites list
        recentFileDao.deleteByUri(uriString)
        documentNoteDao.deleteNotesForUri(uriString)
        Result.success(Unit)
    }

    suspend fun removeRecent(uri: String) = withContext(Dispatchers.IO) {
        recentFileDao.deleteByUri(uri)
    }

    suspend fun clearRecent() = withContext(Dispatchers.IO) {
        recentFileDao.clearAll()
    }

    // Notes
    fun getNotesForUri(uri: String): Flow<List<DocumentNoteEntity>> =
        documentNoteDao.getNotesForUri(uri)

    suspend fun saveNote(uri: String, targetKey: String, noteText: String) = withContext(Dispatchers.IO) {
        val existing = documentNoteDao.getNote(uri, targetKey)
        val note = DocumentNoteEntity(
            id = existing?.id ?: 0,
            uri = uri,
            targetKey = targetKey,
            noteText = noteText,
            updatedAt = System.currentTimeMillis()
        )
        documentNoteDao.upsertNote(note)
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        documentNoteDao.deleteNoteById(id)
    }
}
