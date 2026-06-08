package com.example.adfeed.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 自动重试 OkHttp 拦截器
 *
 * 在遇到以下情况时自动重试（最多 [maxRetries] 次）：
 * - HTTP 5xx 服务器错误
 * - 网络 I/O 异常（[IOException]，如连接超时、连接重置等）
 *
 * 重试策略采用指数退避（exponential backoff）：
 * 第一次重试等待 [retryDelayMs]，第二次等待 2×[retryDelayMs]，以此类推。
 *
 * **不会重试的情况**：
 * - HTTP 4xx 客户端错误（错误在请求本身，重试无意义）
 * - 2xx/3xx 成功响应
 *
 * 使用示例：
 * ```kotlin
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(RetryInterceptor(maxRetries = 2, retryDelayMs = 500))
 *     .build()
 * ```
 *
 * @param maxRetries 最大重试次数（不含首次请求），默认 2 次
 * @param retryDelayMs 基础重试等待时间（毫秒），每次重试延迟 = 基础时间 × 当前尝试次数
 */
class RetryInterceptor(
    private val maxRetries: Int = 2,
    private val retryDelayMs: Long = 500L
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: IOException? = null
        var attemptCount = 0

        while (attemptCount <= maxRetries) {
            try {
                val response = chain.proceed(request)

                // 5xx 服务器错误 → 重试
                if (response.code in 500..599) {
                    attemptCount++
                    response.close() // 必须关闭，否则连接泄漏
                    if (attemptCount <= maxRetries) {
                        Thread.sleep(retryDelayMs * attemptCount) // 指数退避
                    }
                    continue
                }

                // 2xx / 3xx / 4xx → 直接返回，不重试
                return response
            } catch (e: IOException) {
                // 网络 I/O 异常 → 重试
                lastException = e
                attemptCount++
                if (attemptCount <= maxRetries) {
                    Thread.sleep(retryDelayMs * attemptCount)
                }
            }
        }

        // 所有重试已耗尽
        throw lastException
            ?: IOException("请求失败：已达最大重试次数 ($maxRetries)")
    }
}
