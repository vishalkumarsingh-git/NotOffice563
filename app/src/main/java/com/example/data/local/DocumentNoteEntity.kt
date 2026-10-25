package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for text annotations or notes associated with a document (e.g. PDF URI + page number,
 * or document-level comments) without modifying the original user file.
 */
@Entity(
    tableName = "document_notes",
    indices = [Index(value = ["uri", "targetKey"], unique = true)]
)
data class DocumentNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val targetKey: String, // e.g. "page:1", "general"
    val noteText: String,
    val updatedAt: Long = System.currentTimeMillis()
)
