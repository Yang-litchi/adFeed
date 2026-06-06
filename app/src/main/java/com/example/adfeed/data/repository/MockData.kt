package com.example.adfeed.data.repository

import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.model.AdType

object MockData {
    val allAds = listOf(
        AdItem(
            id = "001", type = AdType.LARGE_IMAGE,
            title = "Nike Air Max 2024 全新上市",
            description = "革命性气垫科技，给你极致缓震体验，适合日常训练和街头穿搭。",
            imageUrl = "https://picsum.photos/seed/nike/800/400",
            channel = "精选", likeCount = 1204,
            tags = listOf("运动", "性价比", "学生党")
        ),
        AdItem(
            id = "002", type = AdType.VIDEO,
            title = "星巴克新品：樱花拿铁限时上市",
            description = "限时春季特饮，现在下单享8折优惠，门店自提免排队。",
            imageUrl = "https://picsum.photos/seed/coffee/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "精选", likeCount = 867,
            tags = listOf("美食", "限时", "本地")
        ),
        AdItem(
            id = "003", type = AdType.SMALL_IMAGE,
            title = "双十一提前购，全场5折起",
            description = "限时48小时，错过再等一年，数码、家居、服饰全覆盖。",
            imageUrl = "https://picsum.photos/seed/shop/400/300",
            channel = "电商", likeCount = 3421,
            tags = listOf("电商", "限时", "性价比")
        ),
        AdItem(
            id = "004", type = AdType.LARGE_IMAGE,
            title = "海底捞外卖，30分钟送达",
            description = "本地门店直送，火锅自由从未如此简单，满99元免配送费。",
            imageUrl = "https://picsum.photos/seed/hotpot/800/400",
            channel = "本地", likeCount = 562,
            tags = listOf("美食", "本地", "高端")
        ),
        AdItem(
            id = "005", type = AdType.VIDEO,
            title = "小米15 Pro 正式发布",
            description = "徕卡影像 + 骁龙8 Elite，影像旗舰新标杆，预售立减500元。",
            imageUrl = "https://picsum.photos/seed/phone/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商", likeCount = 9823,
            tags = listOf("科技", "高端")
        ),
        AdItem(
            id = "006", type = AdType.SMALL_IMAGE,
            title = "Keep会员限时5折，运动不停歇",
            description = "超过10000节课程任意学，私教课程8折起，学生认证再减20元。",
            imageUrl = "https://picsum.photos/seed/keep/400/300",
            channel = "精选", likeCount = 445,
            tags = listOf("运动", "学生党", "性价比")
        ),
        AdItem(
            id = "007", type = AdType.LARGE_IMAGE,
            title = "宜家新品春季家居系列",
            description = "清新北欧风格，让家里充满春天气息，全系列现货发售。",
            imageUrl = "https://picsum.photos/seed/ikea/800/400",
            channel = "电商", likeCount = 731,
            tags = listOf("家居", "性价比")
        ),
        AdItem(
            id = "008", type = AdType.VIDEO,
            title = "瑞幸咖啡 x 茅台联名回归",
            description = "酱香拿铁2.0升级版，全国门店同步上市，每日限量供应。",
            imageUrl = "https://picsum.photos/seed/luckin/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "本地", likeCount = 15632,
            tags = listOf("美食", "限时", "本地")
        ),
        AdItem(
            id = "009", type = AdType.SMALL_IMAGE,
            title = "网易严选护肤套装买一送一",
            description = "氨基酸洁面+玻尿酸精华+防晒三件套，适合敏感肌日常护理。",
            imageUrl = "https://picsum.photos/seed/skincare/400/300",
            channel = "电商", likeCount = 2108,
            tags = listOf("时尚", "性价比", "学生党")
        ),
        AdItem(
            id = "010", type = AdType.LARGE_IMAGE,
            title = "顺丰同城·急送，1小时达",
            description = "文件、蛋糕、药品急送，下单即配，最快30分钟上门取件。",
            imageUrl = "https://picsum.photos/seed/delivery/800/400",
            channel = "本地", likeCount = 334,
            tags = listOf("本地", "性价比")
        ),
        AdItem(
            id = "011", type = AdType.LARGE_IMAGE,
            title = "Adidas 跑鞋新款限时特惠",
            description = "Boost缓震技术，马拉松级别支撑，现在购买立享7折。",
            imageUrl = "https://picsum.photos/seed/adidas/800/400",
            channel = "精选", likeCount = 2341,
            tags = listOf("运动", "高端", "限时")
        ),
        AdItem(
            id = "012", type = AdType.SMALL_IMAGE,
            title = "京东超市 生鲜配送",
            description = "当日下单次日达，新鲜蔬果直达家门，首单减20元。",
            imageUrl = "https://picsum.photos/seed/jd/400/300",
            channel = "电商", likeCount = 876,
            tags = listOf("电商", "本地", "性价比")
        ),
        AdItem(
            id = "013", type = AdType.VIDEO,
            title = "索尼 WH-1000XM6 降噪耳机",
            description = "业界顶级主动降噪，30小时续航，学生党专属优惠价。",
            imageUrl = "https://picsum.photos/seed/sony/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商", likeCount = 5421,
            tags = listOf("科技", "学生党", "高端")
        ),
        AdItem(
            id = "014", type = AdType.LARGE_IMAGE,
            title = "本地健身房月卡特惠",
            description = "全器械开放+游泳池+团课无限次，新用户首月仅需99元。",
            imageUrl = "https://picsum.photos/seed/gym/800/400",
            channel = "本地", likeCount = 1087,
            tags = listOf("运动", "本地", "学生党")
        ),
        AdItem(
            id = "015", type = AdType.SMALL_IMAGE,
            title = "优衣库 UT 联名系列上新",
            description = "全球艺术家联名设计，限量发售，尺码告急中。",
            imageUrl = "https://picsum.photos/seed/uniqlo/400/300",
            channel = "精选", likeCount = 3209,
            tags = listOf("时尚", "限时", "学生党")
        ),
        AdItem(
            id = "016", type = AdType.VIDEO,
            title = "大疆 Osmo Mobile 6 稳定器",
            description = "手机拍摄神器，三轴防抖，一键跟踪，拍出电影级画面。",
            imageUrl = "https://picsum.photos/seed/dji/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商", likeCount = 4102,
            tags = listOf("科技", "性价比")
        ),
        AdItem(
            id = "017", type = AdType.LARGE_IMAGE,
            title = "必胜客新品披萨 买一送一",
            description = "芝士爆浆新口味，堂食外卖同享优惠，限本周末。",
            imageUrl = "https://picsum.photos/seed/pizza/800/400",
            channel = "本地", likeCount = 2876,
            tags = listOf("美食", "限时", "本地")
        ),
        AdItem(
            id = "018", type = AdType.SMALL_IMAGE,
            title = "亚马逊 Kindle 青春版",
            description = "轻薄护眼电子书阅读器，内置海量书籍，学生党必备神器。",
            imageUrl = "https://picsum.photos/seed/kindle/400/300",
            channel = "电商", likeCount = 1543,
            tags = listOf("科技", "学生党", "性价比")
        ),
        AdItem(
            id = "019", type = AdType.LARGE_IMAGE,
            title = "完美日记 新品唇膏系列",
            description = "国货之光，20种色号任选，滋润不脱色，学生党平价首选。",
            imageUrl = "https://picsum.photos/seed/makeup/800/400",
            channel = "精选", likeCount = 6721,
            tags = listOf("时尚", "学生党", "性价比")
        ),
        AdItem(
            id = "020", type = AdType.VIDEO,
            title = "特斯拉 Model 3 焕新版",
            description = "续航800km，15分钟快充，现在下定金享专属权益。",
            imageUrl = "https://picsum.photos/seed/tesla/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "精选", likeCount = 18432,
            tags = listOf("科技", "高端")
        ),
        AdItem(
            id = "021", type = AdType.SMALL_IMAGE,
            title = "饿了么超级会员 首月1元",
            description = "每月12张红包券+免配送费+专属折扣，外卖自由从此开始。",
            imageUrl = "https://picsum.photos/seed/eleme/400/300",
            channel = "本地", likeCount = 4312,
            tags = listOf("美食", "本地", "性价比")
        ),
        AdItem(
            id = "022", type = AdType.LARGE_IMAGE,
            title = "lululemon 瑜伽裤新色上架",
            description = "面料柔软亲肤，高腰提臀设计，运动日常两相宜。",
            imageUrl = "https://picsum.photos/seed/yoga/800/400",
            channel = "精选", likeCount = 3876,
            tags = listOf("运动", "时尚", "高端")
        ),
        AdItem(
            id = "023", type = AdType.VIDEO,
            title = "Switch 2 游戏主机正式开售",
            description = "全新性能升级，支持4K输出，首发限量礼包现已开放预购。",
            imageUrl = "https://picsum.photos/seed/switch/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商", likeCount = 22341,
            tags = listOf("科技", "高端", "限时")
        ),
        AdItem(
            id = "024", type = AdType.SMALL_IMAGE,
            title = "周黑鸭 卤味拼盘 新品上市",
            description = "精选食材卤制，香辣鲜嫩，下单满50元包邮到家。",
            imageUrl = "https://picsum.photos/seed/spicy/400/300",
            channel = "本地", likeCount = 987,
            tags = listOf("美食", "性价比", "本地")
        ),
        AdItem(
            id = "025", type = AdType.LARGE_IMAGE,
            title = "Apple Watch Series 10",
            description = "更薄更轻，血氧心率实时监测，运动健康管理全面升级。",
            imageUrl = "https://picsum.photos/seed/applewatch/800/400",
            channel = "精选", likeCount = 14231,
            tags = listOf("科技", "运动", "高端")
        ),
        AdItem(
            id = "026", type = AdType.SMALL_IMAGE,
            title = "网易云音乐黑胶VIP季卡",
            description = "无损音质+独家歌单+歌词翻译，现在购买享8折优惠。",
            imageUrl = "https://picsum.photos/seed/music/400/300",
            channel = "电商", likeCount = 2109,
            tags = listOf("性价比", "学生党")
        ),
        AdItem(
            id = "027", type = AdType.VIDEO,
            title = "戴森 V15 吸尘器旗舰款",
            description = "激光除尘技术，智能感应调节吸力，家居清洁一步到位。",
            imageUrl = "https://picsum.photos/seed/dyson/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商", likeCount = 7654,
            tags = listOf("家居", "高端", "科技")
        ),
        AdItem(
            id = "028", type = AdType.LARGE_IMAGE,
            title = "汉堡王 新品皇堡双层特惠",
            description = "双层牛肉+芝士融合，本周买一赠一，仅限门店自取。",
            imageUrl = "https://picsum.photos/seed/burger/800/400",
            channel = "本地", likeCount = 1432,
            tags = listOf("美食", "限时", "本地")
        ),
        AdItem(
            id = "029", type = AdType.SMALL_IMAGE,
            title = "芒果TV会员 年卡特价",
            description = "热门综艺+独播剧+4K画质，年卡价格直降50元，限时抢购。",
            imageUrl = "https://picsum.photos/seed/mango/400/300",
            channel = "精选", likeCount = 3241,
            tags = listOf("限时", "性价比", "学生党")
        ),
        AdItem(
            id = "030", type = AdType.LARGE_IMAGE,
            title = "三顿半 冻干咖啡新品礼盒",
            description = "精品咖啡豆萃取，0糖0脂，冷热水均可溶，送礼自用两相宜。",
            imageUrl = "https://picsum.photos/seed/3tops/800/400",
            channel = "电商", likeCount = 5432,
            tags = listOf("美食", "性价比", "电商")
        )
    )

    fun getByChannel(channel: String): List<AdItem> {
        if (channel == "精选") return allAds
        return allAds.filter { it.channel == channel }
    }
}