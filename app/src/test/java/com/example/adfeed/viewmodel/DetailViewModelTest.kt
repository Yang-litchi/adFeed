package com.example.adfeed.viewmodel

import com.example.adfeed.data.local.entity.AICacheEntity
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.model.AdType
import com.example.adfeed.data.model.AiInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadIntro_usesRoomCacheWithoutCallingGenerator() = runTest(dispatcher) {
        var generatorCalls = 0
        val cache = FakeAiCacheStore(
            cached = AICacheEntity(
                adId = "ad-1",
                summary = "本地摘要",
                introText = "Room 缓存介绍",
                timestamp = 100L
            )
        )
        val viewModel = DetailViewModel(
            cacheStore = cache,
            generateIntro = { _, _ ->
                generatorCalls++
                Result.success("不应生成")
            },
            typingDelayMillis = 0L
        )

        viewModel.loadIntro(testAd())
        advanceUntilIdle()

        assertEquals("Room 缓存介绍", viewModel.uiState.value.displayedText)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isTyping)
        assertEquals(0, generatorCalls)
        assertTrue(cache.saved.isEmpty())
    }

    @Test
    fun loadIntro_savesGeneratedIntroToRoomCache() = runTest(dispatcher) {
        val cache = FakeAiCacheStore()
        val viewModel = DetailViewModel(
            cacheStore = cache,
            generateIntro = { _, title -> Result.success("$title 的生成介绍") },
            typingDelayMillis = 0L
        )

        viewModel.loadIntro(testAd())
        advanceUntilIdle()

        assertEquals("测试广告 的生成介绍", viewModel.uiState.value.displayedText)
        assertEquals(1, cache.saved.size)
        assertEquals("ad-1", cache.saved.single().adId)
        assertEquals("本地摘要", cache.saved.single().summary)
        assertEquals("测试广告 的生成介绍", cache.saved.single().introText)
    }

    @Test
    fun loadIntro_showsSummaryAndErrorWhenGenerationFails() = runTest(dispatcher) {
        val cache = FakeAiCacheStore()
        val viewModel = DetailViewModel(
            cacheStore = cache,
            generateIntro = { _, _ -> Result.failure(IllegalStateException("network failed")) },
            typingDelayMillis = 0L
        )

        viewModel.loadIntro(testAd(summary = "失败时的本地摘要"))
        advanceUntilIdle()

        assertEquals("失败时的本地摘要", viewModel.uiState.value.displayedText)
        assertEquals("AI介绍生成失败，已展示本地摘要", viewModel.uiState.value.error)
        assertTrue(cache.saved.isEmpty())
    }

    @Test
    fun loadIntro_usesFallbackTextWhenSummaryIsBlank() = runTest(dispatcher) {
        val viewModel = DetailViewModel(
            cacheStore = FakeAiCacheStore(),
            generateIntro = { _, _ -> Result.failure(IllegalStateException("network failed")) },
            typingDelayMillis = 0L
        )

        viewModel.loadIntro(testAd(summary = ""))
        advanceUntilIdle()

        assertEquals("暂无 AI 介绍，可稍后重试。", viewModel.uiState.value.displayedText)
        assertEquals("AI介绍生成失败，已展示本地摘要", viewModel.uiState.value.error)
    }

    private fun testAd(
        id: String = "ad-1",
        summary: String = "本地摘要"
    ): AdItem {
        return AdItem(
            id = id,
            type = AdType.LARGE_IMAGE,
            title = "测试广告",
            description = "测试描述",
            imageUrl = "https://example.com/a.jpg",
            channel = "精选",
            aiInfo = AiInfo(
                summary = summary,
                features = listOf("轻便"),
                targetUsers = listOf("学生"),
                recommendReasons = listOf("性价比高"),
                scenarios = listOf("通勤")
            )
        )
    }
}

private class FakeAiCacheStore(
    private val cached: AICacheEntity? = null
) : AiCacheStore {
    val saved = mutableListOf<AICacheEntity>()

    override suspend fun get(adId: String): AICacheEntity? {
        return cached?.takeIf { it.adId == adId }
    }

    override suspend fun save(cache: AICacheEntity) {
        saved += cache
    }
}
