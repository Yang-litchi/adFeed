package com.example.adfeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adfeed.AdApplication
import com.example.adfeed.data.local.entity.AICacheEntity
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.model.AiInfo
import com.example.adfeed.data.remote.QwenApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val displayedText: String = "",   // 当前逐字显示的文字
    val isTyping: Boolean = false,    // 是否正在打字动画
    val isLoading: Boolean = false,   // 是否正在请求API
    val error: String? = null
)

interface AiCacheStore {
    suspend fun get(adId: String): AICacheEntity?
    suspend fun save(cache: AICacheEntity)
}

private object RoomAiCacheStore : AiCacheStore {
    override suspend fun get(adId: String): AICacheEntity? {
        return AdApplication.database.aiCacheDao().getAICache(adId)
    }

    override suspend fun save(cache: AICacheEntity) {
        AdApplication.database.aiCacheDao().insertCache(cache)
    }
}

class DetailViewModel(
    private val cacheStore: AiCacheStore = RoomAiCacheStore,
    private val generateIntro: suspend (AiInfo, String) -> Result<String> = QwenApi::generateAdIntro,
    private val typingDelayMillis: Long = 30L
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var typingJob: Job? = null

    fun loadIntro(ad: AdItem) {
        val aiInfo = ad.aiInfo
        typingJob?.cancel()

        if (aiInfo == null) {
            _uiState.value = DetailUiState()
            return
        }

        viewModelScope.launch {
            _uiState.value = DetailUiState(isLoading = true)

            val cachedIntro = runCatching { cacheStore.get(ad.id)?.introText }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }

            if (cachedIntro != null) {
                startTypingAnimation(cachedIntro)
                return@launch
            }

            generateIntro(aiInfo, ad.title).fold(
                onSuccess = { text ->
                    val introText = text.ifBlank { fallbackSummary(aiInfo) }
                    runCatching {
                        cacheStore.save(
                            AICacheEntity(
                                adId = ad.id,
                                summary = aiInfo.summary,
                                introText = introText,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    startTypingAnimation(introText)
                },
                onFailure = {
                    startTypingAnimation(
                        fullText = fallbackSummary(aiInfo),
                        error = "AI介绍生成失败，已展示本地摘要"
                    )
                }
            )
        }
    }

    private fun startTypingAnimation(fullText: String, error: String? = null) {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            _uiState.value = DetailUiState(
                isTyping = true,
                displayedText = "",
                error = error
            )
            val sb = StringBuilder()
            for (char in fullText) {
                sb.append(char)
                _uiState.value = DetailUiState(
                    isTyping = true,
                    displayedText = sb.toString(),
                    error = error
                )
                delay(typingDelayMillis)
            }
            _uiState.value = DetailUiState(
                isTyping = false,
                displayedText = fullText,
                error = error
            )
        }
    }

    private fun fallbackSummary(aiInfo: AiInfo): String {
        return aiInfo.summary.ifBlank { "暂无 AI 介绍，可稍后重试。" }
    }

    override fun onCleared() {
        super.onCleared()
        typingJob?.cancel()
    }
}
