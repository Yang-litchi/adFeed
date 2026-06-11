# 说明：AI 悬浮球点击为何只记录 Click 而不记录 Exposure

## 结论

AI 悬浮球中**只记录点击、不记录曝光是正确的设计**，不是 bug。

## 核心原因

点击（Click）和曝光（Exposure）是两个独立且职责不同的统计事件：

| 事件 | 含义 | 触发条件 | 性质 |
|------|------|----------|------|
| **Exposure** | 广告被用户「看到」 | 可见面积 ≥50% + 持续 1 秒 | 被动、基于可见性 |
| **Click** | 用户主动「点击」广告 | 用户触摸操作 | 主动、基于交互 |

两者不应混为一谈——点击广告的同时就"必然曝光"是一种错误的语义混淆。

## Feed 列表中的设计印证

在 Feed 列表中，Click 和 Exposure 同样是**彻底分离**的：

```kotlin
// ── 曝光：由 ExposureDetector 独立负责 ──
// 触发条件：可见面积 ≥50% + 在该状态下持续 ≥1 秒 + 会话去重
ExposureDetector(
    adId = ad.id,
    visibleFraction = visibleFractionMap[ad.id] ?: 0f,
    onExposed = { viewModel.recordExposure(ad.id) }  // ← 仅曝光
)

// ── 点击：由用户触摸操作触发 ──
onCardClick = {
    viewModel.recordClick(ad.id)  // ← 仅点击
    onAdClick(ad)
}
```

一个广告在 Feed 中可能被曝光多次（滚动进出可见区域），但 ExposureDetector 通过 `ExposureTracker` 确保单次会话仅计 1 次。而点击完全独立于此计时逻辑。

## AI 悬浮球场景的完整链路

```
AI 悬浮球中点击广告
  │
  ├─ recordClick(adId)        ← 立即记录点击 ✅   (FeedScreen.kt:424)
  ├─ 导航到 DetailScreen
  │
  └─ DetailScreen 内容渲染完成
       │
       └─ ExposureDetector     ← 1 秒后记录曝光 ✅ (DetailScreen.kt:110-114)
          visibleFraction = 1f
          ↓
          ExposureTracker.isExposed(adId) ?
          ├─ false → 延迟 1s → markExposed + recordExposure
          └─ true  → 跳过（已在 Feed 中曝光过）
```

## Feed 卡片快速点击的对等情况

这个设计在 Feed 卡片场景中也完全一致——如果用户快速点击 Feed 卡片（停留不足 1 秒），ExposureDetector 的计时尚未完成：

```
用户快速点击 Feed 卡片（停留 < 1s）
  │
  ├─ ExposureDetector: 计时被中断，未触发 onExposed
  ├─ recordClick(adId)        ← 点击仍被记录 ✅
  ├─ 导航到 DetailScreen
  │
  └─ DetailScreen 中的 ExposureDetector
       → 1 秒后触发 recordExposure ✅
```

这正是为什么 `DetailScreen` 中也需要放置 `ExposureDetector`——它不仅是 AI 入口的补位，也为**任何入口的快速点击场景兜底**。

## 去重机制

`ExposureTracker` 是全局单例（`object`），跨页面共享已曝光集合：

```
用户行为                              ExposureTracker 状态         结果
─────────────────────────────────────────────────────────────────────────────
在 Feed 中看到广告 A ≥1秒            markExposed("A")            曝光计 1 次
→ 点击进入详情页                      isExposed("A") == true      详情页跳过曝光

从 AI 搜索直接点击广告 B（Feed 未曝光） isExposed("B") == false   详情页 1 秒后曝光计 1 次
→ 返回 Feed，广告 B 进入可视区         isExposed("B") == true      Feed 跳过曝光
```

## 总结

| 入口 | Click 记录位置 | Exposure 记录位置 | 说明 |
|------|:-----------:|:-------------:|------|
| Feed 卡片 | `FeedScreen → onCardClick` | `FeedScreen → ExposureDetector` | 分开触发 |
| AI 悬浮球 | `FeedScreen → AiFloatingBall → onAdClick` | `DetailScreen → ExposureDetector` | 分开触发 |

两个入口的设计逻辑一致：**Click 在交互点即时记录，Exposure 由 ExposureDetector 在可见性条件满足后延迟触发**，两者职责分明、不重复、不遗漏。
