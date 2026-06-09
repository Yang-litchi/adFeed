package com.example.adfeed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 统计事件持久化实体（曝光/点击）
 *
 * 每条曝光或点击记录为一行，与 [StatisticEvent] 模型一一对应。
 * 以自增 ID 作为主键，避免广告 ID 冲突导致记录丢失。
 */
@Entity(tableName = "statistic_events")
data class StatisticEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val adId: String,                       // 广告ID
    val tags: String,                       // 标签列表，以逗号分隔存储
    val eventType: String,                  // 事件类型："EXPOSURE" 或 "CLICK"
    val timestamp: Long                     // 事件发生时间戳（毫秒）
)
