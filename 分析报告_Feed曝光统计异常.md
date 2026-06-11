# Feed 曝光统计异常分析报告

## 问题描述

用户观察到：Feed 列表中某广告初始可见面积 <50%，滚动后达到 >50% 可见并等待数秒后，曝光统计数据与之前相比没有变化。

## 排查范围

对整个曝光统计链路进行了逐层审查：

```
LazyListState → visibleFractionMap → ExposureDetector → FeedViewModel.recordExposure
                                                              ├─ _uiState (内存)
                                                              └─ recordStatEvent → Room DB
```

---

## 发现的问题

### 🔴 问题一（严重）：HorizontalPager + 共享 LazyListState 导致 layoutInfo 不可靠

**文件**: `ui/feed/FeedScreen.kt` 第 56 行 + 第 202–214 行

**代码**：

```kotlin
val listState = rememberLazyListState()       // ← 第 56 行：仅创建 1 个

HorizontalPager(state = pagerState, ...) {     // ← 3 个页面
    Box(...) {
        LazyColumn(state = listState, ...) {   // ← 3 个 LazyColumn，共用 1 个 state
            items(...) { ad -> ExposureDetector(...) }
        }
    }
}
```

**根因分析**：

`HorizontalPager` 默认会预组合相邻页面（`beyondViewportPageCount` 默认值 ≥ 1）。这意味着当用户在「精选」页面时，「电商」页面的 `LazyColumn` 也会被组合并执行布局。

**三个 LazyColumn 共享一个 `LazyListState`**。`listState.layoutInfo` 会被**最后一个执行布局的 LazyColumn 覆盖**。

正常情况下，可见页面和离屏页面有相同的 `filteredAds` 和滚动位置，布局结果一致。但在以下场景中会产生差异：

| 场景 | 可见页面 LazyColumn | 离屏页面 LazyColumn | 最终 layoutInfo |
|------|---------------------|---------------------|:---:|
| **稳态（同一频道）** | 正确布局，viewport = 屏幕高度 | 同样正确布局 | ✅ 一致 |
| **频道切换中** | 新频道数据已加载 | 可能仍持有旧频道数据（或空数据） | ❌ 不确定 |
| **数据加载中 (isLoading)** | 可见广告列表完整 | 离屏 LazyColumn 延迟组合，viewport 可能为 0 | ❌ 可能为空 |
| **初次组合** | 正常布局 | 先被组合（预组合顺序），后布局可能写入空 layoutInfo | ❌ 可能为空 |

关键问题在于**布局顺序的不确定性**：如果离屏页面的 LazyColumn **后执行布局**且其 `layoutInfo` 与可见页面不同，`visibleFractionMap` 就拿到了错误的数据。当 `visibleFractionMap` 为空或全是 0 时，所有 `ExposureDetector` 的 `visibleFraction = 0`，`meetsThreshold = false`，曝光永远不会触发。

**具体到 `visibleFractionMap` 的连锁反应**：

```kotlin
// FeedScreen.kt:96-110
val visibleFractionMap by remember {
    derivedStateOf {
        val viewportHeight = listState.layoutInfo.viewportSize.height
        // 如果被离屏 LazyColumn 覆盖为 0 → 返回空 Map
        if (viewportHeight == 0) return@derivedStateOf emptyMap<String, Float>()
        // ↑ 一旦走到这里，所有广告的 visibleFraction 都是 0（map 中找不到 key）
        ...
    }
}

// FeedScreen.kt:237-241
ExposureDetector(
    visibleFraction = visibleFractionMap[ad.id] ?: 0f,  // ← 永远为 0
    ...
)
```

**验证方法**：在 `ExposureDetector` 中添加日志打印 `visibleFraction` 值，观察是否存在一直为 0 的情况。

### 🟡 问题二（次要）：Feed 卡片不展示曝光数

**文件**: `LargeImageCard.kt`, `SmallImageCard.kt`, `VideoCard.kt`

三个卡片组件均未渲染 `ad.exposureCount` 或 `ad.clickCount`。`recordExposure` 虽然正确更新了 `_uiState.ads` 中的内存值，但**用户在 Feed 卡片上看不到任何变化**。

用户只能通过以下方式验证：
- 导航到**统计页面**（`StatisticsScreen`），从 Room DB 读取
- 直接查看 Room 数据库

这导致用户无法在 Feed 中直观感知到曝光已被成功记录，可能误判为"统计未生效"。

### 🟢 排查结论：以下环节无问题

| 环节 | 文件 | 排查结论 |
|------|------|:---:|
| `visibleFractionMap` 计算逻辑 | FeedScreen.kt:96-110 | ✅ 算法正确 |
| `ExposureDetector` 计时+判定 | ExposureDetector.kt | ✅ LaunchedEffect key 设计正确 |
| `ExposureTracker` 会话去重 | ExposureTracker.kt | ✅ 单例+Set 实现正确 |
| `recordExposure` 内存更新 | FeedViewModel.kt:269-279 | ✅ StateFlow.update 正确 |
| `recordStatEvent` DB 持久化 | FeedViewModel.kt:283-298 | ✅ 写入逻辑正确 |
| Room DAO 查询 | StatisticEventDao.kt | ✅ SQL 查询正确 |
| `observeDatabaseChanges` 不覆盖曝光数 | FeedViewModel.kt:122-141 | ✅ 保留 exposureCount |

---

## 修复建议

### 修复一（推荐）：每页独立 LazyListState

将 `rememberLazyListState()` 移入 `HorizontalPager` 的 content lambda 内，并为每个页面独立维护 `visibleFractionMap`。

**修改范围**：`FeedScreen.kt`
- `listState` 从 FeedScreen 作用域移入 HorizontalPager 作用域
- `visibleFractionMap` 同步移入
- 或改为在 `ExposureDetector` 所在 item 内部使用 `Modifier.onGloballyPositioned` 获取可见比例

### 修复二（备选）：禁用 HorizontalPager 预组合

```kotlin
val pagerState = rememberPagerState(
    initialPage = 0,
    pageCount = { 3 },
    beyondViewportPageCount = 0  // ← 禁止预组合离屏页面
)
```

**优点**：改动最小，一行代码
**缺点**：滑动切换页面时可能出现短暂白屏（离屏页面需即时组合渲染）

### 修复三：Feed 卡片增加曝光数展示

在卡片组件中增加 `exposureCount` 的显示（可选），让用户能直观确认曝光是否被记录。

---

## 总结

| 问题 | 严重程度 | 影响 |
|------|:---:|------|
| HorizontalPager 共享 LazyListState 导致 layoutInfo 不可靠 | 🔴 严重 | 曝光完全无法触发 |
| Feed 卡片不展示曝光数 | 🟡 次要 | 用户无法直观确认统计生效 |
| 其余曝光链路代码（计算/判定/去重/持久化）| 🟢 正常 | 无问题 |
