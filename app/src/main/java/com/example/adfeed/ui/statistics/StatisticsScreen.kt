package com.example.adfeed.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adfeed.data.repository.MockData
import com.example.adfeed.ui.components.swipeNavigable
import com.example.adfeed.ui.statistics.charts.BarChart
import com.example.adfeed.ui.statistics.charts.LineChart
import com.example.adfeed.viewmodel.StatisticsViewModel
import java.util.Locale

/**
 * 统计页面
 *
 * 通过 [StatisticsViewModel] 获取数据，
 * 展示当前广告统计、同Tag对比柱状图和7天趋势折线图。
 *
 * @param adId 广告ID
 * @param onBack 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    adId: String,
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 获取广告信息用于标题显示
    val ad = remember(adId) {
        MockData.allAds.find { it.id == adId }
    }

    LaunchedEffect(adId) {
        viewModel.loadStatistics(adId)
    }

    Scaffold(
        modifier = Modifier.swipeNavigable(
            onSwipeRight = onBack  // 左→右滑动 → 返回详情页
        ),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = ad?.title ?: "统计数据",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            // 错误状态
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry(adId) }) {
                            Text("重试")
                        }
                    }
                }
            }

            // 加载中
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6650A4))
                }
            }

            // 正常内容
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // ─── 区块1：当前广告统计卡片 ───
                    StatSummaryCard(uiState.adSummary)

                    // ─── 区块2：同Tag对比柱状图 ───
                    val tagLabel = ad?.tags?.firstOrNull() ?: ""
                    TagComparisonSection(
                        tag = tagLabel,
                        tagStatistics = uiState.tagStatistics
                    )

                    // ─── 区块3：7天趋势折线图 ───
                    TrendSection(trendData = uiState.trendData)

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ==================== 子组件 ====================

/** 统计摘要卡片 */
@Composable
private fun StatSummaryCard(summary: com.example.adfeed.data.model.StatisticSummary?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EEFF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 当前广告统计",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6650A4)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "曝光量", value = "${summary?.totalExposure ?: 0}")
                StatItem(label = "点击量", value = "${summary?.totalClick ?: 0}")
                StatItem(
                    label = "CTR",
                    value = String.format(Locale.US, "%.2f%%", (summary?.ctr ?: 0f) * 100)
                )
            }
        }
    }
}

/** 单个统计指标 */
@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A4060)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

/** 同Tag对比区块 */
@Composable
private fun TagComparisonSection(
    tag: String,
    tagStatistics: List<com.example.adfeed.data.model.TagStatistic>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (tag.isNotEmpty()) "📈 同Tag对比（#$tag）" else "📈 同Tag对比",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6650A4)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 表头
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "广告名称",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "曝光量",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.width(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            BarChart(data = tagStatistics)
        }
    }
}

/** 7天趋势区块 */
@Composable
private fun TrendSection(trendData: List<com.example.adfeed.data.model.TrendData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📉 最近7天趋势",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6650A4)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LineChart(data = trendData)
        }
    }
}
