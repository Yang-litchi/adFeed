package com.example.adfeed.ui.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.remote.QwenApi
import com.example.adfeed.data.repository.MockData
import kotlinx.coroutines.launch
import com.example.adfeed.viewmodel.FeedViewModel

data class SearchRecord(
    val query: String,
    val reply: String,
    val ads: List<AdItem>
)

@Composable
fun AiFloatingBall(
    onAdClick: (AdItem) -> Unit,
    feedViewModel: FeedViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val searchHistory = remember { mutableStateListOf<SearchRecord>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 悬浮球脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "ball")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // 历史记录更新时滚动到底部
    LaunchedEffect(searchHistory.size) {
        if (searchHistory.isNotEmpty()) {
            listState.animateScrollToItem(searchHistory.size - 1)
        }
    }

    // 悬浮球左边距
    val ballStartPadding = 20.dp
    // 输入框右边距与左边距一致
    val inputEndPadding = 20.dp

    fun search() {
        val query = inputText.trim()
        if (query.isEmpty() || isLoading) return
        inputText = ""
        isLoading = true

        scope.launch {
            val adSummaries = MockData.allAds.joinToString("\n") {
                "ID:${it.id} 标题:${it.title} 标签:${it.tags.joinToString(",")}"
            }
            val systemPrompt = """
                用户想找某类广告，从列表中返回最匹配的广告ID和回复。
                只返回JSON格式：{"ids": ["id1","id2"], "reply": "简短说明"}
                广告列表：
                $adSummaries
            """.trimIndent()

            QwenApi.chat(
                systemPrompt = systemPrompt,
                history = emptyList(),
                userMessage = query
            ).fold(
                onSuccess = { response ->
                    try {
                        val json = org.json.JSONObject(response)
                        val ids = json.getJSONArray("ids")
                        val idList = (0 until ids.length()).map { ids.getString(it) }
                        val resultAds = MockData.allAds.filter { it.id in idList }
                        // AI推荐即曝光
                        resultAds.forEach { ad ->
                            feedViewModel.recordAiExposure(ad.id)
                        }
                        val reply = json.optString("reply", "为你找到以下广告")
                        searchHistory.add(SearchRecord(query, reply, resultAds))
                    } catch (e: Exception) {
                        searchHistory.add(SearchRecord(query, response, emptyList()))
                    }
                },
                onFailure = {
                    searchHistory.add(SearchRecord(query, "搜索出错，请重试", emptyList()))
                }
            )
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            modifier = Modifier
                .padding(start = ballStartPadding, end = inputEndPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom
        ) {
            // 历史记录 + 结果区域（最高屏幕2/3）
            AnimatedVisibility(
                visible = expanded && searchHistory.isNotEmpty(),
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.67f)  // 最高2/3屏幕
                        .padding(bottom = 8.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 4.dp
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchHistory) { record ->
                            Column {
                                // 用户提问气泡
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Surface(
                                        color = Color(0xFF6650A4),
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 4.dp,
                                            bottomStart = 12.dp,
                                            bottomEnd = 12.dp
                                        )
                                    ) {
                                        Text(
                                            text = record.query,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // AI回复
                                Text(
                                    text = "✨ ${record.reply}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6650A4),
                                    modifier = Modifier.padding(start = 4.dp)
                                )

                                // 推荐广告卡片
                                if (record.ads.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    record.ads.forEach { ad ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clickable {
                                                    feedViewModel.recordClick(ad.id)
                                                    onAdClick(ad)
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF5F5F5)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(
                                                    model = ad.imageUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(RoundedCornerShape(6.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = ad.title,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = ad.tags.take(3).joinToString(" ") { "#$it" },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF6650A4)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 加载中
                        if (isLoading) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF6650A4)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "正在为你搜索...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 悬浮球 + 输入框行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 悬浮球
                Box(
                    modifier = Modifier
                        .scale(if (expanded) 1f else glowScale)
                        .size(56.dp)
                        .shadow(8.dp, CircleShape)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF6650A4),
                                    Color(0xFF9C89D4),
                                    Color(0xFF6650A4)
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable { expanded = !expanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.SmartToy,
                        contentDescription = "AI",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 展开的输入框（撑满剩余宽度）
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f)
                            .shadow(6.dp, RoundedCornerShape(28.dp))
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "描述你想要的广告...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { search() })
                        )
                        IconButton(
                            onClick = { search() },
                            enabled = !isLoading && inputText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "搜索",
                                tint = if (!isLoading && inputText.isNotBlank())
                                    Color(0xFF6650A4) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}