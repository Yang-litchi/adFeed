package com.example.adfeed.data.repository

import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.model.AdType

object MockData {
    val allAds = listOf(
        AdItem(
            id = "001",
            type = AdType.LARGE_IMAGE,
            title = "Nike Air Max 2024 全新上市",
            description = "革命性气垫科技，给你极致缓震体验，适合日常训练和街头穿搭。",
            imageUrl = "https://picsum.photos/seed/nike/800/400",
            channel = "精选",
            likeCount = 1204,
            tags = listOf("运动", "性价比", "学生党")
        ),
        AdItem(
            id = "002",
            type = AdType.VIDEO,
            title = "星巴克新品：樱花拿铁限时上市",
            description = "限时春季特饮，现在下单享8折优惠，门店自提免排队。",
            imageUrl = "https://picsum.photos/seed/coffee/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "精选",
            likeCount = 867,
            tags = listOf("美食", "限时", "本地")
        ),
        AdItem(
            id = "003",
            type = AdType.SMALL_IMAGE,
            title = "双十一提前购，全场5折起",
            description = "限时48小时，错过再等一年，数码、家居、服饰全覆盖。",
            imageUrl = "https://picsum.photos/seed/shop/400/300",
            channel = "电商",
            likeCount = 3421,
            tags = listOf("电商", "限时", "性价比")
        ),
        AdItem(
            id = "004",
            type = AdType.LARGE_IMAGE,
            title = "海底捞外卖，30分钟送达",
            description = "本地门店直送，火锅自由从未如此简单，满99元免配送费。",
            imageUrl = "https://picsum.photos/seed/hotpot/800/400",
            channel = "本地",
            likeCount = 562,
            tags = listOf("美食", "本地", "高端")
        ),
        AdItem(
            id = "005",
            type = AdType.VIDEO,
            title = "小米15 Pro 正式发布",
            description = "徕卡影像 + 骁龙8 Elite，影像旗舰新标杆，预售立减500元。",
            imageUrl = "https://picsum.photos/seed/phone/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商",
            likeCount = 9823,
            tags = listOf("科技", "高端")
        ),
        AdItem(
            id = "006",
            type = AdType.SMALL_IMAGE,
            title = "Keep会员限时5折，运动不停歇",
            description = "超过10000节课程任意学，私教课程8折起，学生认证再减20元。",
            imageUrl = "https://picsum.photos/seed/keep/400/300",
            channel = "精选",
            likeCount = 445,
            tags = listOf("运动", "学生党", "性价比")
        ),
        AdItem(
            id = "007",
            type = AdType.LARGE_IMAGE,
            title = "宜家新品春季家居系列",
            description = "清新北欧风格，让家里充满春天气息，全系列现货发售。",
            imageUrl = "https://picsum.photos/seed/ikea/800/400",
            channel = "电商",
            likeCount = 731,
            tags = listOf("家居", "性价比")
        ),
        AdItem(
            id = "008",
            type = AdType.VIDEO,
            title = "瑞幸咖啡 x 茅台联名回归",
            description = "酱香拿铁2.0升级版，全国门店同步上市，每日限量供应。",
            imageUrl = "https://picsum.photos/seed/luckin/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "本地",
            likeCount = 15632,
            tags = listOf("美食", "限时", "本地")
        ),
        AdItem(
            id = "009",
            type = AdType.SMALL_IMAGE,
            title = "网易严选护肤套装买一送一",
            description = "氨基酸洁面+玻尿酸精华+防晒三件套，适合敏感肌日常护理。",
            imageUrl = "https://picsum.photos/seed/skincare/400/300",
            channel = "电商",
            likeCount = 2108,
            tags = listOf("时尚", "性价比", "学生党")
        ),
        AdItem(
            id = "010",
            type = AdType.LARGE_IMAGE,
            title = "顺丰同城·急送，1小时达",
            description = "文件、蛋糕、药品急送，下单即配，最快30分钟上门取件。",
            imageUrl = "https://picsum.photos/seed/delivery/800/400",
            channel = "本地",
            likeCount = 334,
            tags = listOf("本地", "性价比")
        )
    )

    fun getByChannel(channel: String): List<AdItem> {
        if (channel == "精选") return allAds
        return allAds.filter { it.channel == channel }
    }
}