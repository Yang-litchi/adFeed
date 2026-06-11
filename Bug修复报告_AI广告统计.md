# Bug 修复报告：AI 问答结果中广告点击/曝光统计缺失

## 问题描述

在主界面的 AI 问答结果中，广告链接点击后能够正确跳转到对应广告详情页，但通过该入口进入详情页时，**未正确统计广告的曝光数（Exposure）和点击数（Click）**。

## Bug 原因分析

经过对广告点击/曝光统计全链路的追踪，发现 **两个缺失环节** 导致 AI 问答结果中广告的统计失效。

### Bug 1：AI 悬浮球搜索结果点击未记录 Click 事件

**文件**: `app/src/main/java/com/example/adfeed/ui/feed/FeedScreen.kt`（原第 423 行）

```
                   Feed 卡片点击 ✅                    AI 悬浮球点击 ❌
                   ════════════════                    ════════════════
FeedScreen.kt:     viewModel.recordClick(ad.id)         (无 recordClick)
                   → onAdClick(ad)                      → onAdClick(ad)

MainActivity.kt:   navController.navigate("detail/...") navController.navigate("detail/...")
```

- **Feed 卡片**（`FeedScreen.kt:247-248`）：`onCardClick` 回调中先调用 `viewModel.recordClick(ad.id)` 再触发导航
- **AI 悬浮球**（`FeedScreen.kt` 原第 423 行）：`AiFloatingBall(onAdClick = onAdClick)` 直接使用了 `MainActivity` 传入的原始回调（仅做导航 `navController.navigate("detail/${ad.id}")`），**缺少 `viewModel.recordClick(ad.id)` 调用**

### Bug 2：进入详情页后完全无曝光追踪

**文件**: `app/src/main/java/com/example/adfeed/ui/detail/DetailScreen.kt`

| 页面 | 曝光追踪机制 | 状态 |
|------|-------------|:----:|
| Feed 列表 | `ExposureDetector`（≥50%可见 + 持续1秒 + `ExposureTracker` 会话去重） | ✅ |
| 详情页 | **无任何曝光追踪** | ❌ |

- **Feed 列表**：每个广告项通过 `ExposureDetector` 组件实现完整的曝光判定逻辑
- **DetailScreen**：既没有使用 `ExposureDetector`，也没有调用 `viewModel.recordExposure()`，导致从 AI 搜索结果入口进入详情页时曝光数始终不被统计

---

## 修复内容（2 处修改，共约 10 行代码）

### 修改 1：`FeedScreen.kt` — AI 悬浮球点击记录 Click

**文件**: `app/src/main/java/com/example/adfeed/ui/feed/FeedScreen.kt`  
**位置**: 第 423 行

```diff
-        AiFloatingBall(onAdClick = onAdClick)
+        AiFloatingBall(onAdClick = { ad ->
+            viewModel.recordClick(ad.id)
+            onAdClick(ad)
+        })
```

AI 悬浮球中点击广告时，**先调用 `viewModel.recordClick(ad.id)` 记录点击事件**，再触发导航，与 Feed 卡片点击行为保持一致。

### 修改 2：`DetailScreen.kt` — 详情页曝光追踪

**文件**: `app/src/main/java/com/example/adfeed/ui/detail/DetailScreen.kt`

**2a. 添加 import**（第 29 行）：

```diff
+ import com.example.adfeed.ui.components.ExposureDetector
  import com.example.adfeed.ui.components.LikeButton
```

**2b. 添加 ExposureDetector**（第 108-114 行，在 `Scaffold` 内容 `LazyColumn` 之前）：

```diff
+        // 曝光统计：详情页内容默认100%可见，1秒后计入曝光（单会话去重）
+        // 与 Feed 列表使用相同的 ExposureDetector + ExposureTracker 规则
+        ExposureDetector(
+            adId = adItem.id,
+            visibleFraction = 1f,
+            onExposed = { viewModel.recordExposure(adItem.id) }
+        )
```

详情页使用与 Feed 列表**完全相同的曝光判定机制**：
- **`visibleFraction = 1f`**：详情页内容默认 100% 可见
- **1 秒计时**：与 Feed 列表一致的 `durationMs = 1000L`
- **`ExposureTracker` 会话去重**：同一广告单次会话仅记录 1 次有效曝光，跨页面（Feed → Detail）共享

---

## 验收标准验证

| 验收标准 | 状态 | 说明 |
|----------|:----:|------|
| 点击 AI 问答结果中的广告链接后，点击数正确增加 | ✅ | `recordClick` 已加入 AI悬浮球 `onAdClick` 回调 |
| 进入广告详情页后，曝光数按现有规则正确增加 | ✅ | `ExposureDetector` 已加入 `DetailScreen` |
| 其他广告入口的统计逻辑不受影响 | ✅ | Feed 卡片点击/曝光逻辑完全未改动 |
| 无重复计数问题 | ✅ | `ExposureTracker` 全局单例跨页面去重，同一广告单次会话仅允许 1 次有效曝光 |

### 去重机制说明

```
用户行为                              ExposureTracker 状态         结果
─────────────────────────────────────────────────────────────────────────────
在 Feed 中看到广告 A >=1秒            markExposed("A")            曝光计1次
→ 点击进入详情页                      isExposed("A") == true      详情页跳过曝光

从 AI 搜索直接点击广告 B（Feed 中未曝光） isExposed("B") == false   详情页等1秒后曝光计1次
→ 返回 Feed，广告 B 进入可视区         isExposed("B") == true      Feed 跳过曝光
```

两个入口的曝光统计通过 `ExposureTracker` 全局单例实现 **跨页面协调**，确保同一广告在任何入口组合下都不会重复计数。
