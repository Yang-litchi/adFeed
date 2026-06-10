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
import com.example.adfeed.ui.components.LikeButton
import com.example.adfeed.ui.components.TagChip
import com.example.adfeed.viewmodel.FeedViewModel

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adfeed.viewmodel.DetailViewModel

import com.example.adfeed.ui.ai.AiChatOverlay
import com.example.adfeed.ui.components.swipeNavigable
import com.example.adfeed.data.repository.MockData
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    adId: String,
    viewModel: FeedViewModel,
    detailViewModel: DetailViewModel = viewModel(),
    onBack: () -> Unit,
    onViewStatistics: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val ad = uiState.ads.find { it.id == adId }
        ?: MockData.allAds.find { it.id == adId }
    var showAiChat by remember { mutableStateOf(false) }

    if (ad == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("内容不存在")
        }
        return
    }

    Scaffold(
        modifier = Modifier.swipeNavigable(
            onSwipeLeft = { onViewStatistics(ad.id) }  // 右→左滑动 → 进入统计页
        ),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = ad.title,
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
                    IconButton(onClick = { onViewStatistics(ad.id) }) {
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

        LaunchedEffect(ad.id) {
            if (ad.aiInfo != null) detailViewModel.loadIntro(ad)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                // 顶部媒体
                if (ad.type == AdType.VIDEO && !ad.videoUrl.isNullOrEmpty()) {
                    DetailVideoPlayer(videoUrl = ad.videoUrl)
                } else {
                    AsyncImage(
                        model = ad.imageUrl,
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
                        text = ad.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 标签
                    if (ad.tags.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ad.tags) { tag -> TagChip(tag) }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // AI介绍
                    if (ad.aiInfo != null) {
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
                        text = ad.description,
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
                            isLiked = ad.isLiked,
                            count = ad.likeCount,
                            onClick = { viewModel.toggleLike(ad.id) }
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.toggleCollect(ad.id) }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = if (ad.isCollected) Icons.Filled.Bookmark
                                else Icons.Outlined.BookmarkBorder,
                                contentDescription = "收藏",
                                tint = if (ad.isCollected) Color(0xFFFFAA00) else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (ad.collectCount >= 1000) {
                                    "${"%.1f".format(ad.collectCount / 1000f)}k"
                                } else {
                                    ad.collectCount.toString()
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
                ad = ad,
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
