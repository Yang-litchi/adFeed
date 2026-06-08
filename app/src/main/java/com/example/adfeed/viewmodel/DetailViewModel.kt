package com.example.adfeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adfeed.AdApplication
import com.example.adfeed.data.local.entity.AICacheEntity
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.remote.QwenApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DetailUiState(
    val displayedText: String = "",   // 当前逐字显示的文字
    val isTyping: Boolean = false,    // 是否正在打字动画
    val isLoading: Boolean = false,   // 是否正在加载
    val error: String? = null
)

class DetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var typingJob: Job? = null

    fun loadIntro(ad: AdItem) {
        val aiInfo = ad.aiInfo ?: return

        viewModelScope.launch {
            _uiState.value = DetailUiState(isLoading = true)
            
            // 1. 尝试从 Room 本地缓存加载 AI 介绍
            val cached = withContext(Dispatchers.IO) {
                AdApplication.database.aiCacheDao().getAICache(ad.id)
            }
            
            if (cached != null) {
                // 缓存命中：直接启动打字机动画播放已保存的介绍
                startTypingAnimation(cached.introText)
                return@launch
            }

            // 2. 缓存未命中：发起 Qwen API 调用进行生成
            QwenApi.generateAdIntro(aiInfo, ad.title).fold(
                onSuccess = { text ->
                    // 生成成功：将结果写入本地 Room 缓存
                    withContext(Dispatchers.IO) {
                        AdApplication.database.aiCacheDao().insertCache(
                            AICacheEntity(
                                adId = ad.id,
                                summary = aiInfo.summary,
                                introText = text,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    startTypingAnimation(text)
                },
                onFailure = {
                    // 生成失败 (如网络连接超时/出错)：优雅降级，使用 aiInfo.summary 作为打字机内容
                    val fallbackText = aiInfo.summary
                    startTypingAnimation(fallbackText)
                }
            )
        }
    }

    private fun startTypingAnimation(fullText: String) {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            _uiState.value = DetailUiState(isTyping = true, displayedText = "")
            val sb = StringBuilder()
            for (char in fullText) {
                sb.append(char)
                _uiState.value = DetailUiState(
                    isTyping = true,
                    displayedText = sb.toString()
                )
                delay(30L)
            }
            _uiState.value = DetailUiState(
                isTyping = false,
                displayedText = fullText
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        typingJob?.cancel()
    }
}