package com.example.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items WHERE isTrashed = 0 ORDER BY dateAdded DESC")
    fun getAllMedia(): PagingSource<Int, MediaItem>

    @Query("SELECT * FROM media_items WHERE isTrashed = 0 ORDER BY dateAdded DESC")
    fun getAllMediaFlow(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isTrashed = 0 AND bucketId = :bucketId ORDER BY dateAdded DESC")
    fun getMediaByBucket(bucketId: String): PagingSource<Int, MediaItem>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 AND isTrashed = 0 ORDER BY dateAdded DESC")
    fun getFavorites(): PagingSource<Int, MediaItem>

    @Query("SELECT * FROM media_items WHERE isTrashed = 1 ORDER BY dateAdded DESC")
    fun getTrashedMedia(): PagingSource<Int, MediaItem>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaById(id: Long): MediaItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(items: List<MediaItem>)

    @Update
    suspend fun updateMedia(item: MediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("DELETE FROM media_items WHERE id IN (:ids)")
    suspend fun deleteMediaByIds(ids: List<Long>)

    @Query("SELECT id FROM media_items")
    suspend fun getAllMediaIds(): List<Long>
    
    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getMediaCount(): Int
}
