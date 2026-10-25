package com.example.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFileDao {

    @Query("SELECT * FROM recent_files ORDER BY lastOpened DESC")
    fun getAllRecentFiles(): Flow<List<RecentFileEntity>>

    @Query("SELECT * FROM recent_files ORDER BY lastOpened DESC LIMIT :limit")
    fun getRecentFilesLimited(limit: Int): Flow<List<RecentFileEntity>>

    @Query("SELECT * FROM recent_files WHERE isFavorite = 1 ORDER BY lastOpened DESC")
    fun getFavoriteFiles(): Flow<List<RecentFileEntity>>

    @Query("SELECT * FROM recent_files WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): RecentFileEntity?

    @Upsert
    suspend fun upsert(entity: RecentFileEntity)

    @Query("UPDATE recent_files SET isFavorite = :isFavorite WHERE uri = :uri")
    suspend fun setFavorite(uri: String, isFavorite: Boolean)

    @Query("UPDATE recent_files SET lastPosition = :position WHERE uri = :uri")
    suspend fun updateLastPosition(uri: String, position: String)

    @Query("UPDATE recent_files SET displayName = :newName WHERE uri = :uri")
    suspend fun updateDisplayName(uri: String, newName: String)

    @Query("DELETE FROM recent_files WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)

    @Query("DELETE FROM recent_files")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM recent_files 
        WHERE displayName LIKE '%' || :query || '%' 
           OR extension LIKE '%' || :query || '%' 
           OR fileType LIKE '%' || :query || '%'
        ORDER BY lastOpened DESC
    """)
    fun searchFiles(query: String): Flow<List<RecentFileEntity>>
}
