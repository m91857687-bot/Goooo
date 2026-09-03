package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey
    val id: Long,
    val uri: String,
    val name: String,
    val mimeType: String,
    val size: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val width: Int,
    val height: Int,
    val duration: Long = 0,
    val bucketId: String,
    val bucketDisplayName: String,
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false
) {
    val isVideo: Boolean get() = mimeType.startsWith("video/")
}
