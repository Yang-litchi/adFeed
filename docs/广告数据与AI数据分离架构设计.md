# 广告数据与AI数据分离架构设计

> **背景**：本文档基于讨论结论，重新设计广告原始数据与 AI 分析结果的存储与加载架构，替代原有混合方案。

---

## 目录

1. [核心原则](#1-核心原则)
2. [分层架构中的位置](#2-分层架构中的位置)
3. [文件职责划分](#3-文件职责划分)
4. [数据模型定义](#4-数据模型定义)
5. [模块职责与接口](#5-模块职责与接口)
6. [数据流设计](#6-数据流设计)
7. [AIAdRepository 设计](#7-aiadrepository-设计)
8. [降级策略](#8-降级策略)
9. [开发期与完成期切换方案](#9-开发期与完成期切换方案)
10. [与现有文档的冲突与修改建议](#10-与现有文档的冲突与修改建议)

---

## 1. 核心原则

> **data 层只关心广告业务数据，不关心 AI 细节。AI 模块自己管自己的 JSON 解析和 API 调用，不影响 data 层。**

| 原则 | 说明 |
|------|------|
| **关注点分离** | 广告原始数据（title、description、mediaUrl 等）与 AI 分析结果（摘要、标签）物理隔离 |
| **纯净 AdItem** | `AdItem` 定义中不含任何 `aiSummary`/`tags` 字段，是纯粹的广告业务模型 |
| **装饰器增强** | AI 模块通过 `AIEnhancedAd` 包装 `AdItem` + AI 数据，feed/detail 只消费增强后的类型 |
| **无 AI 开关** | 不需要 `BuildConfig.ENABLE_AI`，AI 数据天然"有则增强，无则降级" |
| **可替换数据源** | `AIDataService` 内部实现可从本地 JSON 平滑切换到大模型 API |

---

## 2. 分层架构中的位置

### 2.1 决策：不增加新层级

新增的 `AIAdRepository` 和 `AIDataService` **横跨 Domain 和 Data 两层**，保持原有的 4 层架构不变：

```
┌──────────────────────────────────────────────────────────────┐
│                    Presentation 层                           │
│  feed (FeedFragment/VM)    detail (DetailFragment/VM)       │
│  analytics (StatsFragment/VM)    ai (SearchFragment/VM)     │
│                                                              │
│  消费类型：AIEnhancedAd                                       │
│  依赖接口：AIAdRepository（来自 Domain 层）                    │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────┴───────────────────────────────────┐
│                      Domain 层（纯 Kotlin，接口定义）          │
│                                                              │
│  «interface»                «interface»                      │
│  AdRepository               AIAdRepository                   │
│  + getFeed(...)             + getEnhancedFeed(...)           │
│  + getAdDetail(...)         + getEnhancedDetail(...)         │
│  + getAllAds(...)           + filterByTags(...)              │
│                             + search(...)                    │
│                                                              │
│  «interface»                                                 │
│  AIDataService                                               │
│  + loadAIData(adIds): Map<String, AIData>                    │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────┴───────────────────────────────────┐
│                       Data 层（接口实现）                      │
│                                                              │
│  AdRepositoryImpl          AIAdRepositoryImpl                │
│  ├─ LocalFileDataSource    ├─ AdRepository (注入)            │
│  └─ 读 ad_data.json        ├─ AIDataService (注入)           │
│                             │   ├─ LocalAIDataService        │
│  ChannelRepositoryImpl     │   │   └─ 读 ai_data.json        │
│  InteractionRepositoryImpl │   └─ RemoteAIDataService        │
│  (Room)                    │       └─ 调 API + Room 缓存     │
│                             └─ 产出 AIEnhancedAd              │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────┴───────────────────────────────────┐
│                       Core 层（基础能力）                      │
│  文件读取、图片加载(Coil)、播放器(ExoPlayer)、日志(Timber)     │
└──────────────────────────────────────────────────────────────┘
```

### 2.2 依赖方向

```
Presentation ──→ Domain ←── Data
        ↓                      ↑
   AIEnhancedAd         AIAdRepositoryImpl
  (Domain 数据类型)       (实现 Domain 接口)
```

**关键规则**：
- Presentation 只知道 `AIAdRepository` 接口和 `AIEnhancedAd` 类型，不知道 `AdItem` 的存在
- `AIAdRepository` 接口定义在 Domain 层（纯 Kotlin），对 Presentation 暴露
- `AIAdRepositoryImpl` 在 Data 层实现，持有 `AdRepository` + `AIDataService` 两个依赖
- Data 层通过 `AdRepositoryImpl` 实现 `AdRepository` 接口（依赖反转）
- feed/detail 不直接依赖 `AdRepository`，只依赖 `AIAdRepository`

### 2.3 为什么不是独立第五层

| 考量 | 结论 |
|------|------|
| **项目规模** | 2 周、3 人、~10 个类，五层是过度设计 |
| **概念负担** | 每多一层就要多解释一层，对新人增加认知负荷 |
| **AI 模块的本质** | AI 增强本质上还是"数据获取 + 组装"，是 Data 层的职责延伸，不是全新的架构维度 |
| **接口归属** | AIAdRepository 接口放 Domain 层是合理的（依赖反转），但实现不放单独一层 |

### 2.4 与 Gradle 模块的关系

架构分层 ≠ Gradle 模块划分。`AIAdRepository` 的接口和实现虽然在架构上分属 Domain 和 Data 两层，但在 Gradle 模块层面它们都属于 `ai` 模块：

| Gradle 模块 | 包含的架构层内容 |
|------------|----------------|
| `data` | AdRepository 接口（Domain 层）+ AdRepositoryImpl（Data 层） |
| `ai` | AIAdRepository 接口（Domain 层）+ AIAdRepositoryImpl + AIDataService 实现（Data 层） |

简单说：**Gradle 模块是物理打包方式，分层是逻辑依赖约束，两者不必一一对应。**

---

## 3. 文件职责划分

```
app/src/main/assets/
├── ad_data.json          ← data 层管辖：纯广告业务数据
├── channels.json         ← data 层管辖：频道列表
├── ai_data.json          ← AI 模块管辖：AI 分析结果（开发期预生成，完成期可能删除）
├── images/               ← 共享：广告图片
└── videos/               ← 共享：广告视频
```

### 3.1 `ad_data.json` — 纯广告数据（data 层）

**不包含**任何 AI 相关字段（`aiSummary`、`tags` 等）。

```json
{
  "ads": [
    {
      "id": "ad_001",
      "title": "NIKE Air Max 2024 新款运动跑鞋",
      "description": "全新升级气垫科技，带来前所未有的舒适体验。透气网面设计，适合日常跑步和健身训练。限时8折优惠中。",
      "mediaType": "IMAGE_LARGE",
      "mediaUrl": "file:///android_asset/images/ad_001.jpg",
      "thumbnailUrl": "file:///android_asset/images/ad_001_thumb.jpg",
      "channelId": "recommend",
      "metadata": {
        "advertiser": "NIKE官方旗舰店",
        "price": "¥599",
        "aspectRatio": 1.778
      }
    }
  ]
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String | 唯一标识 |
| `title` | String | 广告标题 |
| `description` | String | 广告原始描述文本 |
| `mediaType` | String | `IMAGE_LARGE` / `IMAGE_SMALL` / `VIDEO` |
| `mediaUrl` | String | 主媒体文件路径（本地 `file:///android_asset/` 路径） |
| `thumbnailUrl` | String | 缩略图路径 |
| `channelId` | String | 所属频道：`recommend` / `ecommerce` / `local` |
| `metadata` | Object | 广告元数据（广告主、价格、宽高比等业务信息） |

### 3.2 `ai_data.json` — AI 分析结果（AI 模块，开发期使用）

**查找表结构**，以 `adId` 为 key，加载为 `Map<String, AIData>`。

```json
{
  "ad_001": {
    "summary": "潮流运动跑鞋，升级气垫科技，限时8折仅需599元",
    "tags": [
      {"id": "tag_sports", "name": "运动", "category": "CATEGORY"},
      {"id": "tag_trendy", "name": "潮流", "category": "STYLE"},
      {"id": "tag_student", "name": "学生党", "category": "AUDIENCE"},
      {"id": "tag_outdoor", "name": "户外", "category": "SCENE"}
    ]
  },
  "ad_002": {
    "summary": "平价蓝牙耳机，长续航低延迟，学生党首选",
    "tags": [
      {"id": "tag_digital", "name": "数码", "category": "CATEGORY"},
      {"id": "tag_affordable", "name": "性价比", "category": "STYLE"},
      {"id": "tag_student", "name": "学生党", "category": "AUDIENCE"}
    ]
  }
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `(key)` | String | adId，与 `ad_data.json` 中的 `id` 对应 |
| `summary` | String | AI 生成的摘要文本（≤ 50 字） |
| `tags[].id` | String | 标签唯一标识 |
| `tags[].name` | String | 标签显示名 |
| `tags[].category` | String | 标签分类：`CATEGORY` / `STYLE` / `AUDIENCE` / `SCENE` |

---

## 4. 数据模型定义

### 4.1 `AdItem` — 纯净广告模型（data 层）

```kotlin
data class AdItem(
    val id: String,
    val title: String,
    val description: String,
    val mediaType: MediaType,
    val mediaUrl: String,
    val thumbnailUrl: String,
    val channelId: String,
    val metadata: AdMetadata,
    val interactionState: InteractionState = InteractionState()
)
```

**关键变化**：移除了 `aiSummary: String? = null` 和 `tags: List<Tag> = emptyList()` 字段。

### 4.2 `AIData` — AI 分析结果（AI 模块）

```kotlin
data class AIData(
    val summary: String,
    val tags: List<Tag>
)
```

### 4.3 `AIEnhancedAd` — 增强后的广告（AI 模块，feed/detail 消费）

```kotlin
data class AIEnhancedAd(
    val adItem: AdItem,
    val summary: String?,      // null 时不展示摘要
    val tags: List<Tag>        // emptyList 时不展示标签
)
```

---

## 5. 模块职责与接口

### 5.1 模块依赖关系

```
feed ──→ ai ──→ data ──→ core
detail ──→ ai ──→ data ──→ core
```

- feed/detail **不直接依赖** data 层的 `AdRepository`
- feed/detail 通过 AI 模块的 `AIAdRepository` 获取增强后的数据
- AI 模块内部依赖 data 层的 `AdRepository`（接口）

### 5.2 `AdRepository`（data 层，简化后）

移除 `searchByKeywords()` 和 `filterByTags()`（这些移到 AI 模块），只保留纯粹的广告数据查询：

```kotlin
interface AdRepository {
    /** 获取指定频道的分页广告数据 */
    fun getFeed(channelId: String, page: Int, pageSize: Int): Flow<Result<PageResponse<AdItem>>>

    /** 根据 ID 获取单条广告详情 */
    fun getAdDetail(adId: String): Flow<Result<AdItem>>

    /** 获取所有广告（供 AI 模块做搜索/过滤） */
    suspend fun getAllAds(channelId: String? = null): List<AdItem>
}
```

### 5.3 `AIDataService`（AI 模块内部，数据源抽象）

```kotlin
interface AIDataService {
    /** 批量获取 AI 数据，返回 adId → AIData 映射 */
    suspend fun loadAIData(adIds: List<String>): Map<String, AIData>
}
```

**两种实现**：

```kotlin
// 开发期：读本地 JSON
class LocalAIDataService(private val context: Context) : AIDataService {
    private val cache: Map<String, AIData> by lazy {
        // 从 assets/ai_data.json 加载，Moshi 解析为 Map<String, AIData>
        // 文件缺失 → 返回 emptyMap()
    }
    override suspend fun loadAIData(adIds: List<String>): Map<String, AIData> {
        return adIds.mapNotNull { id -> cache[id]?.let { id to it } }.toMap()
    }
}

// 完成期：调用大模型 API + Room 缓存
class RemoteAIDataService(
    private val api: AIModelAPI,
    private val cache: AICacheDao
) : AIDataService {
    override suspend fun loadAIData(adIds: List<String>): Map<String, AIData> {
        // 1. 查 Room 缓存
        // 2. 未命中的发 API 请求
        // 3. 结果写入 Room 缓存
        // 4. 返回合并结果
    }
}
```

### 5.4 `AIAdRepository`（AI 模块对外暴露的唯一数据接口）

```kotlin
interface AIAdRepository {
    /** 获取增强后的分页 Feed 数据 */
    fun getEnhancedFeed(channelId: String, page: Int, pageSize: Int): Flow<Result<PageResponse<AIEnhancedAd>>>

    /** 获取增强后的单条广告详情 */
    fun getEnhancedDetail(adId: String): Flow<Result<AIEnhancedAd>>

    /** 按标签过滤（在 AI 数据层面匹配） */
    suspend fun filterByTags(tags: List<String>, channelId: String? = null): List<AIEnhancedAd>

    /** 对话式搜索（优先 AI 语义匹配，降级为关键词匹配） */
    suspend fun search(query: String, channelId: String? = null): List<AIEnhancedAd>
}
```

### 5.5 不再需要的接口

以下接口从原设计中被移除：

| 移除的接口 | 原因 |
|-----------|------|
| `IAISummaryService` | 摘要获取整合到 `AIDataService` + `AIAdRepository` 内部 |
| `IAITagService` | 标签获取整合到 `AIDataService` + `AIAdRepository` 内部 |
| `NoOpSummaryService` | 降级逻辑在 `AIAdRepositoryImpl` 内部统一处理 |
| `NoOpTagService` | 同上 |
| `BuildConfig.ENABLE_AI` | AIDataService 天然可缺失，无需开关 |

### 5.6 保留的接口

| 保留的接口 | 说明 |
|-----------|------|
| `IAISearchService` | 搜索有独立的 UI 页面，仍需接口抽象，但降级也是关键词匹配而非 NoOp |

---

## 6. 数据流设计

### 6.1 信息流加载流程

```
FeedViewModel
  │
  └─ AIAdRepository.getEnhancedFeed(channelId, page, pageSize)
       │
       ├─ 1. AdRepository.getFeed() → List<AdItem>
       │
       ├─ 2. 提取所有 adId → AIDataService.loadAIData(adIds) → Map<String, AIData>
       │     ├─ ai_data.json 缺失 → emptyMap()
       │     ├─ JSON 解析失败 → emptyMap() + Timber.w
       │     └─ 成功 → Map<String, AIData>
       │
       ├─ 3. 逐条包装为 AIEnhancedAd
       │     for each AdItem:
       │       if (aiDataMap.containsKey(adItem.id))
       │         → AIEnhancedAd(adItem, aiData.summary, aiData.tags)
       │       else
       │         → AIEnhancedAd(
       │             adItem,
       │             summary = adItem.description.take(50) + "...",
       │             tags = emptyList()
       │           )
       │
       └─ 4. 分页返回 PageResponse<AIEnhancedAd>
```

### 6.2 详情页加载流程

```
DetailViewModel
  │
  └─ AIAdRepository.getEnhancedDetail(adId)
       │
       ├─ 1. AdRepository.getAdDetail(adId) → AdItem
       │
       ├─ 2. AIDataService.loadAIData(listOf(adId)) → Map<String, AIData>
       │
       └─ 3. 包装为 AIEnhancedAd（降级逻辑同上）
```

### 6.3 搜索流程

```
SearchViewModel
  │
  └─ AIAdRepository.search(query, channelId)
       │
       ├─ AI 版本（ai_data.json 存在或 API 可用）：
       │   1. AdRepository.getAllAds() → 所有 AdItem
       │   2. AIDataService 获取所有 AI 数据
       │   3. 在 summary + tags 中做语义/关键词匹配
       │   4. 返回匹配的 AIEnhancedAd 列表
       │
       └─ 降级版本（无 AI 数据）：
           1. AdRepository.getAllAds() → 所有 AdItem
           2. 在 title + description 中做子串匹配
           3. 返回匹配的 AIEnhancedAd（summary 降级为 description 截断）
```

### 6.4 标签过滤流程

```
FeedViewModel
  │
  └─ AIAdRepository.filterByTags(tags, channelId)
       │
       ├─ 1. AdRepository.getAllAds(channelId) → 所有 AdItem
       │
       ├─ 2. AIDataService 获取 AI 数据
       │
       ├─ 3. 在 AIEnhancedAd.tags 中做交集匹配
       │     selectedTags.all { tag → aiEnhancedAd.tags.any { it.name == tag } }
       │
       └─ 4. 返回匹配列表
```

---

## 7. AIAdRepository 实现

```kotlin
class AIAdRepositoryImpl(
    private val adRepository: AdRepository,
    private val aiDataService: AIDataService
) : AIAdRepository {

    override fun getEnhancedFeed(
        channelId: String, page: Int, pageSize: Int
    ): Flow<Result<PageResponse<AIEnhancedAd>>> = flow {
        adRepository.getFeed(channelId, page, pageSize).collect { result ->
            result.onSuccess { pageResponse ->
                val enhancedItems = enhanceItems(pageResponse.items)
                emit(Result.success(pageResponse.copy(items = enhancedItems)))
            }.onFailure {
                emit(Result.failure(it))
            }
        }
    }

    override fun getEnhancedDetail(adId: String): Flow<Result<AIEnhancedAd>> = flow {
        adRepository.getAdDetail(adId).collect { result ->
            result.onSuccess { adItem ->
                val enhanced = enhanceItems(listOf(adItem)).first()
                emit(Result.success(enhanced))
            }.onFailure {
                emit(Result.failure(it))
            }
        }
    }

    override suspend fun filterByTags(
        tags: List<String>, channelId: String?
    ): List<AIEnhancedAd> {
        val allAds = adRepository.getAllAds(channelId)
        val enhanced = enhanceItems(allAds)
        return enhanced.filter { ad ->
            tags.all { tag -> ad.tags.any { it.name == tag } }
        }
    }

    override suspend fun search(
        query: String, channelId: String?
    ): List<AIEnhancedAd> {
        val allAds = adRepository.getAllAds(channelId)
        val enhanced = enhanceItems(allAds)
        val keywords = query.trim().split("\\s+".toRegex())
        return enhanced.filter { ad ->
            val searchText = buildString {
                append(ad.adItem.title)
                append(" ")
                append(ad.adItem.description)
                ad.summary?.let { append(" ").append(it) }
                ad.tags.forEach { append(" ").append(it.name) }
            }
            keywords.any { kw -> searchText.contains(kw, ignoreCase = true) }
        }
    }

    // ===== 私有方法 =====

    private suspend fun enhanceItems(items: List<AdItem>): List<AIEnhancedAd> {
        val adIds = items.map { it.id }
        val aiDataMap = try {
            aiDataService.loadAIData(adIds)
        } catch (e: Exception) {
            Timber.w(e, "AI 数据加载失败，使用降级方案")
            emptyMap()
        }
        return items.map { adItem ->
            val aiData = aiDataMap[adItem.id]
            AIEnhancedAd(
                adItem = adItem,
                summary = aiData?.summary ?: adItem.description.take(50) + "...",
                tags = aiData?.tags ?: emptyList()
            )
        }
    }
}
```

---

## 8. 降级策略

所有降级在 `AIAdRepositoryImpl.enhanceItems()` 内部统一处理，feed/detail 无感知：

| 失效场景 | 表现 | 用户感知 |
|---------|------|---------|
| `ai_data.json` 文件缺失 | `AIDataService.loadAIData()` 返回 `emptyMap()` | 摘要 = description 截断，无标签 |
| `ai_data.json` JSON 解析失败 | catch 异常，返回 `emptyMap()` + Timber.w 日志 | 同上 |
| 某条广告无对应 AI 数据 | `aiDataMap[adId]` 返回 null | 同上 |
| 大模型 API 调用失败（完成期） | 查 Room 缓存 → 缓存未命中 → 降级 | 同上 |
| 网络不可达（完成期） | 同上 | 同上 |

**降级后的 `AIEnhancedAd` 始终有效**，不存在 null 或异常传播。

---

## 9. 开发期与完成期切换方案

### 9.1 切换点

唯一切换点在 `AppContainer`（手工 DI 容器）：

```kotlin
// app 模块 — AppContainer.kt

// 开发期：本地 JSON
private val aiDataService: AIDataService = LocalAIDataService(context)

// 完成期：切一行代码
// private val aiDataService: AIDataService = RemoteAIDataService(aiModelAPI, aiCacheDao)

// AIAdRepository（始终用同一个实现）
val aiAdRepository: AIAdRepository = AIAdRepositoryImpl(adRepository, aiDataService)
```

feed/detail 模块的 ViewModel 工厂改为注入 `AIAdRepository` 而非 `AdRepository`：

```kotlin
fun createFeedViewModel() = FeedViewModel(
    aiAdRepository, channelRepository, interactionRepository, analyticsRepository
)
fun createDetailViewModel(adId: String) = DetailViewModel(
    aiAdRepository, interactionRepository, analyticsRepository, adId
)
fun createSearchViewModel() = SearchViewModel(aiAdRepository)
```

### 9.2 开发流程

```
Day 1-2: 用 ad_data.json 开发核心流程，此时 AI 数据为空，全部走降级
         → 信息流正常展示（summary = description 截断，无标签）

Day 3-4: 用大模型网页版逐条生成摘要+标签 → 整理为 ai_data.json
         → LocalAIDataService 自动加载，信息流出现 AI 摘要和标签

Day 5+:  标签过滤、对话搜索等功能上线

完成期:   切 RemoteAIDataService，接入大模型 API
```

---

## 10. 与现有文档的冲突与修改建议

以下列出三份现有文档中与新架构冲突的具体位置。**仅在得到明确指示后才修改这些文件。**

### 10.1 《模块划分与详细设计.md》

| 位置 | 冲突内容 | 需改为 |
|------|---------|--------|
| **§4.4 AdItem 数据模型** | 包含 `aiSummary: String? = null` 和 `tags: List<Tag> = emptyList()` 字段 | 移除这两个字段 |
| **§4.5 ad_data.json 示例** | JSON 中包含 `tags` 数组和 `aiSummary` 字符串 | 移除这两个字段 |
| **§4.2 AdRepository 接口** | 包含 `searchByKeywords()` 和 `filterByTags()` 方法 | 移除这两个方法，新增 `getAllAds()` |
| **§2.2 AppContainer 代码** | 创建 `IAISummaryService`, `IAITagService`, `IAISearchService` 实例，使用 `BuildConfig.ENABLE_AI` | 改为创建 `AIDataService` + `AIAdRepository`，移除 `BuildConfig.ENABLE_AI` |
| **§2.2 ViewModel 工厂方法** | `createFeedViewModel()` 接收 `aiTagService` 参数 | 改为接收 `aiAdRepository` |
| **§7 播放器模块** | 无冲突 | 无需修改 |
| **§8 AI 模块全部内容** | 描述了 `IAISummaryService`/`IAITagService`/`IAISearchService` + NoOp 模式 | 改为 `AIDataService` + `AIAdRepository` 模式，移除 NoOp 实现 |
| **§9 埋点统计模块** | 无冲突 | 无需修改 |
| **§10.1 模块依赖矩阵** | ai 模块依赖关系与旧设计一致（功能上不影响） | 无需修改 |
| **§10.2 关键接口契约表** | 列出 `IAISummaryService`、`IAITagService` | 替换为 `AIDataService`、`AIAdRepository` |
| **附录D Git 提交格式** | 无冲突 | 无需修改 |

### 10.2 《整体方案设计.md》

| 位置 | 冲突内容 | 需改为 |
|------|---------|--------|
| **§3.2 模块划分表** | ai 模块职责描述为"本地AI数据预处理、标签过滤、对话式搜索、AI结果缓存" | 改为"AI数据加载/缓存、AI增强包装、标签过滤、对话式搜索"，职责边界更清晰 |
| **§6.1 接口抽象层** | 展示 `IAISummaryService`/`IAITagService`/`IAISearchService` + NoOp 降级图 | 替换为 `AIDataService` + `AIAdRepository` 架构图 |
| **§6.2 手工DI绑定** | 使用 `BuildConfig.ENABLE_AI` 开关 + NoOp 实现 | 改为 `AIDataService` 的两种实现（`LocalAIDataService` / `RemoteAIDataService`），移除 `BuildConfig.ENABLE_AI` |
| **§6.3 降级方案表** | AI摘要降级为"预置静态摘要数据或展示原始 description"；AI标签降级为"预置静态标签数据" | 改为：摘要降级 = description 截断；标签降级 = emptyList |
| **§7.1 单向数据流图** | 无直接冲突，但 UseCase 层需要感知 AI 增强 | 建议更新数据流图，在 UseCase 和 AdRepository 之间加入 AIAdRepository |
| **§9.4 AI输出约束** | 同时描述了嵌入 ad_data.json 和独立 ai_data.json 两种方式 | 只保留独立 ai_data.json 方式，移除嵌入方案 |
| **§9.6 播放器复用** | 无冲突 | 无需修改 |

### 10.3 《技术选型理由与优势说明.md》

| 位置 | 冲突内容 | 需改为 |
|------|---------|--------|
| **全文** | 主要涉及技术库选型，不涉及数据文件结构的细节设计 | **无冲突**，无需修改 |

### 10.4 无冲突的部分（确认无需修改）

以下内容与新架构**完全兼容**，无需任何更改：

- `player` 模块全部设计
- `analytics` 模块全部设计
- `core` 模块全部设计
- `feed` 模块的 RecyclerView、卡片策略、Tab 切换、刷新/加载更多逻辑
- `detail` 模块的 UI 结构、动画、互动操作
- Room 数据库设计（interaction 表、track_events 表）
- 跨页面状态同步方案（SharedFlow）
- 曝光统计口径
- 播放器池设计
- Git 分支策略与开发排期
- 团队分工（人员职责不变）
- 技术选型全部决策（Kotlin、Room、Coil、ExoPlayer 等）
- toast/snackbar 等通用 UI 组件

---

## 附录A：新旧架构对比图

### 旧架构（嵌入 + NoOp 模式）

```
IAISummaryService (if ENABLE_AI → 真实现, else → NoOp)
IAITagService     (同上)
IAISearchService  (同上)

feed → AdRepository → AdItem (含 aiSummary, tags)
     → IAITagService (可选)

detail → AdRepository → AdItem
```

### 新架构（分离 + AIAdRepository 统一入口）

```
feed ──→ AIAdRepository ──→ AdRepository ──→ AdItem (纯净)
detail ──→                ──→ AIDataService ──→ Map<String, AIData>
                              │
                              └── 包装 → AIEnhancedAd
```

---

## 附录B：关键决策记录

| 决策 | 结论 | 日期 |
|------|------|------|
| 广告与 AI 数据是否分文件 | 分，`AdItem` 不含任何 AI 字段 | 2026-06-05 |
| AI 数据文件格式 | 查找表 `{adId: {summary, tags}}` | 2026-06-05 |
| 缺失 AI 数据的降级 | summary = description 截断，tags = emptyList | 2026-06-05 |
| 增强方式 | `AIAdRepository` 封装，直接产出 `AIEnhancedAd` | 2026-06-05 |
| 数据源可替换 | 开发期 `LocalAIDataService`，完成期 `RemoteAIDataService` | 2026-06-05 |
| 是否需要 `BuildConfig.ENABLE_AI` | 不需要，天然降级 | 2026-06-05 |
