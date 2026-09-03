package com.example.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.data.MediaDao
import com.example.data.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MediaRepository(
    private val context: Context,
    private val mediaDao: MediaDao
) {
    fun getAllMedia() = mediaDao.getAllMedia()
    
    fun getAllMediaFlow() = mediaDao.getAllMediaFlow()

    suspend fun syncMedia() = withContext(Dispatchers.IO) {
        val existingIds = mediaDao.getAllMediaIds().toSet()
        val currentMedia = scanMediaStore()
        
        val currentIds = currentMedia.map { it.id }.toSet()
        
        val newMedia = currentMedia.filter { !existingIds.contains(it.id) }
        val deletedIds = existingIds.filter { !currentIds.contains(it) }
        
        if (newMedia.isNotEmpty()) {
            mediaDao.insertMedia(newMedia)
        }
        
        if (deletedIds.isNotEmpty()) {
            mediaDao.deleteMediaByIds(deletedIds)
        }
        Log.d("MediaRepository", "Sync complete. Added: ${newMedia.size}, Deleted: ${deletedIds.size}")
    }

    private fun scanMediaStore(): List<MediaItem> {
        val mediaItems = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.DURATION,
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        )

        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        val query = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val mimeType = cursor.getString(mimeTypeColumn) ?: ""
                
                val contentUri: Uri = if (mimeType.startsWith("image/")) {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                
                val uri = ContentUris.withAppendedId(contentUri, id)

                mediaItems.add(
                    MediaItem(
                        id = id,
                        uri = uri.toString(),
                        name = cursor.getString(nameColumn) ?: "Unknown",
                        mimeType = mimeType,
                        size = cursor.getLong(sizeColumn),
                        dateAdded = cursor.getLong(dateAddedColumn) * 1000, // Convert to ms
                        dateModified = cursor.getLong(dateModifiedColumn) * 1000,
                        width = cursor.getInt(widthColumn),
                        height = cursor.getInt(heightColumn),
                        duration = cursor.getLong(durationColumn),
                        bucketId = cursor.getString(bucketIdColumn) ?: "",
                        bucketDisplayName = cursor.getString(bucketNameColumn) ?: "Unknown"
                    )
                )
            }
        }
        return mediaItems
    }
}
