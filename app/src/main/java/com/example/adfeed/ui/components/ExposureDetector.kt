package com.example.adfeed.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.adfeed.data.exposure.ExposureTracker
import kotlinx.coroutines.delay

/**
 * 广告曝光检测器
 *
 * ## 曝光判定规则
 * 1. 可见面积 ≥ [minFraction]（默认 50%）→ 进入候选状态
 * 2. 在满足可见面积 ≥ 50% 的条件下**连续展示 [durationMs]（默认 1000ms）** → 计为 1 次有效曝光
 * 3. 计时期间可见面积下降到阈值以下 → 取消本次计时，重新进入候选时重新计时
 * 4. 同一广告单次会话内仅允许 1 次有效曝光 → 通过 [ExposureTracker] 去重
 *
 * ## 设计原则
 * - **独立模块**：与具体页面、ViewModel、数据源解耦
 * - **纯组合逻辑**：不包含UI渲染，仅通过 LaunchedEffect 管理计时
 * - **可复用**：任何页面/组件均可使用此检测器
 *
 * @param adId           广告ID，用于会话去重
 * @param visibleFraction 当前可见面积比例（0.0 ~ 1.0），由调用方通过 LazyListState.layoutInfo 等机制计算
 * @param onExposed       满足曝光条件后的回调（仅触发一次/会话）
 * @param minFraction     可见面积阈值，默认 0.5（50%）
 * @param durationMs      连续可见时长阈值（毫秒），默认 1000ms
 */
@Composable
fun ExposureDetector(
    adId: String,
    visibleFraction: Float,
    onExposed: () -> Unit,
    minFraction: Float = 0.5f,
    durationMs: Long = 1000L
) {
    val tracker = remember { ExposureTracker }

    // 以"是否满足可见阈值"作为 LaunchedEffect 的 key
    // → 阈值跨越时自动取消/重启协程，实现计时取消语义
    val meetsThreshold = visibleFraction >= minFraction && !tracker.isExposed(adId)

    LaunchedEffect(adId, meetsThreshold) {
        if (meetsThreshold) {
            // 满足 ≥50% 可见 → 开始 1秒计时
            delay(durationMs)

            // 计时结束后再次检查去重（以防并发场景下的竞态）
            if (!tracker.isExposed(adId)) {
                //tracker.markExposed(adId)
                //让 ExposureDetector 只负责检测，防重放在recordExposure内
                onExposed()
            }
        }
        // meetsThreshold == false → LaunchedEffect 取消 → delay 被中断 → 计时取消
    }
}
