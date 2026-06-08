package com.example.adfeed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI 生成内容本地高速磁盘缓存实体
 */
@Entity(tableName = "ai_cache")
data class AICacheEntity(
    @PrimaryKey val adId: String,            // 广告ID
    val summary: String,                     // 缓存的摘要信息
    val introText: String,                   // 缓存的广告介绍大文本
    val timestamp: Long                      // 缓存写入时间戳
)
