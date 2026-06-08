package com.example.adfeed.ui.statistics.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.unit.dp
import com.example.adfeed.data.model.TrendData
import kotlin.math.max

/** 曝光线颜色 */
private val EXPOSURE_COLOR = Color(0xFF6650A4)
/** 点击线颜色 */
private val CLICK_COLOR = Color(0xFFE0A860)
/** Y轴刻度数量 */
private const val Y_AXIS_TICKS = 4

/**
 * 折线图 — 用于7天曝光/点击趋势展示
 *
 * @param data 7天趋势数据
 * @param modifier 布局修饰符
 */
@Composable
fun LineChart(
    data: List<TrendData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.height(200.dp), contentAlignment = Alignment.Center) {
            Text("暂无趋势数据", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        return
    }

    // 计算Y轴最大值（取曝光和点击中的最大值，留10%边距）
    val maxExposure = data.maxOf { it.exposureCount }.coerceAtLeast(1)
    val maxClick = data.maxOf { it.clickCount }.coerceAtLeast(1)
    val yMax = max(maxExposure, maxClick) * 1.1f

    Column(modifier = modifier.fillMaxWidth()) {
        // 图例
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem(color = EXPOSURE_COLOR, label = "曝光")
            Spacer(modifier = Modifier.width(24.dp))
            LegendItem(color = CLICK_COLOR, label = "点击")
        }

        // 折线图画布
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val pointCount = data.size

            if (pointCount < 2) return@Canvas

            // 计算X轴步长
            val xStep = canvasWidth / (pointCount - 1).coerceAtLeast(1)

            // === 辅助线（水平虚线） ===
            for (i in 0..Y_AXIS_TICKS) {
                val y = canvasHeight * i / Y_AXIS_TICKS
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(8f, 4f), 0f
                    )
                )
            }

            // === 计算各数据点坐标 ===
            val exposurePoints = data.mapIndexed { index, d ->
                Offset(
                    x = index * xStep,
                    y = canvasHeight - (d.exposureCount / yMax * canvasHeight)
                )
            }
            val clickPoints = data.mapIndexed { index, d ->
                Offset(
                    x = index * xStep,
                    y = canvasHeight - (d.clickCount / yMax * canvasHeight)
                )
            }

            // === 绘制曝光折线 ===
            drawPathLine(exposurePoints, EXPOSURE_COLOR)

            // === 绘制点击折线 ===
            drawPathLine(clickPoints, CLICK_COLOR)

            // === 绘制数据点 ===
            exposurePoints.forEach { pt ->
                drawCircle(color = EXPOSURE_COLOR, radius = 5f, center = pt)
                drawCircle(color = Color.White, radius = 2.5f, center = pt)
            }
            clickPoints.forEach { pt ->
                drawCircle(color = CLICK_COLOR, radius = 5f, center = pt)
                drawCircle(color = Color.White, radius = 2.5f, center = pt)
            }
        }

        // X轴标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { item ->
                Text(
                    text = item.dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/** 使用 Path 绘制折线 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPathLine(
    points: List<Offset>,
    color: Color
) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 3f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/** 图例项：色块 + 文字 */
@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color, radius = 6f)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
    }
}
