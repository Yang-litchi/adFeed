package com.example.adfeed.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.adfeed.data.model.AdType
import com.example.adfeed.ui.components.ExposureDetector
import com.example.adfeed.ui.components.LikeButton
import com.example.adfeed.ui.components.TagChip
import com.example.adfeed.viewmodel.FeedViewModel

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adfeed.viewmodel.DetailViewModel

import com.example.adfeed.ui.ai.AiChatOverlay
import com.example.adfeed.ui.components.swipeNavigable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    adId: String,
    viewModel: FeedViewModel,
    detailViewModel: DetailViewModel = viewModel(),
    onBack: () -> Unit,
    onViewStatistics: (String) -> Unit = {}
) {
    LaunchedEffect(adId) {
        viewModel.loadDetailAd(adId)
    }

    DisposableEffect(adId) {
        onDispose { viewModel.clearDetailAd() }
    }

    val ad by viewModel.detailAd.collectAsState()
    var showAiChat by remember { mutableStateOf(false) }

    if (ad == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val adItem = ad!!

    Scaffold(
        modifier = Modifier.swipeNavigable(
            onSwipeLeft = { onViewStatistics(adItem.id) }  // 右→左滑动 → 进入统计页
        ),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = adItem.title,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onViewStatistics(adItem.id) }) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "查看统计数据",
                            tint = Color(0xFF6650A4)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        val detailUiState by detailViewModel.uiState.collectAsState()

        LaunchedEffect(adItem.id) {
            if (adItem.aiInfo != null) detailViewModel.loadIntro(adItem)
        }

        // 曝光统计：详情页内容默认100%可见，1秒后计入曝光（单会话去重）
        // 与 Feed 列表使用相同的 ExposureDetector + ExposureTracker 规则
        ExposureDetector(
            adId = adItem.id,
            visibleFraction = 1f,
            onExposed = { viewModel.recordExposure(adItem.id) }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                // 顶部媒体
                if (adItem.type == AdType.VIDEO && !adItem.videoUrl.isNullOrEmpty()) {
                    DetailVideoPlayer(videoUrl = adItem.videoUrl)
                } else {
                    AsyncImage(
                        model = adItem.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // 标题
                    Text(
                        text = adItem.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 标签
                    if (adItem.tags.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(adItem.tags) { tag -> TagChip(tag) }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // AI介绍
                    if (adItem.aiInfo != null) {
                        Surface(
                            color = Color(0xFFF0EEFF),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "✨ AI 介绍",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF6650A4)
                                    )
                                    if (detailUiState.isLoading || detailUiState.isTyping) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 1.5.dp,
                                            color = Color(0xFF6650A4)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                detailUiState.error?.let { error ->
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    if (detailUiState.displayedText.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                                when {
                                    detailUiState.displayedText.isNotEmpty() -> {
                                        Text(
                                            text = detailUiState.displayedText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF4A4060)
                                        )
                                    }
                                    detailUiState.isLoading -> {
                                        Text(
                                            text = "正在生成...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 正文
                    Text(
                        text = adItem.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // 互动栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LikeButton(
                            isLiked = adItem.isLiked,
                            count = adItem.likeCount,
                            onClick = { viewModel.toggleLike(adItem.id) }
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.toggleCollect(adItem.id) }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = if (adItem.isCollected) Icons.Filled.Bookmark
                                else Icons.Outlined.BookmarkBorder,
                                contentDescription = "收藏",
                                tint = if (adItem.isCollected) Color(0xFFFFAA00) else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (adItem.collectCount >= 1000) {
                                    "${"%.1f".format(adItem.collectCount / 1000f)}k"
                                } else {
                                    adItem.collectCount.toString()
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "分享",
                                    tint = Color.Gray
                                )
                            }
                            Text(
                                "分享",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    //唤起ai助手
                    Button(
                        onClick = { showAiChat = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6650A4)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("✨ 问问AI顾问")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        if (showAiChat) {
            AiChatOverlay(
                ad = adItem,
                onDismiss = { showAiChat = false }
            )
        }
    }
}

@Composable
fun DetailVideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    )
}
