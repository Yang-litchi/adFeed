package com.example.adfeed.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.adfeed.data.local.entity.StatisticEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * 统计事件的 Room 数据库访问接口 (DAO)
 */
@Dao
interface StatisticEventDao {

    /** 插入一条统计事件 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: StatisticEventEntity)

    /** 以 Flow 形式响应式观察所有统计事件 */
    @Query("SELECT * FROM statistic_events")
    fun getAllEventsFlow(): Flow<List<StatisticEventEntity>>

    /** 获取指定广告的所有统计事件 */
    @Query("SELECT * FROM statistic_events WHERE adId = :adId")
    suspend fun getEventsByAdId(adId: String): List<StatisticEventEntity>

    /** 获取指定广告的曝光次数 */
    @Query("SELECT COUNT(*) FROM statistic_events WHERE adId = :adId AND eventType = 'EXPOSURE'")
    suspend fun getExposureCount(adId: String): Int

    /** 获取指定广告的点击次数 */
    @Query("SELECT COUNT(*) FROM statistic_events WHERE adId = :adId AND eventType = 'CLICK'")
    suspend fun getClickCount(adId: String): Int

    /** 获取包含指定标签的广告ID列表（去重） */
    @Query("SELECT DISTINCT adId FROM statistic_events WHERE tags LIKE '%' || :tag || '%'")
    suspend fun getAdIdsByTag(tag: String): List<String>

    /** 获取指定广告的每日事件统计（用于趋势图） */
    @Query("""
        SELECT
            (timestamp / 86400000) as dayEpoch,
            COUNT(CASE WHEN eventType = 'EXPOSURE' THEN 1 END) as exposureCount,
            COUNT(CASE WHEN eventType = 'CLICK' THEN 1 END) as clickCount
        FROM statistic_events
        WHERE adId = :adId AND timestamp > :sinceTimestamp
        GROUP BY dayEpoch
        ORDER BY dayEpoch
    """)
    suspend fun getDailyEventCounts(adId: String, sinceTimestamp: Long): List<DailyEventCount>

    /** 每日事件计数结果 */
    data class DailyEventCount(
        val dayEpoch: Long,
        val exposureCount: Int,
        val clickCount: Int
    )
}
