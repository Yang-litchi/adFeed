package com.example.adfeed.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.adfeed.data.local.dao.AICacheDao
import com.example.adfeed.data.local.dao.InteractionDao
import com.example.adfeed.data.local.dao.StatisticEventDao
import com.example.adfeed.data.local.entity.AICacheEntity
import com.example.adfeed.data.local.entity.InteractionEntity
import com.example.adfeed.data.local.entity.StatisticEventEntity

@Database(
    entities = [InteractionEntity::class, AICacheEntity::class, StatisticEventEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun interactionDao(): InteractionDao
    abstract fun aiCacheDao(): AICacheDao
    abstract fun statisticEventDao(): StatisticEventDao
}
