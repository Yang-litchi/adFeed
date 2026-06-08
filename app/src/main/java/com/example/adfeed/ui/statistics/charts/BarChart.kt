package com.example.adfeed.ui.statistics.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.adfeed.data.model.TagStatistic
import kotlin.math.max

/** 柱状图配色（循环使用） */
private val BAR_COLORS = listOf(
    0xFF6650A4, 0xFF9C89D4, 0xFFB8A9E8, 0xFF7C6BB0,
    0xFFE0A860, 0xFF60B0A8, 0xFFA86080, 0xFF6080B8
).map { Color(it) }

/**
 * 水平柱状图 — 用于同Tag广告曝光量对比
 *
 * @param data 统计对比数据列表
 * @param modifier 布局修饰符
 */
@Composable
fun BarChart(
    data: List<TagStatistic>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.height(120.dp), contentAlignment = Alignment.Center) {
            Text("暂无对比数据", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        return
    }

    val maxValue = data.maxOf { it.exposureCount }.coerceAtLeast(1)
    val labelWidth = 100.dp  // 左侧标题宽度

    Column(modifier = modifier.fillMaxWidth()) {
        data.forEachIndexed { index, item ->
            BarRow(
                item = item,
                maxValue = maxValue,
                color = BAR_COLORS[index % BAR_COLORS.size],
                labelWidth = labelWidth
            )
            if (index < data.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BarRow(
    item: TagStatistic,
    maxValue: Int,
    color: Color,
    labelWidth: Dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：广告标题（截断显示）
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            color = Color.DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(labelWidth)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 中间：柱状条（fillMaxWidth 占满剩余空间）
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(22.dp)
        ) {
            val barWidth = size.width * (item.exposureCount.toFloat() / maxValue)
            if (barWidth > 0f) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset.Zero,
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 右侧：数值标签
        Text(
            text = "${item.exposureCount}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.DarkGray,
            modifier = Modifier.width(40.dp)
        )
    }
}
