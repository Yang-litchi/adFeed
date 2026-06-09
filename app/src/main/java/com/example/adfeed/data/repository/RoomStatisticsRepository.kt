package com.example.adfeed.data.repository

import com.example.adfeed.AdApplication
import com.example.adfeed.data.local.entity.StatisticEventEntity
import com.example.adfeed.data.model.EventType
import com.example.adfeed.data.model.StatisticEvent
import com.example.adfeed.data.model.StatisticSummary
import com.example.adfeed.data.model.TagStatistic
import com.example.adfeed.data.model.TrendData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 基于 Room 数据库的统计仓库实现
 *
 * 替代 [FakeStatisticsRepository] 的内存存储，
 * 所有统计数据持久化到 Room 数据库，App 重启后数据不丢失。
 */
object RoomStatisticsRepository : StatisticsRepository {

    private val dao get() = AdApplication.database.statisticEventDao()

    override suspend fun recordExposure(event: StatisticEvent) {
        dao.insertEvent(
            StatisticEventEntity(
                adId = event.adId,
                tags = event.tags.joinToString(","),
                eventType = event.eventType.name,
                timestamp = event.timestamp
            )
        )
    }

    override suspend fun recordClick(event: StatisticEvent) {
        dao.insertEvent(
            StatisticEventEntity(
                adId = event.adId,
                tags = event.tags.joinToString(","),
                eventType = event.eventType.name,
                timestamp = event.timestamp
            )
        )
    }

    override suspend fun getAdStatistics(adId: String): StatisticSummary {
        val exposureCount = dao.getExposureCount(adId)
        val clickCount = dao.getClickCount(adId)
        return StatisticSummary(
            adId = adId,
            totalExposure = exposureCount,
            totalClick = clickCount
        )
    }

    override suspend fun getTagStatistics(tag: String): List<TagStatistic> {
        val adIds = dao.getAdIdsByTag(tag)
        if (adIds.isEmpty()) return emptyList()

        return adIds.map { adId ->
            val exposureCount = dao.getExposureCount(adId)
            val clickCount = dao.getClickCount(adId)
            val title = getAdTitle(adId)
            TagStatistic(
                adId = adId,
                title = title,
                exposureCount = exposureCount,
                clickCount = clickCount
            )
        }.sortedByDescending { it.exposureCount }
    }

    override suspend fun getTrendStatistics(adId: String): List<TrendData> {
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // 计算7天前的时间戳（每天按 86400000 毫秒）
        val sevenDaysAgo = calendar.timeInMillis - 7 * 86400000L

        // 从 Room 读取每日事件计数
        val dailyCounts = dao.getDailyEventCounts(adId, sevenDaysAgo)

        // 构建日期标签映射：dayEpoch → DailyEventCount
        val countMap = dailyCounts.associateBy { it.dayEpoch }

        // 生成过去7天的完整列表（即使没有数据的日期也显示为0）
        val dates = (6 downTo 0).map { daysAgo ->
            val cal = calendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            // 获取当天0点的时间戳（按天对齐）
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayEpoch = cal.timeInMillis / 86400000L
            dateFormat.format(cal.time) to dayEpoch
        }

        return dates.map { (dateLabel, dayEpoch) ->
            val dayData = countMap[dayEpoch]
            TrendData(
                dateLabel = dateLabel,
                exposureCount = dayData?.exposureCount ?: 0,
                clickCount = dayData?.clickCount ?: 0
            )
        }
    }

    /**
     * 获取广告标题，优先从缓存读取，缓存未命中时从 MockData 查询
     */
    private var adTitleCache = mutableMapOf<String, String>()

    private fun getAdTitle(adId: String): String {
        return adTitleCache.getOrPut(adId) {
            MockData.allAds.find { it.id == adId }?.title ?: "未知广告($adId)"
        }
    }
}
