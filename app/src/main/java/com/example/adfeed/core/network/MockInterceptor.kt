package com.example.adfeed.core.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.SocketTimeoutException

/**
 * Mock 模拟拦截器，用于离线开发和故障模拟
 *
 * 开启后拦截 Qwen API 请求，返回预设的模拟响应，支持：
 * - 模拟网络延迟（[simulateLatencyMs]）
 * - 模拟随机故障（[simulateFailureRate]）
 * - 运行时开关（[isMockEnabled]）
 *
 * 使用示例：
 * ```kotlin
 * // 离线开发模式
 * NetworkClient.mockInterceptor.isMockEnabled = true
 * NetworkClient.mockInterceptor.simulateFailureRate = 0f
 *
 * // 压力测试模式
 * NetworkClient.mockInterceptor.simulateFailureRate = 0.5f
 * ```
 *
 * @param isMockEnabled Mock 总开关，默认关闭（生产环境使用真实 API）
 * @param simulateLatencyMs 模拟的网络延迟（毫秒），默认 800ms
 * @param simulateFailureRate 模拟故障概率（0.0 ~ 1.0），默认 0（不模拟故障）
 */
class MockInterceptor(
    @Volatile var isMockEnabled: Boolean = false,
    var simulateLatencyMs: Long = 800L,
    var simulateFailureRate: Float = 0f
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 非 Mock 模式 → 正常请求透传
        if (!isMockEnabled) {
            return chain.proceed(request)
        }

        // 模拟网络延迟
        Thread.sleep(simulateLatencyMs)

        // 模拟随机故障（用于测试重试 + 降级逻辑）
        if (Math.random() < simulateFailureRate) {
            throw SocketTimeoutException("Mock: 模拟网络超时 (failureRate=$simulateFailureRate)")
        }

        // 仅拦截 chat/completions 路径的请求
        val path = request.url.encodedPath
        if (path.contains("chat/completions")) {
            val mockJson = buildMockChatResponse()
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK (Mock)")
                .addHeader("X-Mock-Response", "true")
                .addHeader("X-Mock-Latency", "${simulateLatencyMs}ms")
                .body(mockJson.toResponseBody("application/json; charset=utf-8".toMediaType()))
                .build()
        }

        // 其他路径正常透传
        return chain.proceed(request)
    }

    /**
     * 构建模拟的 AI 对话响应 JSON
     *
     * 返回与真实 Qwen API 完全一致的 JSON 结构，
     * 保证调用方解析逻辑无需任何修改。
     */
    private fun buildMockChatResponse(): String {
        return """
            {
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": "【AI产品顾问】这是一款极具性价比的商品，专为追求品质生活的你设计。核心卖点突出，非常适合日常使用场景。无论是自用还是送人，都是不错的选择。"
                        }
                    }
                ]
            }
        """.trimIndent()
    }
}
