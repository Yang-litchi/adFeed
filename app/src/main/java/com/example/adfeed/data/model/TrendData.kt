package com.example.adfeed.data.model

/**
 * 单日趋势数据点
 *
 * @param dateLabel 日期标签（如 "06/01"）
 * @param exposureCount 当日曝光量
 * @param clickCount 当日点击量
 */
data class TrendData(
    val dateLabel: String,
    val exposureCount: Int = 0,
    val clickCount: Int = 0
)
