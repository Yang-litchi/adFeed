package com.example.adfeed.data.model

/**
 * 单个广告的统计摘要
 *
 * @param adId 广告ID
 * @param totalExposure 总曝光量
 * @param totalClick 总点击量
 */
data class StatisticSummary(
    val adId: String,
    val totalExposure: Int = 0,
    val totalClick: Int = 0
) {
    /** 点击率（CTR），曝光量为0时返回0 */
    val ctr: Float
        get() = if (totalExposure > 0) totalClick.toFloat() / totalExposure else 0f
}
