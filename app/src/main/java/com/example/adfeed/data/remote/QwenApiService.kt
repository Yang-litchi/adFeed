package com.example.adfeed.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Qwen API (DashScope) Retrofit 服务接口
 *
 * 定义了与阿里云通义千问大模型 API 的 HTTP 通信契约。
 * 当前用于 [QwenApi] 的可选 Retrofit 通路（默认使用 OkHttp 直连方案）。
 *
 * 当未来 API 调用场景增多（如标签分析、推荐排序等独立接口）时，
 * 可通过此接口 + [com.example.adfeed.core.network.NetworkClient.createService]
 * 快速创建类型安全的 API 客户端。
 */
interface QwenApiService {

    /**
     * 调用通义千问 chat/completions 接口
     *
     * @param body 完整的 JSON 请求体（由调用方使用 JSONObject 构建）
     * @return [ResponseBody] 原始响应体，由调用方自行解析 JSON
     */
    @POST("chat/completions")
    @Headers("Content-Type: application/json")
    fun chat(@Body body: RequestBody): Call<ResponseBody>
}
