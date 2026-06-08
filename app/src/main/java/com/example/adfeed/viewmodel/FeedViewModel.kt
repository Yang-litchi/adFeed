package com.example.adfeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.model.EventType
import com.example.adfeed.data.model.StatisticEvent
import com.example.adfeed.data.repository.FakeStatisticsRepository
import com.example.adfeed.data.repository.MockData
import com.example.adfeed.data.repository.StatisticsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val ads: List<AdItem> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

class FeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    // 统计数据仓库（接口抽象，当前内存实现，后续可替换Room）
    private val statisticsRepository: StatisticsRepository = FakeStatisticsRepository

    // 本地点赞/收藏状态持久化，key=adId
    private val localStates = HashMap<String, LocalAdState>()

    private data class LocalAdState(
        val isLiked: Boolean,
        val likeCount: Int,
        val isCollected: Boolean
    )

    private var currentChannel = "精选"
    private var currentPage = 0
    private val pageSize = 6
    // 当前频道的随机顺序池
    private var shuffledPool: List<AdItem> = emptyList()

    init {
        shuffledPool = buildPool(currentChannel)
        loadAds()
    }

    private fun buildPool(channel: String): List<AdItem> {
        return MockData.getByChannel(channel).shuffled()
    }

    private fun mergeLocalState(ad: AdItem): AdItem {
        val state = localStates[ad.id] ?: return ad
        return ad.copy(
            isLiked = state.isLiked,
            likeCount = state.likeCount,
            isCollected = state.isCollected
        )
    }

    fun loadAds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(600)
            val start = currentPage * pageSize
            val end = minOf(start + pageSize, shuffledPool.size)
            if (start >= shuffledPool.size) {
                _uiState.update { it.copy(isLoading = false, hasMore = false) }
                return@launch
            }
            val newAds = shuffledPool.subList(start, end).map { mergeLocalState(it) }
            _uiState.update { state ->
                state.copy(
                    ads = if (currentPage == 0) newAds else state.ads + newAds,
                    isLoading = false,
                    hasMore = end < shuffledPool.size
                )
            }
            currentPage++
        }
    }

    fun refresh() {
        // 刷新时重新shuffle，呈现不同顺序
        shuffledPool = buildPool(currentChannel)
        currentPage = 0
        loadAds()
    }

    fun loadMore() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return
        loadAds()
    }

    fun switchChannel(channel: String) {
        if (currentChannel == channel) return
        currentChannel = channel
        shuffledPool = buildPool(channel)
        currentPage = 0
        loadAds()
    }

    fun toggleLike(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) {
                    val newLiked = !ad.isLiked
                    val newCount = if (newLiked) ad.likeCount + 1 else ad.likeCount - 1
                    localStates[adId] = LocalAdState(newLiked, newCount, ad.isCollected)
                    ad.copy(isLiked = newLiked, likeCount = newCount)
                } else ad
            })
        }
    }

    fun toggleCollect(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) {
                    val newCollected = !ad.isCollected
                    localStates[adId] = LocalAdState(ad.isLiked, ad.likeCount, newCollected)
                    ad.copy(isCollected = newCollected)
                } else ad
            })
        }
    }

    fun recordClick(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) ad.copy(clickCount = ad.clickCount + 1) else ad
            })
        }
        // 同时写入统计仓库
        recordStatEvent(adId, EventType.CLICK)
    }

    fun recordExposure(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) ad.copy(exposureCount = ad.exposureCount + 1) else ad
            })
        }
        // 同时写入统计仓库
        recordStatEvent(adId, EventType.EXPOSURE)
    }

    /** 向统计仓库写入事件（异步，不阻塞UI） */
    private fun recordStatEvent(adId: String, eventType: EventType) {
        viewModelScope.launch {
            val ad = _uiState.value.ads.find { it.id == adId } ?: return@launch
            val event = StatisticEvent(
                adId = adId,
                tags = ad.tags,
                eventType = eventType
            )
            when (eventType) {
                EventType.EXPOSURE -> statisticsRepository.recordExposure(event)
                EventType.CLICK -> statisticsRepository.recordClick(event)
            }
        }
    }
}