package com.example.adfeed.data.repository

import org.junit.Assert.assertTrue
import org.junit.Test

class MockDataTest {
    @Test
    fun allMockAds_havePositiveCollectCount() {
        val adsWithoutCollectCount = MockData.allAds.filter { it.collectCount <= 0 }

        assertTrue(
            "以下广告缺少正数收藏初始值: ${adsWithoutCollectCount.map { it.id }}",
            adsWithoutCollectCount.isEmpty()
        )
    }
}
