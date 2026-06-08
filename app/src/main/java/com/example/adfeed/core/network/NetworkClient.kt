package com.example.adfeed.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络客户端工厂
 *
 * 提供全局唯一的 OkHttp 客户端和 Retrofit 实例创建能力。
 * 所有网络请求均通过此工厂获取配置好的 Retrofit Service 接口。
 *
 * 特性：
 * - 统一超时配置（连接/读/写 各 15 秒）
 * - 请求/响应日志输出（通过 [HttpLoggingInterceptor]）
 * - 自动重试（通过 [RetryInterceptor]）
 * - Mock 模式支持（通过 [MockInterceptor]）
 * - 统一 [NetworkResult] 结果转换
 *
 * 使用示例：
 * ```kotlin
 * // 创建 API Service
 * val service = NetworkClient.createService(
 *     baseUrl = "https://api.example.com/",
 *     serviceClass = MyApiService::class.java
 * )
 *
 * // 运行时切换 Mock 模式
 * NetworkClient.mockInterceptor.isMockEnabled = false
 * ```
 */
object NetworkClient {

    /**
     * 全局 Mock 拦截器实例
     *
     * 外部可通过此属性运行时切换 Mock 开关或调整模拟参数：
     * ```kotlin
     * // 关闭 Mock，使用真实网络
     * NetworkClient.mockInterceptor.isMockEnabled = false
     *
     * // 模拟 50% 故障率进行压力测试
     * NetworkClient.mockInterceptor.simulateFailureRate = 0.5f
     * ```
     */
    val mockInterceptor = MockInterceptor()

    /**
     * 全局唯一的 OkHttpClient 实例（懒加载）
     *
     * 拦截器执行顺序（按 addInterceptor 顺序）：
     * 1. [MockInterceptor] — Mock 模式优先拦截，返回假数据
     * 2. [RetryInterceptor] — 对 5xx/IOException 自动重试
     * 3. [HttpLoggingInterceptor] — 打印 BODY 级别的请求/响应日志
     *
     * 可直接用于执行原始 OkHttp 请求（适合需要自定义 JSON 解析的场景）。
     */
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(mockInterceptor)
            .addInterceptor(RetryInterceptor(maxRetries = 2))
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    /**
     * 创建 Retrofit Service 接口的代理实例
     *
     * @param baseUrl API 基础地址（必须以 "/" 结尾）
     * @param serviceClass Service 接口的 Class 对象
     * @return Retrofit 动态代理生成的 Service 实例
     */
    fun <T> createService(
        baseUrl: String,
        serviceClass: Class<T>
    ): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
            .create(serviceClass)
    }
}
