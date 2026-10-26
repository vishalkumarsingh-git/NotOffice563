package com.programmingblitz.notoffice563.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentNoteDao {

    @Query("SELECT * FROM document_notes WHERE uri = :uri ORDER BY updatedAt DESC")
    fun getNotesForUri(uri: String): Flow<List<DocumentNoteEntity>>

    @Query("SELECT * FROM document_notes WHERE uri = :uri AND targetKey = :targetKey LIMIT 1")
    suspend fun getNote(uri: String, targetKey: String): DocumentNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: DocumentNoteEntity): Long

    @Query("DELETE FROM document_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM document_notes WHERE uri = :uri")
    suspend fun deleteNotesForUri(uri: String)
}
