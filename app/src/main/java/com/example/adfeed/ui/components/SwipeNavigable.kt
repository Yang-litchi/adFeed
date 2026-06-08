package com.example.adfeed.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 水平滑动导航修饰符
 *
 * 检测水平滑动手势，在手势结束时若累积位移超过阈值则触发导航回调。
 * 与垂直滚动（LazyColumn、verticalScroll）互不冲突 —
 * [detectHorizontalDragGestures] 只响应水平为主的手势。
 *
 * 采用模块化设计：手势识别（此修饰符）与页面导航（回调参数）完全解耦。
 *
 * @param onSwipeLeft  向左滑动（右→左）的回调，通常用于前进导航
 * @param onSwipeRight 向右滑动（左→右）的回调，通常用于返回导航
 * @param thresholdDp  触发阈值（dp），默认 120dp，避免误触发
 *
 * 使用示例：
 * ```
 * Scaffold(
 *     modifier = Modifier.swipeNavigable(
 *         onSwipeLeft = { navigateToNext() },
 *         onSwipeRight = { navigateBack() }
 *     )
 * )
 * ```
 */
@Composable
fun Modifier.swipeNavigable(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    thresholdDp: Dp = 120.dp
): Modifier {
    if (onSwipeLeft == null && onSwipeRight == null) return this

    val density = LocalDensity.current
    val thresholdPx = remember(thresholdDp) {
        with(density) { thresholdDp.toPx() }
    }

    // 累积水平位移（需要在 pointerInput 之外 remember 以在重组时保持状态）
    var totalDragX by remember { mutableFloatStateOf(0f) }

    return this.pointerInput(onSwipeLeft, onSwipeRight) {
        detectHorizontalDragGestures(
            onDragStart = {
                totalDragX = 0f
            },
            onDragEnd = {
                // 手势结束时根据累积位移判断是否触发导航
                when {
                    totalDragX <= -thresholdPx -> onSwipeLeft?.invoke()
                    totalDragX >= thresholdPx  -> onSwipeRight?.invoke()
                    // 位移不足阈值 → 不触发
                }
            },
            onDragCancel = {
                totalDragX = 0f
            },
            onHorizontalDrag = { _, dragAmount ->
                // dragAmount 是本次事件的增量（非累积），需手动累加
                totalDragX += dragAmount
            }
        )
    }
}
