package com.example.adfeed.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.adfeed.data.model.AdItem
import com.example.adfeed.data.remote.QwenApi
import kotlinx.coroutines.launch

data class ChatMessage(
    val role: String,   // "user" or "assistant"
    val content: String
)

fun buildSystemPrompt(ad: AdItem): String {
    val aiInfo = ad.aiInfo
    return buildString {
        appendLine("你是一名产品顾问")
        appendLine("以下是商品资料：")
        appendLine()
        appendLine("商品名称：")
        appendLine(ad.title)
        appendLine()
        appendLine("商品描述：")
        appendLine(ad.description)
        if (aiInfo != null) {
            appendLine()
            appendLine("产品特点：")
            aiInfo.features.forEach { appendLine(it) }
            appendLine()
            appendLine("目标用户：")
            aiInfo.targetUsers.forEach { appendLine(it) }
            appendLine()
            appendLine("推荐理由：")
            aiInfo.recommendReasons.forEach { appendLine(it) }
            appendLine()
            appendLine("使用场景：")
            aiInfo.scenarios.forEach { appendLine(it) }
        }
        appendLine()
        appendLine("请仅基于以上信息回答问题。")
        appendLine("如果资料中不存在答案，请明确说明。")
        appendLine("不要编造商品信息。")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatOverlay(
    ad: AdItem,
    onDismiss: () -> Unit
) {
    val systemPrompt = remember(ad.id) { buildSystemPrompt(ad) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 自动滚到最新消息
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty() || isLoading) return
        inputText = ""
        messages.add(ChatMessage("user", text))
        isLoading = true

        scope.launch {
            QwenApi.chat(
                systemPrompt = systemPrompt,
                history = messages.dropLast(1), // 不含刚加的user msg
                userMessage = text
            ).fold(
                onSuccess = { reply ->
                    messages.add(ChatMessage("assistant", reply))
                },
                onFailure = {
                    messages.add(ChatMessage("assistant", "出错了，请重试"))
                }
            )
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨ AI 产品顾问",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF6650A4),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ad.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            HorizontalDivider()

            // 消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Surface(
                                color = Color(0xFFF0EEFF),
                                shape = RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 12.dp,
                                    bottomStart = 12.dp,
                                    bottomEnd = 12.dp
                                )
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF6650A4)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // 输入框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "问问这个产品...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage() })
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { sendMessage() },
                    enabled = !isLoading && inputText.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "发送",
                        tint = if (!isLoading && inputText.isNotBlank())
                            Color(0xFF6650A4)
                        else
                            Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) Color(0xFF6650A4) else Color(0xFFF0EEFF),
            shape = RoundedCornerShape(
                topStart = if (isUser) 12.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 12.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) Color.White else Color(0xFF4A4060)
            )
        }
    }
}