package com.example.adfeed.data.remote

import com.example.adfeed.core.network.NetworkClient
import com.example.adfeed.data.model.AiInfo
import com.example.adfeed.ui.ai.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * 通义千问 (Qwen) 大模型 API 服务
 *
 * 基于 OkHttp 执行网络请求，通过 [NetworkClient.okHttpClient] 获得：
 * - 自动重试（[com.example.adfeed.core.network.RetryInterceptor]）
 * - Mock 离线模拟（[com.example.adfeed.core.network.MockInterceptor]）
 * - 请求/响应日志（HttpLoggingInterceptor）
 * - 统一超时配置（连接/读取各 15 秒）
 *
 * 所有方法均为挂起函数，在 [Dispatchers.IO] 上执行网络 I/O。
 */
object QwenApi {

    private const val API_KEY = "your apikey"
    private const val API_URL =
        "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * 执行一次 Qwen API 请求的通用方法
     *
     * 构建 OkHttp Request → 通过 [NetworkClient.okHttpClient] 执行 → 解析响应 JSON
     * 所有拦截器（Mock、重试、日志）在此过程中自动生效。
     *
     * @param requestBody 完整的请求 JSON 对象
     * @return 成功时返回 AI 回复文本，失败时返回 [Result.failure]
     */
    private suspend fun executeRequest(requestBody: JSONObject): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // 1. 构建 OkHttp Request
                val request = Request.Builder()
                    .url(API_URL)
                    .post(requestBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .build()

                // 2. 通过 NetworkClient 的 OkHttpClient 执行（自动享受重试+Mock+日志）
                val response = NetworkClient.okHttpClient.newCall(request).execute()

                // 3. 解析响应体
                val responseBody = response.body?.string()
                    ?: throw IOException("响应体为空")

                if (!response.isSuccessful) {
                    throw IOException("API 请求失败 (HTTP ${response.code}): $responseBody")
                }

                // 4. 从 choices[0].message.content 中提取 AI 回复文本
                val content = JSONObject(responseBody)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                Result.success(content.trim())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * AI 产品顾问多轮对话
     *
     * @param systemPrompt 系统角色提示词（如商品资料上下文）
     * @param history 历史对话记录（不含当前用户消息）
     * @param userMessage 当前用户输入的问题
     * @return 成功时返回 AI 回复文本
     */
    suspend fun chat(
        systemPrompt: String,
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> {
        val messages = JSONArray().apply {
            // system 消息
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            // 历史对话
            history.forEach { msg ->
                put(JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                })
            }
            // 当前用户消息
            put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })
        }

        val requestBody = JSONObject().apply {
            put("model", "qwen-turbo")
            put("max_tokens", 500)
            put("messages", messages)
        }

        return executeRequest(requestBody)
    }

    /**
     * 生成 AI 广告介绍文案
     *
     * 基于广告的 [AiInfo] 结构化数据构建专用提示词，
     * 请求大模型生成约 100 字的口语化广告介绍。
     *
     * @param aiInfo 广告 AI 分析数据（特点、目标用户、推荐理由、使用场景）
     * @param title 广告标题
     * @return 成功时返回生成的广告介绍文本
     */
    suspend fun generateAdIntro(aiInfo: AiInfo, title: String): Result<String> {
        val prompt = buildString {
            appendLine("广告名称：$title")
            appendLine("核心特点：${aiInfo.features.joinToString("、")}")
            appendLine("目标用户：${aiInfo.targetUsers.joinToString("、")}")
            appendLine("推荐理由：${aiInfo.recommendReasons.joinToString("、")}")
            appendLine("使用场景：${aiInfo.scenarios.joinToString("、")}")
            appendLine()
            appendLine("基于以上信息写一段100字左右的广告介绍，语气自然亲切。")
        }

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", "你是广告文案助手，只输出介绍内容，不要任何多余文字，控制在100字以内。")
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }

        val requestBody = JSONObject().apply {
            put("model", "qwen-turbo")
            put("max_tokens", 200)
            put("messages", messages)
        }

        return executeRequest(requestBody)
    }
}
