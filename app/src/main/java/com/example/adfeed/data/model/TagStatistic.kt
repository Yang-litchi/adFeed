package com.example.adfeed.data.model

/**
 * 同Tag广告对比统计数据
 *
 * @param adId 广告ID
 * @param title 广告标题
 * @param exposureCount 曝光量
 * @param clickCount 点击量
 */
data class TagStatistic(
    val adId: String,
    val title: String,
    val exposureCount: Int = 0,
    val clickCount: Int = 0
) {
    /** 点击率（CTR） */
    val ctr: Float
        get() = if (exposureCount > 0) clickCount.toFloat() / exposureCount else 0f
}
