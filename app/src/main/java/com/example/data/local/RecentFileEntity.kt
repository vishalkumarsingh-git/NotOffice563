package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing metadata for a recently opened document in NotOffice563.
 *
 * NOTE: As required by modern Android architecture and privacy principles,
 * the actual document payload is NEVER stored in the database. Only persistable
 * SAF URI metadata, display attributes, favorite state, and reading progress are stored.
 */
@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val extension: String,
    val fileType: String,
    val lastOpened: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val lastPosition: String? = null,
    val fileSize: Long = 0L,
    val lastModified: Long = 0L
)
