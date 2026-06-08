package com.example.adfeed.data.repository

import com.example.adfeed.data.model.StatisticEvent
import com.example.adfeed.data.model.StatisticSummary
import com.example.adfeed.data.model.TagStatistic
import com.example.adfeed.data.model.TrendData

/**
 * 统计功能数据仓库接口
 *
 * UI层不得直接访问此接口实现类，所有数据访问通过ViewModel代理。
 * 当前使用 [FakeStatisticsRepository] 内存实现，
 * 后续替换为 Room 实现时仅需新增实现类，无需修改业务层代码。
 */
interface StatisticsRepository {

    /** 记录一条曝光事件 */
    suspend fun recordExposure(event: StatisticEvent)

    /** 记录一条点击事件 */
    suspend fun recordClick(event: StatisticEvent)

    /** 获取指定广告的统计摘要（总曝光量、总点击量、CTR） */
    suspend fun getAdStatistics(adId: String): StatisticSummary

    /** 获取与指定Tag相关的所有广告的统计数据，用于横向对比 */
    suspend fun getTagStatistics(tag: String): List<TagStatistic>

    /** 获取指定广告最近7天的趋势数据 */
    suspend fun getTrendStatistics(adId: String): List<TrendData>
}
