package com.example.adfeed.data.repository

import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.model.AdType
import com.example.adfeed.data.model.AiInfo

object MockData {
    val allAds = listOf(
        AdItem(
            id = "001",
            type = AdType.LARGE_IMAGE,
            title = "Nike Air Max 2024 全新上市",
            description = "革命性气垫科技，给你极致缓震体验，适合日常训练和街头穿搭。",
            imageUrl = "https://picsum.photos/seed/nike/800/400",
            channel = "精选",
            likeCount = 1204, collectCount = 238,
            tags = listOf(
                "运动",
                "性价比",
                "学生党"
            ),
            aiInfo = AiInfo(
                summary =
                    "适合大学生和运动爱好者的高性价比运动鞋。",
                features = listOf(
                    "Air Max气垫",
                    "轻量化设计",
                    "透气鞋面"
                ),
                targetUsers = listOf(
                    "大学生",
                    "运动爱好者"
                ),
                recommendReasons = listOf(
                    "缓震效果优秀",
                    "日常穿搭百搭"
                ),
                scenarios = listOf(
                    "跑步",
                    "健身",
                    "校园通勤"
                )
            )
        ),
        AdItem(
            id = "002", type = AdType.VIDEO,
            title = "星巴克新品：樱花拿铁限时上市",
            description = "限时春季特饮，现在下单享8折优惠，门店自提免排队。",
            imageUrl = "https://picsum.photos/seed/coffee/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "精选", likeCount = 867, collectCount = 156,
            tags = listOf("美食", "限时", "本地"),
            aiInfo = AiInfo(
                summary = "春季限定樱花拿铁，限时优惠，适合咖啡爱好者和上班族。",
                features = listOf("春季限定口味", "8折优惠", "门店自提免排队"),
                targetUsers = listOf("咖啡爱好者", "上班族", "本地居民"),
                recommendReasons = listOf("限时特惠性价比高", "自提便捷省时"),
                scenarios = listOf("下午茶", "早餐搭配", "通勤小憩")
            )
        ),
        AdItem(
            id = "003", type = AdType.SMALL_IMAGE,
            title = "双十一提前购，全场5折起",
            description = "限时48小时，错过再等一年，数码、家居、服饰全覆盖。",
            imageUrl = "https://picsum.photos/seed/shop/400/300",
            channel = "电商", likeCount = 3421, collectCount = 684,
            tags = listOf("电商", "限时", "性价比"),
            aiInfo = AiInfo(
                summary = "双十一提前购限时特惠，全品类5折起，高性价比购物首选。",
                features = listOf("限时48小时", "全场5折起", "全品类覆盖"),
                targetUsers = listOf("网购人群", "性价比追求者", "囤货用户"),
                recommendReasons = listOf("价格优惠力度大", "品类齐全一站式购齐"),
                scenarios = listOf("日常囤货", "节日采购", "数码家居换新")
            )
        ),
        AdItem(
            id = "004", type = AdType.LARGE_IMAGE,
            title = "海底捞外卖，30分钟送达",
            description = "本地门店直送，火锅自由从未如此简单，满99元免配送费。",
            imageUrl = "https://picsum.photos/seed/hotpot/800/400",
            channel = "本地", likeCount = 562, collectCount = 97,
            tags = listOf("美食", "本地", "高端"),
            aiInfo = AiInfo(
                summary = "本地海底捞外卖快速配送，满减优惠，适合家庭聚餐和懒人美食需求。",
                features = listOf("30分钟快速送达", "本地门店直送", "满99免配送费"),
                targetUsers = listOf("火锅爱好者", "本地居民", "家庭聚餐人群"),
                recommendReasons = listOf("配送速度快", "足不出户吃火锅", "配送优惠划算"),
                scenarios = listOf("家庭聚餐", "朋友小聚", "懒人宅家")
            )
        ),
        AdItem(
            id = "005", type = AdType.VIDEO,
            title = "小米15 Pro 正式发布",
            description = "徕卡影像 + 骁龙8 Elite，影像旗舰新标杆，预售立减500元。",
            imageUrl = "https://picsum.photos/seed/phone/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商", likeCount = 9823, collectCount = 1821,
            tags = listOf("科技", "高端"),
            aiInfo = AiInfo(
                summary = "小米高端影像旗舰手机，徕卡加持，预售享大额优惠。",
                features = listOf("徕卡影像系统", "骁龙8 Elite芯片", "预售立减500元"),
                targetUsers = listOf("数码爱好者", "摄影爱好者", "高端手机用户"),
                recommendReasons = listOf("影像实力顶尖", "性能配置拉满", "预售优惠超值"),
                scenarios = listOf("日常使用", "专业摄影", "游戏娱乐")
            )
        ),
        AdItem(
            id = "006", type = AdType.SMALL_IMAGE,
            title = "Keep会员限时5折，运动不停歇",
            description = "超过10000节课程任意学，私教课程8折起，学生认证再减20元。",
            imageUrl = "https://picsum.photos/seed/keep/400/300",
            channel = "精选", likeCount = 445, collectCount = 88,
            tags = listOf("运动", "学生党", "性价比"),
            aiInfo = AiInfo(
                summary = "高性价比健身会员，海量课程，学生专属额外优惠。",
                features = listOf("会员限时5折", "万节免费课程", "私教8折+学生再减20"),
                targetUsers = listOf("健身爱好者", "学生党", "居家运动人群"),
                recommendReasons = listOf("价格实惠性价比高", "课程丰富全面", "学生专属福利"),
                scenarios = listOf("居家健身", "校园运动", "减脂塑形")
            )
        ),
        AdItem(
            id = "007", type = AdType.LARGE_IMAGE,
            title = "宜家新品春季家居系列",
            description = "清新北欧风格，让家里充满春天气息，全系列现货发售。",
            imageUrl = "https://picsum.photos/seed/ikea/800/400",
            channel = "电商", likeCount = 731, collectCount = 143,
            tags = listOf("家居", "性价比"),
            aiInfo = AiInfo(
                summary = "春季北欧风家居新品，清新美观，高性价比现货发售。",
                features = listOf("清新北欧风格", "春季主题设计", "全系列现货"),
                targetUsers = listOf("家居爱好者", "租房人群", "家装改造用户"),
                recommendReasons = listOf("风格清新百搭", "性价比高", "现货即买即得"),
                scenarios = listOf("家居布置", "房间改造", "春季焕新")
            )
        ),
        AdItem(
            id = "008", type = AdType.VIDEO,
            title = "瑞幸咖啡 x 茅台联名回归",
            description = "酱香拿铁2.0升级版，全国门店同步上市，每日限量供应。",
            imageUrl = "https://picsum.photos/seed/luckin/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "本地", likeCount = 15632, collectCount = 2549,
            tags = listOf("美食", "限时", "本地"),
            aiInfo = AiInfo(
                summary = "网红联名酱香拿铁升级版，限量供应，本地门店可购。",
                features = listOf("酱香拿铁2.0", "茅台联名", "每日限量供应"),
                targetUsers = listOf("咖啡爱好者", "网红饮品打卡族", "本地居民"),
                recommendReasons = listOf("联名爆款回归", "口味升级独特", "门店就近购买"),
                scenarios = listOf("下午茶", "通勤提神", "网红打卡")
            )
        ),
        AdItem(
            id = "009", type = AdType.SMALL_IMAGE,
            title = "网易严选护肤套装买一送一",
            description = "氨基酸洁面+玻尿酸精华+防晒三件套，适合敏感肌日常护理。",
            imageUrl = "https://picsum.photos/seed/skincare/400/300",
            channel = "电商", likeCount = 2108, collectCount = 412,
            tags = listOf("时尚", "性价比", "学生党"),
            aiInfo = AiInfo(
                summary = "敏感肌友好护肤套装，买一送一，学生党平价护肤首选。",
                features = listOf("敏感肌适用", "三件套组合", "买一送一优惠"),
                targetUsers = listOf("学生党", "敏感肌人群", "平价护肤追求者"),
                recommendReasons = listOf("价格亲民性价比高", "温和不刺激", "套装护理更全面"),
                scenarios = listOf("日常护肤", "学生护肤", "敏感肌护理")
            )
        ),
        AdItem(
            id = "010", type = AdType.LARGE_IMAGE,
            title = "顺丰同城·急送，1小时达",
            description = "文件、蛋糕、药品急送，下单即配，最快30分钟上门取件。",
            imageUrl = "https://picsum.photos/seed/delivery/800/400",
            channel = "本地", likeCount = 334, collectCount = 63,
            tags = listOf("本地", "性价比"),
            aiInfo = AiInfo(
                summary = "同城急送高效服务，极速送达，适合紧急物品配送需求。",
                features = listOf("1小时达", "最快30分钟取件", "全品类配送"),
                targetUsers = listOf("本地居民", "紧急配送需求者", "上班族"),
                recommendReasons = listOf("配送速度极快", "服务便捷可靠", "性价比高"),
                scenarios = listOf("紧急文件送递", "蛋糕药品配送", "同城急件")
            )
        ),
        AdItem(
            id = "011", type = AdType.LARGE_IMAGE,
            title = "Adidas 跑鞋新款限时特惠",
            description = "Boost缓震技术，马拉松级别支撑，现在购买立享7折。",
            imageUrl = "https://picsum.photos/seed/adidas/800/400",
            channel = "精选", likeCount = 2341, collectCount = 476,
            tags = listOf("运动", "高端", "限时"),
            aiInfo = AiInfo(
                summary = "高端专业跑鞋，Boost缓震科技，限时7折优惠。",
                features = listOf("Boost缓震技术", "马拉松级支撑", "限时7折特惠"),
                targetUsers = listOf("专业跑者", "运动爱好者", "高端跑鞋追求者"),
                recommendReasons = listOf("专业性能拉满", "限时折扣划算", "品牌品质保障"),
                scenarios = listOf("马拉松跑步", "日常训练", "户外慢跑")
            )
        ),
        AdItem(
            id = "012", type = AdType.SMALL_IMAGE,
            title = "京东超市 生鲜配送",
            description = "当日下单次日达，新鲜蔬果直达家门，首单减20元。",
            imageUrl = "https://picsum.photos/seed/jd/400/300",
            channel = "电商", likeCount = 876, collectCount = 159,
            tags = listOf("电商", "本地", "性价比"),
            aiInfo = AiInfo(
                summary = "生鲜极速配送，新鲜直达，首单立减优惠，性价比生鲜采购首选。",
                features = listOf("当日下单次日达", "新鲜蔬果直达", "首单减20元"),
                targetUsers = listOf("居家做饭人群", "本地居民", "性价比采购者"),
                recommendReasons = listOf("配送便捷新鲜", "首单优惠划算", "一站式购齐生鲜"),
                scenarios = listOf("日常买菜", "家庭食材采购", "生鲜囤货")
            )
        ),
        AdItem(
            id = "013", type = AdType.VIDEO,
            title = "索尼 WH-1000XM6 降噪耳机",
            description = "业界顶级主动降噪，30小时续航，学生党专属优惠价。",
            imageUrl = "https://picsum.photos/seed/sony/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商", likeCount = 5421, collectCount = 967,
            tags = listOf("科技", "学生党", "高端"),
            aiInfo = AiInfo(
                summary = "顶级降噪高端耳机，长续航，学生党享专属优惠。",
                features = listOf("顶级主动降噪", "30小时长续航", "学生党专属价"),
                targetUsers = listOf("学生党", "办公人群", "高端数码爱好者"),
                recommendReasons = listOf("降噪效果顶尖", "续航持久耐用", "学生专属福利"),
                scenarios = listOf("学习专注", "办公降噪", "通勤听歌")
            )
        ),
        AdItem(
            id = "014", type = AdType.LARGE_IMAGE,
            title = "本地健身房月卡特惠",
            description = "全器械开放+游泳池+团课无限次，新用户首月仅需99元。",
            imageUrl = "https://picsum.photos/seed/gym/800/400",
            channel = "本地", likeCount = 1087, collectCount = 205,
            tags = listOf("运动", "本地", "学生党"),
            aiInfo = AiInfo(
                summary = "本地全能健身房，新用户超低特惠，适合学生和健身入门人群。",
                features = listOf("全器械开放", "泳池+团课无限次", "新用户99元首月"),
                targetUsers = listOf("学生党", "本地健身人群", "健身入门者"),
                recommendReasons = listOf("价格极低性价比高", "设施齐全", "本地就近健身"),
                scenarios = listOf("日常健身", "减脂塑形", "校园运动补充")
            )
        ),
        AdItem(
            id = "015", type = AdType.SMALL_IMAGE,
            title = "优衣库 UT 联名系列上新",
            description = "全球艺术家联名设计，限量发售，尺码告急中。",
            imageUrl = "https://picsum.photos/seed/uniqlo/400/300",
            channel = "精选", likeCount = 3209, collectCount = 621,
            tags = listOf("时尚", "限时", "学生党"),
            aiInfo = AiInfo(
                summary = "艺术家联名时尚T恤，限量发售，学生党潮流穿搭首选。",
                features = listOf("全球艺术家联名", "限量发售", "潮流设计"),
                targetUsers = listOf("学生党", "潮流穿搭爱好者", "联名收藏者"),
                recommendReasons = listOf("设计独特时尚", "限量款稀缺", "百搭好穿"),
                scenarios = listOf("日常穿搭", "校园出街", "潮流搭配")
            )
        ),
        AdItem(
            id = "016", type = AdType.VIDEO,
            title = "大疆 Osmo Mobile 6 稳定器",
            description = "手机拍摄神器，三轴防抖，一键跟踪，拍出电影级画面。",
            imageUrl = "https://picsum.photos/seed/dji/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商", likeCount = 4102, collectCount = 788,
            tags = listOf("科技", "性价比"),
            aiInfo = AiInfo(
                summary = "专业手机拍摄稳定器，防抖跟踪，高性价比拍摄神器。",
                features = listOf("三轴防抖", "一键智能跟踪", "电影级拍摄效果"),
                targetUsers = listOf("短视频创作者", "摄影爱好者", "居家拍摄人群"),
                recommendReasons = listOf("拍摄效果专业", "操作简单易用", "性价比高"),
                scenarios = listOf("短视频拍摄", "vlog制作", "日常记录")
            )
        ),
        AdItem(
            id = "017", type = AdType.LARGE_IMAGE,
            title = "必胜客新品披萨 买一送一",
            description = "芝士爆浆新口味，堂食外卖同享优惠，限本周末。",
            imageUrl = "https://picsum.photos/seed/pizza/800/400",
            channel = "本地", likeCount = 2876, collectCount = 534,
            tags = listOf("美食", "限时", "本地"),
            aiInfo = AiInfo(
                summary = "新品爆浆披萨限时买一送一，堂食外卖通用，本地美食优选。",
                features = listOf("芝士爆浆新口味", "买一送一", "周末限时优惠"),
                targetUsers = listOf("披萨爱好者", "本地居民", "家庭聚餐人群"),
                recommendReasons = listOf("优惠力度大", "口味新颖", "外卖堂食都便捷"),
                scenarios = listOf("家庭聚餐", "朋友小聚", "周末美食")
            )
        ),
        AdItem(
            id = "018", type = AdType.SMALL_IMAGE,
            title = "亚马逊 Kindle 青春版",
            description = "轻薄护眼电子书阅读器，内置海量书籍，学生党必备神器。",
            imageUrl = "https://picsum.photos/seed/kindle/400/300",
            channel = "电商", likeCount = 1543, collectCount = 319,
            tags = listOf("科技", "学生党", "性价比"),
            aiInfo = AiInfo(
                summary = "轻薄护眼电子书，学生党高性价比学习阅读神器。",
                features = listOf("轻薄便携", "护眼墨水屏", "海量书籍内置"),
                targetUsers = listOf("学生党", "阅读爱好者", "学习备考人群"),
                recommendReasons = listOf("护眼不伤眼", "便携易携带", "性价比超高"),
                scenarios = listOf("学习阅读", "课外看书", "通勤碎片化阅读")
            )
        ),
        AdItem(
            id = "019", type = AdType.LARGE_IMAGE,
            title = "完美日记 新品唇膏系列",
            description = "国货之光，20种色号任选，滋润不脱色，学生党平价首选。",
            imageUrl = "https://picsum.photos/seed/makeup/800/400",
            channel = "精选", likeCount = 6721, collectCount = 1207,
            tags = listOf("时尚", "学生党", "性价比"),
            aiInfo = AiInfo(
                summary = "国货平价唇膏，多色号滋润不脱色，学生党美妆首选。",
                features = listOf("20种色号可选", "滋润不拔干", "持久不脱色"),
                targetUsers = listOf("学生党", "美妆爱好者", "平价彩妆追求者"),
                recommendReasons = listOf("价格亲民性价比高", "色号齐全百搭", "国货品质优秀"),
                scenarios = listOf("日常妆容", "校园美妆", "通勤淡妆")
            )
        ),
        AdItem(
            id = "020", type = AdType.VIDEO,
            title = "特斯拉 Model 3 焕新版",
            description = "续航800km，15分钟快充，现在下定金享专属权益。",
            imageUrl = "https://picsum.photos/seed/tesla/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "精选", likeCount = 18432, collectCount = 3428,
            tags = listOf("科技", "高端"),
            aiInfo = AiInfo(
                summary = "高端智能电动车，长续航快充，定金享专属购车权益。",
                features = listOf("800km长续航", "15分钟快充", "智能驾驶辅助"),
                targetUsers = listOf("高端车主", "科技爱好者", "新能源汽车追求者"),
                recommendReasons = listOf("续航里程顶尖", "充电便捷高效", "专属权益超值"),
                scenarios = listOf("日常通勤", "长途出行", "智能驾驶体验")
            )
        ),
        AdItem(
            id = "021", type = AdType.SMALL_IMAGE,
            title = "饿了么超级会员 首月1元",
            description = "每月12张红包券+免配送费+专属折扣，外卖自由从此开始。",
            imageUrl = "https://picsum.photos/seed/eleme/400/300",
            channel = "本地", likeCount = 4312, collectCount = 816,
            tags = listOf("美食", "本地", "性价比"),
            aiInfo = AiInfo(
                summary = "外卖会员首月1元，红包免邮全覆盖，高性价比点外卖首选。",
                features = listOf("首月1元开通", "12张红包券", "免配送费+专属折扣"),
                targetUsers = listOf("外卖党", "本地居民", "上班族学生党"),
                recommendReasons = listOf("开通成本极低", "省钱力度大", "点外卖更划算"),
                scenarios = listOf("日常外卖", "懒人干饭", "加班点餐")
            )
        ),
        AdItem(
            id = "022", type = AdType.LARGE_IMAGE,
            title = "lululemon 瑜伽裤新色上架",
            description = "面料柔软亲肤，高腰提臀设计，运动日常两相宜。",
            imageUrl = "https://picsum.photos/seed/yoga/800/400",
            channel = "精选", likeCount = 3876, collectCount = 742,
            tags = listOf("运动", "时尚", "高端"),
            aiInfo = AiInfo(
                summary = "高端时尚瑜伽裤，亲肤提臀，运动穿搭两用。",
                features = listOf("柔软亲肤面料", "高腰提臀设计", "新色时尚百搭"),
                targetUsers = listOf("瑜伽爱好者", "时尚运动人群", "高端服饰追求者"),
                recommendReasons = listOf("穿着舒适亲肤", "塑形效果好", "时尚百搭多场景"),
                scenarios = listOf("瑜伽健身", "日常出街", "居家休闲")
            )
        ),
        AdItem(
            id = "023", type = AdType.VIDEO,
            title = "Switch 2 游戏主机正式开售",
            description = "全新性能升级，支持4K输出，首发限量礼包现已开放预购。",
            imageUrl = "https://picsum.photos/seed/switch/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商", likeCount = 22341, collectCount = 4516,
            tags = listOf("科技", "高端", "限时"),
            aiInfo = AiInfo(
                summary = "全新升级游戏主机，4K画质，首发限量礼包预购中。",
                features = listOf("性能全面升级", "4K高清输出", "首发限量礼包"),
                targetUsers = listOf("游戏玩家", "数码爱好者", "高端主机用户"),
                recommendReasons = listOf("性能体验翻倍", "画质顶尖", "限量礼包稀缺"),
                scenarios = listOf("家庭娱乐", "朋友联机", "单人游戏")
            )
        ),
        AdItem(
            id = "024", type = AdType.SMALL_IMAGE,
            title = "周黑鸭 卤味拼盘 新品上市",
            description = "精选食材卤制，香辣鲜嫩，下单满50元包邮到家。",
            imageUrl = "https://picsum.photos/seed/spicy/400/300",
            channel = "本地", likeCount = 987, collectCount = 184,
            tags = listOf("美食", "性价比", "本地"),
            aiInfo = AiInfo(
                summary = "新品卤味拼盘，香辣可口，满额包邮，高性价比休闲零食。",
                features = listOf("精选食材卤制", "香辣鲜嫩口感", "满50元包邮"),
                targetUsers = listOf("卤味爱好者", "休闲零食人群", "本地居民"),
                recommendReasons = listOf("口味正宗好吃", "包邮划算", "性价比高"),
                scenarios = listOf("追剧零食", "朋友聚会", "休闲解馋")
            )
        ),
        AdItem(
            id = "025", type = AdType.LARGE_IMAGE,
            title = "Apple Watch Series 10",
            description = "更薄更轻，血氧心率实时监测，运动健康管理全面升级。",
            imageUrl = "https://picsum.photos/seed/applewatch/800/400",
            channel = "精选", likeCount = 14231, collectCount = 2763,
            tags = listOf("科技", "运动", "高端"),
            aiInfo = AiInfo(
                summary = "高端智能手表，轻薄设计，健康运动全方位监测。",
                features = listOf("轻薄机身设计", "血氧心率实时监测", "健康运动管理"),
                targetUsers = listOf("高端数码用户", "运动爱好者", "健康管理人群"),
                recommendReasons = listOf("佩戴轻薄舒适", "健康功能全面", "品牌高端质感"),
                scenarios = listOf("运动监测", "健康管理", "日常智能穿戴")
            )
        ),
        AdItem(
            id = "026", type = AdType.SMALL_IMAGE,
            title = "网易云音乐黑胶VIP季卡",
            description = "无损音质+独家歌单+歌词翻译，现在购买享8折优惠。",
            imageUrl = "https://picsum.photos/seed/music/400/300",
            channel = "电商", likeCount = 2109, collectCount = 397,
            tags = listOf("性价比", "学生党"),
            aiInfo = AiInfo(
                summary = "音乐会员季卡8折特惠，无损音质，学生党听歌首选。",
                features = listOf("无损音质", "独家歌单", "歌词翻译+8折优惠"),
                targetUsers = listOf("学生党", "音乐爱好者", "性价比追求者"),
                recommendReasons = listOf("价格优惠划算", "听歌体验升级", "功能全面"),
                scenarios = listOf("日常听歌", "学习放松", "通勤音乐")
            )
        ),
        AdItem(
            id = "027", type = AdType.VIDEO,
            title = "戴森 V15 吸尘器旗舰款",
            description = "激光除尘技术，智能感应调节吸力，家居清洁一步到位。",
            imageUrl = "https://picsum.photos/seed/dyson/800/400",
            videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
            channel = "电商", likeCount = 7654, collectCount = 1406,
            tags = listOf("家居", "高端", "科技"),
            aiInfo = AiInfo(
                summary = "高端科技吸尘器，激光除尘智能调节，高效清洁家居。",
                features = listOf("激光除尘技术", "智能感应吸力", "旗舰清洁性能"),
                targetUsers = listOf("高端家居用户", "科技家电爱好者", "居家清洁人群"),
                recommendReasons = listOf("清洁效果顶尖", "智能便捷高效", "品牌品质保障"),
                scenarios = listOf("家庭清洁", "全屋除尘", "宠物家庭除毛")
            )
        ),
        AdItem(
            id = "028", type = AdType.LARGE_IMAGE,
            title = "汉堡王 新品皇堡双层特惠",
            description = "双层牛肉+芝士融合，本周买一赠一，仅限门店自取。",
            imageUrl = "https://picsum.photos/seed/burger/800/400",
            channel = "本地", likeCount = 1432, collectCount = 266,
            tags = listOf("美食", "限时", "本地"),
            aiInfo = AiInfo(
                summary = "双层牛肉皇堡限时买一赠一，门店自取，本地快餐优选。",
                features = listOf("双层牛肉芝士", "买一赠一特惠", "本周限时活动"),
                targetUsers = listOf("汉堡爱好者", "本地居民", "快餐上班族"),
                recommendReasons = listOf("分量足口感好", "买一赠一超划算", "门店自取便捷"),
                scenarios = listOf("工作餐", "快餐简餐", "周末美食")
            )
        ),
        AdItem(
            id = "029", type = AdType.SMALL_IMAGE,
            title = "芒果TV会员 年卡特价",
            description = "热门综艺+独播剧+4K画质，年卡价格直降50元，限时抢购。",
            imageUrl = "https://picsum.photos/seed/mango/400/300",
            channel = "精选", likeCount = 3241, collectCount = 635,
            tags = listOf("限时", "性价比", "学生党"),
            aiInfo = AiInfo(
                summary = "视频会员年卡直降50元，高清独播内容，学生党追剧首选。",
                features = listOf("热门综艺独播", "4K高清画质", "年卡直降50元"),
                targetUsers = listOf("学生党", "追剧爱好者", "综艺粉丝"),
                recommendReasons = listOf("价格直降优惠大", "内容丰富独家", "性价比超高"),
                scenarios = listOf("居家追剧", "综艺观看", "休闲娱乐")
            )
        ),
        AdItem(
            id = "030", type = AdType.LARGE_IMAGE,
            title = "三顿半 冻干咖啡新品礼盒",
            description = "精品咖啡豆萃取，0糖0脂，冷热水均可溶，送礼自用两相宜。",
            imageUrl = "https://picsum.photos/seed/3tops/800/400",
            channel = "电商", likeCount = 5432, collectCount = 1009,
            tags = listOf("美食", "性价比", "电商"),
            aiInfo = AiInfo(
                summary = "精品冻干咖啡礼盒，0糖0脂易溶解，送礼自用都合适。",
                features = listOf("精品咖啡豆萃取", "0糖0脂健康", "冷热水速溶"),
                targetUsers = listOf("咖啡爱好者", "送礼人群", "居家办公人群"),
                recommendReasons = listOf("口感精品优质", "冲泡便捷", "礼盒包装上档次"),
                scenarios = listOf("居家办公", "送礼佳品", "便捷冲泡咖啡")
            )
        )
    )

    fun getByChannel(channel: String): List<AdItem> {
        return allAds.filter { it.channel == channel }
    }
}