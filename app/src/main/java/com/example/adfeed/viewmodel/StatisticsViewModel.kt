package com.example.adfeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adfeed.data.model.StatisticSummary
import com.example.adfeed.data.model.TagStatistic
import com.example.adfeed.data.model.TrendData
import com.example.adfeed.data.repository.MockData
import com.example.adfeed.data.repository.RoomStatisticsRepository
import com.example.adfeed.data.repository.StatisticsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 统计页面的 UI 状态
 */
data class StatisticsUiState(
    val adSummary: StatisticSummary? = null,
    val tagStatistics: List<TagStatistic> = emptyList(),
    val trendData: List<TrendData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 统计页面 ViewModel
 *
 * UI层通过此ViewModel获取统计数据，不得直接访问Repository实现类。
 *
 * @param repository 统计数据仓库（默认使用 Room 持久化实现）
 */
class StatisticsViewModel(
    private val repository: StatisticsRepository = RoomStatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    /**
     * 加载指定广告的全量统计数据
     *
     * 三个数据源并行加载：广告统计摘要、同Tag对比数据、7天趋势数据
     */
    fun loadStatistics(adId: String) {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState(isLoading = true)

            try {
                // 并行加载三份数据
                val summaryDeferred = async { repository.getAdStatistics(adId) }
                val tagStatsDeferred = async {
                    // 获取广告的Tag列表，取第一个有同名广告的Tag做对比
                    val ad = MockData.allAds.find { it.id == adId }
                    val tag = ad?.tags?.firstOrNull()
                    if (tag != null) repository.getTagStatistics(tag) else emptyList()
                }
                val trendDeferred = async { repository.getTrendStatistics(adId) }

                val summary = summaryDeferred.await()
                val tagStats = tagStatsDeferred.await()
                val trend = trendDeferred.await()

                _uiState.value = StatisticsUiState(
                    adSummary = summary,
                    tagStatistics = tagStats,
                    trendData = trend,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState(
                    error = "统计数据加载失败: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /** 重新加载（用于错误重试） */
    fun retry(adId: String) {
        loadStatistics(adId)
    }
}
