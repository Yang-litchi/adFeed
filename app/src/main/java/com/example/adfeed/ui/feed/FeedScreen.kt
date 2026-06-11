package com.example.adfeed.ui.feed

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.model.AdType
import com.example.adfeed.ui.ai.AiFloatingBall
import com.example.adfeed.ui.components.ExposureDetector
import com.example.adfeed.viewmodel.FeedViewModel

val CHANNELS = listOf("精选", "电商", "本地")
private const val MAX_VISIBLE_TAGS = 10

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onAdClick: (AdItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var filterExpanded by remember { mutableStateOf(false) }
    var showCustomTagInput by remember { mutableStateOf(false) }
    var customTagInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // 3页对应3个频道
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    // Pager滑动 → 同步Tab和频道
    LaunchedEffect(pagerState.currentPage) {
        val channelIndex = pagerState.currentPage
        if (selectedIndex != channelIndex) {
            selectedIndex = channelIndex
            selectedTags = setOf()
            filterExpanded = false
            viewModel.switchChannel(CHANNELS[channelIndex])
        }
    }

    // Tab点击 → 同步Pager
    LaunchedEffect(selectedIndex) {
        if (pagerState.currentPage != selectedIndex) {
            pagerState.animateScrollToPage(selectedIndex)
        }
    }

    val availableTags = remember(uiState.ads) {
        uiState.ads.flatMap { it.tags }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(MAX_VISIBLE_TAGS)
            .map { it.key }
    }

    val filteredAds = remember(uiState.ads, selectedTags) {
        if (selectedTags.isEmpty()) uiState.ads
        else uiState.ads.filter { ad -> ad.tags.containsAll(selectedTags.toList()) }
    }

    // 基于 LazyListState 计算每个可见 item 的可见面积比例
    // item.offset 为 item 顶部相对于 viewport 顶部的像素偏移（可为负值）
    val visibleFractionMap by remember {
        derivedStateOf {
            val viewportHeight = listState.layoutInfo.viewportSize.height
            if (viewportHeight == 0) return@derivedStateOf emptyMap<String, Float>()
            listState.layoutInfo.visibleItemsInfo.associate { info ->
                val itemTop = info.offset
                val itemBottom = info.offset + info.size
                val visibleTop = maxOf(itemTop, 0)
                val visibleBottom = minOf(itemBottom, viewportHeight)
                val visibleHeight = maxOf(0, visibleBottom - visibleTop)
                val fraction = if (info.size > 0) visibleHeight.toFloat() / info.size else 0f
                (info.key as? String ?: "") to fraction
            }
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() }
    )

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            // ── TabRow 占满 ──
            TabRow(
                selectedTabIndex = selectedIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                CHANNELS.forEachIndexed { index, channel ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            selectedTags = setOf()
                            filterExpanded = false
                            viewModel.switchChannel(channel)
                        },
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(text = channel, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }

            HorizontalDivider()

            // ── 筛选行 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { filterExpanded = !filterExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "筛选",
                        tint = if (selectedTags.isNotEmpty())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (selectedTags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(selectedTags.toList()) { tag ->
                            FilterChip(
                                selected = true,
                                onClick = { selectedTags = selectedTags - tag },
                                label = {
                                    Text("#$tag", style = MaterialTheme.typography.labelSmall)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                    TextButton(
                        onClick = { selectedTags = setOf() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("清空", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Text(
                        text = "全部",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // ── HorizontalPager ──
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (filteredAds.isEmpty() && !uiState.isLoading) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(64.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("没有匹配的广告", color = Color.Gray)
                                    if (selectedTags.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(onClick = { selectedTags = setOf() }) {
                                            Text("清空筛选")
                                        }
                                    }
                                }
                            }
                        }

                        items(items = filteredAds, key = { it.id }) { ad ->
                            // 曝光统计：≥50%可见 + 连续1秒 + 会话去重
                            ExposureDetector(
                                adId = ad.id,
                                visibleFraction = visibleFractionMap[ad.id] ?: 0f,
                                onExposed = { viewModel.recordExposure(ad.id) }
                            )
                            AdCardDispatcher(
                                ad = ad,
                                onLikeClick = { viewModel.toggleLike(ad.id) },
                                onCollectClick = { viewModel.toggleCollect(ad.id) },
                                onCardClick = {
                                    viewModel.recordClick(ad.id)
                                    onAdClick(ad)
                                },
                                onTagClick = { tag ->
                                    selectedTags = if (tag in selectedTags)
                                        selectedTags - tag
                                    else
                                        selectedTags + tag
                                }
                            )
                        }

                        item {
                            if (uiState.hasMore && uiState.ads.isNotEmpty()) {
                                LaunchedEffect(uiState.ads.size) { viewModel.loadMore() }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            } else if (!uiState.hasMore) {
                                Text(
                                    text = "已经到底了～",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (uiState.error != null) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("加载失败", color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { viewModel.refresh() }) {
                                        Text("重试")
                                    }
                                }
                            }
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = uiState.isLoading,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }

        // ── 悬浮筛选面板 ──
        AnimatedVisibility(
            visible = filterExpanded,
            enter = expandVertically(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200)),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 97.dp)
                .zIndex(10f)
        ) {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = "选择标签（可多选）",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val rows = availableTags.chunked(4)
                    rows.forEach { rowTags ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            rowTags.forEach { tag ->
                                val isSelected = tag in selectedTags
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedTags = if (isSelected)
                                            selectedTags - tag
                                        else
                                            selectedTags + tag
                                    },
                                    label = {
                                        Text("#$tag", style = MaterialTheme.typography.labelSmall)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.height(30.dp)
                                )
                            }
                            if (rowTags == rows.last()) {
                                IconButton(
                                    onClick = { showCustomTagInput = !showCustomTagInput },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "自定义标签",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    AnimatedVisibility(visible = showCustomTagInput) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = customTagInput,
                                onValueChange = { customTagInput = it },
                                placeholder = {
                                    Text(
                                        "输入自定义标签",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                textStyle = MaterialTheme.typography.labelSmall,
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    val tag = customTagInput.trim()
                                    if (tag.isNotEmpty()) {
                                        selectedTags = selectedTags + tag
                                        customTagInput = ""
                                        showCustomTagInput = false
                                    }
                                })
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                val tag = customTagInput.trim()
                                if (tag.isNotEmpty()) {
                                    selectedTags = selectedTags + tag
                                    customTagInput = ""
                                    showCustomTagInput = false
                                }
                            }) {
                                Text("确认")
                            }
                        }
                    }
                }
            }
        }

        // ── 悬浮AI球 ──
        AiFloatingBall(onAdClick = { ad ->
            viewModel.recordClick(ad.id)
            onAdClick(ad)
        })
    }
}

@Composable
fun AdCardDispatcher(
    ad: AdItem,
    onLikeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onCardClick: () -> Unit,
    onTagClick: (String) -> Unit = {}
) {
    when (ad.type) {
        AdType.LARGE_IMAGE -> LargeImageCard(ad, onLikeClick, onCollectClick, onCardClick, onTagClick)
        AdType.SMALL_IMAGE -> SmallImageCard(ad, onLikeClick, onCollectClick, onCardClick, onTagClick)
        AdType.VIDEO -> VideoCard(ad, onLikeClick, onCollectClick, onCardClick)
    }
}