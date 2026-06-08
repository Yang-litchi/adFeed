package com.example.adfeed

import android.app.Application
import androidx.room.Room
import com.example.adfeed.data.local.db.AppDatabase

class AdApplication : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "adfeed.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
}
