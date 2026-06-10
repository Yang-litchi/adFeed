package com.example.adfeed.viewmodel

import com.example.adfeed.data.local.entity.InteractionEntity
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.model.AdType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedInteractionStateTest {
    @Test
    fun mergeInteractionState_addsCollectOffsetToBaseCollectCount() {
        val ad = testAd(collectCount = 20)
        val entity = InteractionEntity(
            adId = "ad-1",
            isLiked = false,
            isCollected = true,
            likeCountOffset = 0,
            collectCountOffset = 1,
            shareCount = 0
        )

        val merged = mergeInteractionState(ad, entity)

        assertTrue(merged.isCollected)
        assertEquals(21, merged.collectCount)
    }

    @Test
    fun mergeInteractionState_keepsBaseCollectCountWhenInteractionIsMissing() {
        val merged = mergeInteractionState(testAd(collectCount = 20), null)

        assertEquals(20, merged.collectCount)
        assertFalse(merged.isCollected)
    }

    @Test
    fun nextCollectInteraction_collectingIncrementsOffset() {
        val next = nextCollectInteraction(
            InteractionEntity("ad-1", false, false, 0, 0, 0),
            adId = "ad-1"
        )

        assertTrue(next.isCollected)
        assertEquals(1, next.collectCountOffset)
    }

    @Test
    fun nextCollectInteraction_uncollectingDecrementsOffset() {
        val next = nextCollectInteraction(
            InteractionEntity("ad-1", false, true, 0, 1, 0),
            adId = "ad-1"
        )

        assertFalse(next.isCollected)
        assertEquals(0, next.collectCountOffset)
    }

    @Test
    fun nextCollectInteraction_uncollectingLegacyCollectedStateDoesNotGoNegative() {
        val next = nextCollectInteraction(
            InteractionEntity("ad-1", false, true, 0, 0, 0),
            adId = "ad-1"
        )

        assertFalse(next.isCollected)
        assertEquals(0, next.collectCountOffset)
    }

    @Test
    fun nextCollectInteraction_collectingInconsistentOffsetNormalizesToOne() {
        val next = nextCollectInteraction(
            InteractionEntity("ad-1", false, false, 0, 2, 0),
            adId = "ad-1"
        )

        assertTrue(next.isCollected)
        assertEquals(1, next.collectCountOffset)
    }

    private fun testAd(collectCount: Int): AdItem {
        return AdItem(
            id = "ad-1",
            type = AdType.LARGE_IMAGE,
            title = "测试广告",
            description = "测试描述",
            imageUrl = "https://example.com/a.jpg",
            channel = "精选",
            collectCount = collectCount
        )
    }
}
