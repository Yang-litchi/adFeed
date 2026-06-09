package com.example.adfeed.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.adfeed.data.local.entity.AICacheEntity

/**
 * AI 生成结果缓存的 Room 数据库访问接口 (DAO)
 */
@Dao
interface AICacheDao {

    /**
     * 根据广告 ID 获取缓存的 AI 分析结果
     */
    @Query("SELECT * FROM ai_cache WHERE adId = :adId")
    suspend fun getAICache(adId: String): AICacheEntity?

    /**
     * 插入 AI 缓存结果，若已存在则直接覆盖
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: AICacheEntity)

    /**
     * 根据广告 ID 删除对应的 AI 缓存结果
     */
    @Query("DELETE FROM ai_cache WHERE adId = :adId")
    suspend fun deleteCache(adId: String)

    /**
     * 清空所有 AI 缓存数据（用于清除 Mock 遗留数据或重置缓存）
     */
    @Query("DELETE FROM ai_cache")
    suspend fun deleteAllCache()
}
