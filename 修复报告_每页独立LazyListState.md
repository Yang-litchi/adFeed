# 修复报告：每页独立 LazyListState

## 问题

Feed 列表中广告曝光统计不生效。广告在可见面积 ≥50% 并停留数秒后，曝光计数无变化。

## 根因

`HorizontalPager` 的 3 个页面各有一个 `LazyColumn`，但共享同一个 `LazyListState`。`HorizontalPager` 默认预组合相邻页面，离屏页面的 `LazyColumn` 执行布局时会覆写共享的 `layoutInfo`。当离屏 LazyColumn 的布局结果与可见页面不一致时，`visibleFractionMap` 拿到错误数据，导致 `ExposureDetector` 始终收到 `visibleFraction = 0`，曝光条件永远不满足。

## 修复方案

将 `listState` 和 `visibleFractionMap` 从 `FeedScreen` 作用域移入 `HorizontalPager` 每页的内容 lambda 内部，使每个页面的 `LazyColumn` 拥有独立的 `LazyListState` 和可见性计算。

## 修改文件

**`app/src/main/java/com/example/adfeed/ui/feed/FeedScreen.kt`** — 3 处编辑

### 编辑 1：删除共享 `listState`（原第 56 行）

```diff
  var customTagInput by remember { mutableStateOf("") }
- val listState = rememberLazyListState()
```

### 编辑 2：删除共享 `visibleFractionMap`（原第 94–110 行）

```diff
- // 基于 LazyListState 计算每个可见 item 的可见面积比例
- // item.offset 为 item 顶部相对于 viewport 顶部的像素偏移（可为负值）
- val visibleFractionMap by remember {
-     derivedStateOf {
-         val viewportHeight = listState.layoutInfo.viewportSize.height
-         if (viewportHeight == 0) return@derivedStateOf emptyMap<String, Float>()
-         listState.layoutInfo.visibleItemsInfo.associate { info ->
-             val itemTop = info.offset
-             val itemBottom = info.offset + info.size
-             val visibleTop = maxOf(itemTop, 0)
-             val visibleBottom = minOf(itemBottom, viewportHeight)
-             val visibleHeight = maxOf(0, visibleBottom - visibleTop)
-             val fraction = if (info.size > 0) visibleHeight.toFloat() / info.size else 0f
-             (info.key as? String ?: "") to fraction
-         }
-     }
- }
-
  val pullRefreshState = rememberPullRefreshState(
```

### 编辑 3：在 `HorizontalPager` 每页内新增独立的 `listState` + `visibleFractionMap`（新增于第 188–206 行）

```diff
  HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxSize(),
      userScrollEnabled = true
  ) {
+     // 每个页面独立的 LazyListState，避免跨页面共享 layoutInfo 导致曝光统计失效
+     val listState = rememberLazyListState()
+
+     // 基于当前页面的 LazyListState 计算每个可见 item 的可见面积比例
+     val visibleFractionMap by remember {
+         derivedStateOf {
+             val viewportHeight = listState.layoutInfo.viewportSize.height
+             if (viewportHeight == 0) return@derivedStateOf emptyMap<String, Float>()
+             listState.layoutInfo.visibleItemsInfo.associate { info ->
+                 val itemTop = info.offset
+                 val itemBottom = info.offset + info.size
+                 val visibleTop = maxOf(itemTop, 0)
+                 val visibleBottom = minOf(itemBottom, viewportHeight)
+                 val visibleHeight = maxOf(0, visibleBottom - visibleTop)
+                 val fraction = if (info.size > 0) visibleHeight.toFloat() / info.size else 0f
+                 (info.key as? String ?: "") to fraction
+             }
+         }
+     }
+
      Box(
```

## 修改前后架构对比

```
修改前:                                    修改后:
════════                                   ════════
FeedScreen                                FeedScreen
  listState ← (1个, 3页共享)                (无共享 LazyListState)
  visibleFractionMap ← (1个, 3页共享)        (无共享 visibleFractionMap)
  └─ HorizontalPager                       └─ HorizontalPager
       ├─ Page 0: LazyColumn(state) ←┐           ├─ Page 0: listState₀, fracMap₀
       ├─ Page 1: LazyColumn(state) ←┤ 共享         LazyColumn(state₀)
       └─ Page 2: LazyColumn(state) ←┘           ├─ Page 1: listState₁, fracMap₁
                                                        LazyColumn(state₁)
                                                  └─ Page 2: listState₂, fracMap₂
                                                        LazyColumn(state₂)
```

## 影响范围

| 组件 | 是否改动 | 说明 |
|------|:---:|------|
| `ExposureDetector` | ❌ 未改动 | 仍从 `visibleFractionMap` 读取，现在是本页独立 Map |
| `AdCardDispatcher` | ❌ 未改动 | 卡片渲染逻辑无变化 |
| `FeedViewModel.recordExposure` | ❌ 未改动 | 曝光写入逻辑无变化 |
| `ExposureTracker` | ❌ 未改动 | 全局会话去重仍跨页面生效 |
| `FeedViewModel.recordClick` | ❌ 未改动 | 点击统计无变化 |
| `AiFloatingBall` | ❌ 未改动 | AI 搜索点击统计无变化 |
| `DetailScreen` | ❌ 未改动 | 详情页曝光追踪无变化 |
| 频道切换 (`switchChannel`) | ❌ 未改动 | 频道切换逻辑无变化 |
| 下拉刷新 (`pullRefresh`) | ❌ 未改动 | 刷新逻辑无变化 |
| 标签筛选 (`selectedTags`) | ❌ 未改动 | 筛选逻辑无变化 |

## 验证要点

1. **曝光统计恢复**：Feed 中广告 ≥50% 可见并停留 1 秒后，曝光数正确增加
2. **会话去重正常**：同一广告不会重复计数（`ExposureTracker` 跨页面生效）
3. **频道切换正常**：切换「精选/电商/本地」后，各自频道独立滚动位置
4. **点击统计正常**：Feed 卡片点击、AI 悬浮球点击均正常计入
5. **页面间隔离**：滚动页面 0 不影响页面 1 的布局状态
