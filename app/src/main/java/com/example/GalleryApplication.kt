package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.repository.MediaRepository

class GalleryApplication : Application() {
    
    lateinit var database: AppDatabase
        private set
        
    lateinit var mediaRepository: MediaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "gallery_database"
        ).build()
        
        mediaRepository = MediaRepository(this, database.mediaDao())
    }
}
