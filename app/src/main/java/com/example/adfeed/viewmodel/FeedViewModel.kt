package com.example.adfeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.repository.MockData
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

    private var currentChannel = "精选"
    private var currentPage = 0
    private val pageSize = 4

    init {
        loadAds()
    }

    fun loadAds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(600) // 模拟网络延迟
            val allData = MockData.getByChannel(currentChannel)
            val start = currentPage * pageSize
            val end = minOf(start + pageSize, allData.size)
            if (start >= allData.size) {
                _uiState.update { it.copy(isLoading = false, hasMore = false) }
                return@launch
            }
            val newAds = allData.subList(start, end)
            _uiState.update { state ->
                state.copy(
                    ads = if (currentPage == 0) newAds else state.ads + newAds,
                    isLoading = false,
                    hasMore = end < allData.size
                )
            }
            currentPage++
        }
    }

    fun refresh() {
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
        currentPage = 0
        loadAds()
    }

    fun toggleLike(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) ad.copy(
                    isLiked = !ad.isLiked,
                    likeCount = if (ad.isLiked) ad.likeCount - 1 else ad.likeCount + 1
                ) else ad
            })
        }
    }

    fun toggleCollect(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) ad.copy(isCollected = !ad.isCollected) else ad
            })
        }
    }

    fun recordClick(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) ad.copy(clickCount = ad.clickCount + 1) else ad
            })
        }
    }

    fun recordExposure(adId: String) {
        _uiState.update { state ->
            state.copy(ads = state.ads.map { ad ->
                if (ad.id == adId) ad.copy(exposureCount = ad.exposureCount + 1) else ad
            })
        }
    }
}