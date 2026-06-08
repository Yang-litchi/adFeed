package com.example.adfeed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户点赞/收藏交互持久化实体
 */
@Entity(tableName = "interactions")
data class InteractionEntity(
    @PrimaryKey val adId: String,            // 广告ID
    val isLiked: Boolean,                    // 是否已点赞
    val isCollected: Boolean,                // 是否已收藏
    val likeCountOffset: Int,                // 用户操作引起的点赞偏移量 (例如 +1, -1)
    val collectCountOffset: Int,             // 用户操作引起的收藏偏移量
    val shareCount: Int = 0                  // 分享统计数
)
