package com.example.adfeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adfeed.data.model.AdItem
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

class DetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // 缓存，key=adId，避免重复请求
    private val cache = HashMap<String, String>()
    private var typingJob: Job? = null

    fun loadIntro(ad: AdItem) {
        val aiInfo = ad.aiInfo ?: return

        // 命中缓存直接播放动画
        cache[ad.id]?.let {
            startTypingAnimation(it)
            return
        }

        viewModelScope.launch {
            _uiState.value = DetailUiState(isLoading = true)
            QwenApi.generateAdIntro(aiInfo, ad.title).fold(
                onSuccess = { text ->
                    cache[ad.id] = text
                    startTypingAnimation(text)
                },
                onFailure = {
                    _uiState.value = DetailUiState(error = "AI介绍生成失败")
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