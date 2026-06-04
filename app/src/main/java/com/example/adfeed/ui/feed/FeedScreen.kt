package com.example.adfeed.ui.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.model.AdType
import com.example.adfeed.viewmodel.FeedViewModel

val CHANNELS = listOf("精选", "电商", "本地")

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onAdClick: (AdItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedIndex by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedIndex) {
            CHANNELS.forEachIndexed { index, channel ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = {
                        selectedIndex = index
                        viewModel.switchChannel(channel)
                    },
                    text = { Text(channel) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(
                    items = uiState.ads,
                    key = { it.id }
                ) { ad ->
                    AdCardDispatcher(
                        ad = ad,
                        onLikeClick = { viewModel.toggleLike(ad.id) },
                        onCollectClick = { viewModel.toggleCollect(ad.id) },
                        onCardClick = {
                            viewModel.recordClick(ad.id)
                            onAdClick(ad)
                        }
                    )
                }

                item {
                    if (uiState.hasMore && uiState.ads.isNotEmpty()) {
                        LaunchedEffect(uiState.ads.size) {
                            viewModel.loadMore()
                        }
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

@Composable
fun AdCardDispatcher(
    ad: AdItem,
    onLikeClick: () -> Unit,
    onCollectClick: () -> Unit,
    onCardClick: () -> Unit
) {
    when (ad.type) {
        AdType.LARGE_IMAGE -> LargeImageCard(ad, onLikeClick, onCollectClick, onCardClick)
        AdType.SMALL_IMAGE -> SmallImageCard(ad, onLikeClick, onCollectClick, onCardClick)
        AdType.VIDEO -> VideoCard(ad, onLikeClick, onCollectClick, onCardClick)
    }
}