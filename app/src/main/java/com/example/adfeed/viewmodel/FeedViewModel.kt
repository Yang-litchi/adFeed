package com.example.adfeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adfeed.AdApplication
import com.example.adfeed.data.local.entity.InteractionEntity
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.model.EventType
import com.example.adfeed.data.model.StatisticEvent
import com.example.adfeed.data.repository.MockData
import com.example.adfeed.data.repository.RoomStatisticsRepository
import com.example.adfeed.data.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FeedUiState(
    val ads: List<AdItem> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

fun mergeInteractionState(ad: AdItem, entity: InteractionEntity?): AdItem {
    return if (entity != null) {
        ad.copy(
            isLiked = entity.isLiked,
            likeCount = ad.likeCount + entity.likeCountOffset,
            isCollected = entity.isCollected,
            collectCount = ad.collectCount + entity.collectCountOffset
        )
    } else {
        ad
    }
}

fun nextLikeDisplayState(ad: AdItem): AdItem {
    val newLiked = !ad.isLiked
    return ad.copy(
        isLiked = newLiked,
        likeCount = nextDisplayCount(ad.likeCount, newLiked)
    )
}

fun nextCollectDisplayState(ad: AdItem): AdItem {
    val newCollected = !ad.isCollected
    return ad.copy(
        isCollected = newCollected,
        collectCount = nextDisplayCount(ad.collectCount, newCollected)
    )
}

fun nextLikeInteraction(entity: InteractionEntity?, adId: String): InteractionEntity {
    val current = entity ?: InteractionEntity(adId, false, false, 0, 0, 0)
    val newLiked = !current.isLiked
    return current.copy(
        isLiked = newLiked,
        likeCountOffset = binaryOffset(newLiked)
    )
}

fun nextCollectInteraction(entity: InteractionEntity?, adId: String): InteractionEntity {
    val current = entity ?: InteractionEntity(adId, false, false, 0, 0, 0)
    val newCollected = !current.isCollected
    return current.copy(
        isCollected = newCollected,
        collectCountOffset = binaryOffset(newCollected)
    )
}

fun mergeDetailInteractionState(ad: AdItem, entity: InteractionEntity?): AdItem {
    val baseAd = MockData.allAds.find { it.id == ad.id } ?: ad
    return mergeInteractionState(baseAd, entity).copy(
        exposureCount = ad.exposureCount,
        clickCount = ad.clickCount
    )
}

private fun nextDisplayCount(count: Int, enabled: Boolean): Int {
    val delta = if (enabled) 1 else -1
    return (count + delta).coerceAtLeast(0)
}

private fun binaryOffset(enabled: Boolean): Int {
    return if (enabled) 1 else 0
}

class FeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _detailAd = MutableStateFlow<AdItem?>(null)
    val detailAd: StateFlow<AdItem?> = _detailAd.asStateFlow()

    // 统计数据仓库（Room 持久化实现）
    private val statisticsRepository: StatisticsRepository = RoomStatisticsRepository

    private var currentChannel = "精选"
    private var currentPage = 0
    private val pageSize = 6
    private var shuffledPool: List<AdItem> = emptyList()

    init {
        shuffledPool = buildPool(currentChannel)
        loadAds()
        observeDatabaseChanges()
    }

    private fun buildPool(channel: String): List<AdItem> {
        return MockData.getByChannel(channel).shuffled()
    }

    /**
     * 订阅 Room 数据库变更，自动同步交互状态（点赞/收藏）到 UI
     */
    private fun observeDatabaseChanges() {
        viewModelScope.launch {
            AdApplication.database.interactionDao().getAllInteractionsFlow().collect { interactions ->
                val interactionMap = interactions.associateBy { it.adId }
                _uiState.update { state ->
                    state.copy(ads = state.ads.map { ad ->
                        val baseAd = MockData.allAds.find { it.id == ad.id } ?: ad
                        mergeInteractionState(baseAd, interactionMap[ad.id]).copy(
                            exposureCount = ad.exposureCount,
                            clickCount = ad.clickCount
                        )
                    })
                }
                _detailAd.value?.let { current ->
                    interactionMap[current.id]?.let { entity ->
                        _detailAd.value = mergeDetailInteractionState(current, entity)
                    }
                }
            }
        }
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

            val newAds = withContext(Dispatchers.IO) {
                shuffledPool.subList(start, end).map { ad ->
                    // 从 Room 加载交互状态
                    val entity = AdApplication.database.interactionDao().getInteraction(ad.id)
                    val merged = mergeInteractionState(ad, entity)

                    // 从 Room 加载持久化的统计数据
                    val exposureCount = AdApplication.database.statisticEventDao()
                        .getExposureCount(ad.id)
                    val clickCount = AdApplication.database.statisticEventDao()
                        .getClickCount(ad.id)
                    merged.copy(
                        exposureCount = exposureCount,
                        clickCount = clickCount
                    )
                }
            }

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

    fun loadDetailAd(adId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val base = _uiState.value.ads.find { it.id == adId }
                ?: MockData.allAds.find { it.id == adId }
                ?: return@launch

            val entity = AdApplication.database.interactionDao().getInteraction(adId)
            val exposureCount = AdApplication.database.statisticEventDao().getExposureCount(adId)
            val clickCount = AdApplication.database.statisticEventDao().getClickCount(adId)

            _detailAd.value = mergeDetailInteractionState(
                base.copy(exposureCount = exposureCount, clickCount = clickCount),
                entity
            )
        }
    }

    fun clearDetailAd() {
        _detailAd.value = null
    }

    fun toggleLike(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) nextLikeDisplayState(ad) else ad
            })
        }
        _detailAd.update { ad ->
            if (ad?.id == adId) nextLikeDisplayState(ad) else ad
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dao = AdApplication.database.interactionDao()
            val entity = dao.getInteraction(adId)
            dao.insertOrUpdate(nextLikeInteraction(entity, adId))
        }
    }

    fun toggleCollect(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) nextCollectDisplayState(ad) else ad
            })
        }
        _detailAd.update { ad ->
            if (ad?.id == adId) nextCollectDisplayState(ad) else ad
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dao = AdApplication.database.interactionDao()
            val entity = dao.getInteraction(adId)
            dao.insertOrUpdate(nextCollectInteraction(entity, adId))
        }
    }

    fun recordClick(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) ad.copy(clickCount = ad.clickCount + 1) else ad
            })
        }
        _detailAd.update { ad ->
            if (ad?.id == adId) ad.copy(clickCount = ad.clickCount + 1) else ad
        }
        // 同时持久化写入 Room 统计仓库
        recordStatEvent(adId, EventType.CLICK)
    }

    fun recordExposure(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) ad.copy(exposureCount = ad.exposureCount + 1) else ad
            })
        }
        _detailAd.update { ad ->
            if (ad?.id == adId) ad.copy(exposureCount = ad.exposureCount + 1) else ad
        }
        // 同时持久化写入 Room 统计仓库
        recordStatEvent(adId, EventType.EXPOSURE)
    }

    /** 向 Room 统计仓库写入事件（异步，不阻塞UI） */
    private fun recordStatEvent(adId: String, eventType: EventType) {
        viewModelScope.launch {
            val ad = _uiState.value.ads.find { it.id == adId }
                ?: _detailAd.value?.takeIf { it.id == adId }
                ?: MockData.allAds.find { it.id == adId }
                ?: return@launch
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
