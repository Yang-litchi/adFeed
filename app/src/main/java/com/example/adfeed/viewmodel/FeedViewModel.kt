package com.example.adfeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adfeed.AdApplication
import com.example.adfeed.data.exposure.ExposureTracker
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

class FeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    // 详情页独立数据源
    private val _detailAd = MutableStateFlow<AdItem?>(null)
    val detailAd: StateFlow<AdItem?> = _detailAd.asStateFlow()

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

    private fun observeDatabaseChanges() {
        viewModelScope.launch {
            AdApplication.database.interactionDao().getAllInteractionsFlow().collect { interactions ->
                val interactionMap = interactions.associateBy { it.adId }

                // 更新列表
                _uiState.update { state ->
                    state.copy(ads = state.ads.map { ad ->
                        val entity = interactionMap[ad.id]
                        if (entity != null) {
                            ad.copy(
                                isLiked = entity.isLiked,
                                likeCount = ad.likeCount + entity.likeCountOffset,
                                isCollected = entity.isCollected
                            )
                        } else ad
                    })
                }

                // 同步更新详情页
                _detailAd.value?.let { current ->
                    val entity = interactionMap[current.id]
                    if (entity != null) {
                        val baseCount = MockData.allAds
                            .find { it.id == current.id }?.likeCount ?: current.likeCount
                        _detailAd.value = current.copy(
                            isLiked = entity.isLiked,
                            likeCount = baseCount + entity.likeCountOffset,
                            isCollected = entity.isCollected
                        )
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
                    val entity = AdApplication.database.interactionDao().getInteraction(ad.id)
                    val merged = if (entity != null) {
                        ad.copy(
                            isLiked = entity.isLiked,
                            likeCount = ad.likeCount + entity.likeCountOffset,
                            isCollected = entity.isCollected
                        )
                    } else ad

                    val exposureCount = AdApplication.database.statisticEventDao()
                        .getExposureCount(ad.id)
                    val clickCount = AdApplication.database.statisticEventDao()
                        .getClickCount(ad.id)
                    merged.copy(exposureCount = exposureCount, clickCount = clickCount)
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

            val merged = if (entity != null) {
                base.copy(
                    isLiked = entity.isLiked,
                    likeCount = (MockData.allAds.find { it.id == adId }?.likeCount
                        ?: base.likeCount) + entity.likeCountOffset,
                    isCollected = entity.isCollected,
                    exposureCount = exposureCount,
                    clickCount = clickCount
                )
            } else {
                base.copy(exposureCount = exposureCount, clickCount = clickCount)
            }

            _detailAd.value = merged
        }
    }

    fun clearDetailAd() {
        _detailAd.value = null
    }

    fun toggleLike(adId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dao = AdApplication.database.interactionDao()
            val entity = dao.getInteraction(adId)
                ?: InteractionEntity(adId, false, false, 0, 0, 0)
            val newLiked = !entity.isLiked
            val newOffset = entity.likeCountOffset + (if (newLiked) 1 else -1)
            dao.insertOrUpdate(entity.copy(isLiked = newLiked, likeCountOffset = newOffset))
        }
    }

    fun toggleCollect(adId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dao = AdApplication.database.interactionDao()
            val entity = dao.getInteraction(adId)
                ?: InteractionEntity(adId, false, false, 0, 0, 0)
            val newCollected = !entity.isCollected
            dao.insertOrUpdate(entity.copy(isCollected = newCollected))
        }
    }

    fun recordClick(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) ad.copy(clickCount = ad.clickCount + 1) else ad
            })
        }
        // detailAd同步更新点击数
        _detailAd.value?.let { current ->
            if (current.id == adId) {
                _detailAd.value = current.copy(clickCount = current.clickCount + 1)
            }
        }
        recordStatEvent(adId, EventType.CLICK)
    }

    private fun increaseExposure(adId: String) {

        _uiState.update { state ->
            state.copy(
                ads = state.ads.map { ad ->
                    if (ad.id == adId)
                        ad.copy(
                            exposureCount = ad.exposureCount + 1
                        )
                    else ad
                }
            )
        }

        _detailAd.value?.let { current ->
            if (current.id == adId) {
                _detailAd.value = current.copy(
                    exposureCount = current.exposureCount + 1
                )
            }
        }

        recordStatEvent(adId, EventType.EXPOSURE)
    }
    fun recordExposure(adId: String) {

        // Feed曝光：本次App启动期间只统计一次
        if (ExposureTracker.isExposed(adId)) {
            return
        }
        // 记录曝光
        ExposureTracker.markExposed(adId)

        increaseExposure(adId)
    }

    fun recordAiExposure(adId: String) {
        increaseExposure(adId)
    }
    private fun recordStatEvent(adId: String, eventType: EventType) {
        viewModelScope.launch {
            // 优先从uiState找，找不到从detailAd找，再找不到从MockData找
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