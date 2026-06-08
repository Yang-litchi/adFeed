package com.example.adfeed.data.model

/** 统计事件类型 */
enum class EventType {
    /** 曝光事件 — 广告进入用户可视区域 */
    EXPOSURE,
    /** 点击事件 — 用户点击广告 */
    CLICK
}

/**
 * 统计事件记录
 *
 * @param adId 广告ID
 * @param tags 广告标签列表
 * @param eventType 事件类型
 * @param timestamp 事件发生时间戳（毫秒）
 */
data class StatisticEvent(
    val adId: String,
    val tags: List<String>,
    val eventType: EventType,
    val timestamp: Long = System.currentTimeMillis()
)
