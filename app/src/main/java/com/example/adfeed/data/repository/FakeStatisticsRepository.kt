package com.example.adfeed.data.repository

import com.example.adfeed.data.model.EventType
import com.example.adfeed.data.model.StatisticEvent
import com.example.adfeed.data.model.StatisticSummary
import com.example.adfeed.data.model.TagStatistic
import com.example.adfeed.data.model.TrendData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 统计仓库的内存模拟实现（object 单例）
 *
 * 与 [MockData] 和 QwenApi 的模式一致。
 * 使用线程安全的数据结构确保 FeedViewModel（写入）和
 * StatisticsViewModel（读取）之间的数据一致性。
 *
 * 后续替换为 Room 实现时，无需修改任何业务层代码 —
 * 仅需新增一个 [StatisticsRepository] 的实现类即可。
 */
object FakeStatisticsRepository : StatisticsRepository {

    /** 所有统计事件的内存存储（线程安全） */
    private val events = CopyOnWriteArrayList<StatisticEvent>()

    /** 广告ID到标题的映射缓存，避免频繁查 MockData */
    private val adTitleCache = ConcurrentHashMap<String, String>()

    override suspend fun recordExposure(event: StatisticEvent) {
        events.add(event)
    }

    override suspend fun recordClick(event: StatisticEvent) {
        events.add(event)
    }

    override suspend fun getAdStatistics(adId: String): StatisticSummary {
        val adEvents = events.filter { it.adId == adId }
        val exposureCount = adEvents.count { it.eventType == EventType.EXPOSURE }
        val clickCount = adEvents.count { it.eventType == EventType.CLICK }
        return StatisticSummary(
            adId = adId,
            totalExposure = exposureCount,
            totalClick = clickCount
        )
    }

    override suspend fun getTagStatistics(tag: String): List<TagStatistic> {
        // 从所有事件中找出包含指定Tag的广告ID
        val tagAdIds = events
            .filter { tag in it.tags }
            .map { it.adId }
            .distinct()

        if (tagAdIds.isEmpty()) return emptyList()

        return tagAdIds.map { adId ->
            val adEvents = events.filter { it.adId == adId }
            val exposureCount = adEvents.count { it.eventType == EventType.EXPOSURE }
            val clickCount = adEvents.count { it.eventType == EventType.CLICK }
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
        val adEvents = events.filter { it.adId == adId }
        val totalExposure = adEvents.count { it.eventType == EventType.EXPOSURE }
        val totalClick = adEvents.count { it.eventType == EventType.CLICK }

        // 基于真实事件总量模拟7天趋势数据
        // 如果没有任何事件记录，返回全0数据
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // 计算过去7天的日期
        val dates = (6 downTo 0).map { daysAgo ->
            val cal = calendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            dateFormat.format(cal.time)
        }

        // 模拟每日分布：总事件按随机权重分配到7天
        val random = Random(adId.hashCode()) // 用 adId 作为种子保证同一广告趋势可复现
        val weights = (0 until 7).map { random.nextDouble(0.3, 1.7) }
        val totalWeight = weights.sum()

        return dates.mapIndexed { index, dateLabel ->
            val dayWeight = weights[index] / totalWeight
            val dayExposure = (totalExposure * dayWeight).roundToInt()
            val dayClick = (totalClick * dayWeight).roundToInt()
            TrendData(
                dateLabel = dateLabel,
                exposureCount = max(dayExposure, 0),
                clickCount = max(dayClick, 0)
            )
        }
    }

    /**
     * 获取广告标题，优先从缓存读取，缓存未命中时从 MockData 查询
     */
    private fun getAdTitle(adId: String): String {
        return adTitleCache.getOrPut(adId) {
            MockData.allAds.find { it.id == adId }?.title ?: "未知广告($adId)"
        }
    }

    /** 清空所有事件记录（仅用于测试） */
    fun reset() {
        events.clear()
        adTitleCache.clear()
    }
}
