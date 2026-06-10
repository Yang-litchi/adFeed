package com.example.adfeed.data.model

data class AdItem(
    val id: String,
    val type: AdType,
    val title: String,
    val description: String,
    val imageUrl: String,
    val videoUrl: String? = null,
    val channel: String,
    val likeCount: Int = 0,
    val collectCount: Int = 0,
    val isLiked: Boolean = false,
    val isCollected: Boolean = false,
    val exposureCount: Int = 0,
    val clickCount: Int = 0,
    val summary: String? = null,
    val tags: List<String> = emptyList(),
    //AI增强数据
    val aiInfo: AiInfo? = null
)

enum class AdType {
    LARGE_IMAGE,
    SMALL_IMAGE,
    VIDEO
}
