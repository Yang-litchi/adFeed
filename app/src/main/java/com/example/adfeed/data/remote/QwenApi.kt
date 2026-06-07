package com.example.adfeed.data.remote

import com.example.adfeed.data.model.AiInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.example.adfeed.ui.ai.ChatMessage
object QwenApi {

    private const val API_KEY = "your apikey"
    private const val API_URL =
        "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"

    suspend fun chat(
        systemPrompt: String,
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = JSONArray().apply {
                // system
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                // history
                history.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }
                // 新消息
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

            val url = URL(API_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $API_KEY")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            OutputStreamWriter(conn.outputStream).use {
                it.write(requestBody.toString())
            }

            val response = conn.inputStream.bufferedReader().readText()
            val content = JSONObject(response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            Result.success(content.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun generateAdIntro(aiInfo: AiInfo, title: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(aiInfo, title)
                val requestBody = JSONObject().apply {
                    put("model", "qwen-turbo")
                    put("max_tokens", 200)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "你是广告文案助手，只输出介绍内容，不要任何多余文字，控制在100字以内。")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                }

                val url = URL(API_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $API_KEY")
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 15000

                OutputStreamWriter(conn.outputStream).use {
                    it.write(requestBody.toString())
                }

                val response = conn.inputStream.bufferedReader().readText()
                val content = JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                Result.success(content.trim())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun buildPrompt(aiInfo: AiInfo, title: String): String {
        return """
            广告名称：$title
            核心特点：${aiInfo.features.joinToString("、")}
            目标用户：${aiInfo.targetUsers.joinToString("、")}
            推荐理由：${aiInfo.recommendReasons.joinToString("、")}
            使用场景：${aiInfo.scenarios.joinToString("、")}
            
            基于以上信息写一段100字左右的广告介绍，语气自然亲切。
        """.trimIndent()
    }
}