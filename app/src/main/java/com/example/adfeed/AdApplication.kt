package com.example.adfeed

import android.app.Application
import androidx.room.Room
import com.example.adfeed.data.local.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        // 开发阶段：清除所有 Mock 遗留的 AI 缓存，确保所有广告走真实 Qwen API
        // 正式发布时可移除此调用，利用缓存减少 API 请求
//        CoroutineScope(Dispatchers.IO).launch {
//            database.aiCacheDao().deleteAllCache()
//        }
    }
}
