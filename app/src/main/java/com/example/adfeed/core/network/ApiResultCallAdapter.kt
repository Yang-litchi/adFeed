package com.example.adfeed.core.network

import com.example.adfeed.core.network.NetworkResult.ApiError
import com.example.adfeed.core.network.NetworkResult.NetworkError
import com.example.adfeed.core.network.NetworkResult.Success
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Retrofit CallAdapter，将 [Call<T>] 适配为 [Call<NetworkResult<T>>]
 *
 * 核心作用：在执行 Retrofit 请求时，自动将以下三种情况转换为统一的 [NetworkResult]：
 * - HTTP 2xx 响应 + 成功解析 → [Success]
 * - HTTP 4xx/5xx 响应 → [ApiError]
 * - 网络 I/O 异常（超时、断连等）→ [NetworkError]
 *
 * 使用时通过 [ResultCallAdapterFactory] 注册到 Retrofit：
 * ```kotlin
 * Retrofit.Builder()
 *     .addCallAdapterFactory(ResultCallAdapterFactory())
 *     .build()
 * ```
 */
class ResultCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        // 只处理返回类型为 Call<NetworkResult<*>> 的接口方法
        if (getRawType(returnType) != Call::class.java) return null
        check(returnType is ParameterizedType) { "返回类型必须是参数化类型" }

        val callType = getParameterUpperBound(0, returnType)
        if (getRawType(callType) != NetworkResult::class.java) return null
        check(callType is ParameterizedType) { "NetworkResult 必须是参数化类型" }

        val successType = getParameterUpperBound(0, callType)
        return ApiResultCallAdapter<Any>(successType)
    }
}

/**
 * 将 [Call<T>] 包装为 [Call<NetworkResult<T>>]
 */
private class ApiResultCallAdapter<T : Any>(
    private val successType: Type
) : CallAdapter<T, Call<NetworkResult<T>>> {

    override fun responseType(): Type = successType

    override fun adapt(call: Call<T>): Call<NetworkResult<T>> = ApiResultCall(call)
}

/**
 * 包装后的 Call 实现，执行时自动转换结果为 [NetworkResult]
 */
private class ApiResultCall<T : Any>(
    private val delegate: Call<T>
) : Call<NetworkResult<T>> {

    override fun enqueue(callback: Callback<NetworkResult<T>>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onResponse(
                    this@ApiResultCall,
                    Response.success(response.toNetworkResult())
                )
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onResponse(
                    this@ApiResultCall,
                    Response.success(NetworkError(t))
                )
            }
        })
    }

    override fun execute(): Response<NetworkResult<T>> {
        return try {
            val response = delegate.execute()
            Response.success(response.toNetworkResult())
        } catch (e: IOException) {
            Response.success(NetworkError(e))
        }
    }

    /**
     * 将 Retrofit 的 [Response<T>] 转换为 [NetworkResult<T>]
     */
    private fun Response<T>.toNetworkResult(): NetworkResult<T> {
        val body = body()
        return if (isSuccessful && body != null) {
            Success(body)
        } else {
            val errorBody = try {
                errorBody()?.string()
            } catch (e: Exception) {
                null
            }
            ApiError(
                code = code(),
                message = "请求失败 (HTTP ${code()})",
                body = errorBody
            )
        }
    }

    override fun clone(): Call<NetworkResult<T>> = ApiResultCall(delegate.clone())
    override fun isExecuted(): Boolean = delegate.isExecuted
    override fun cancel() = delegate.cancel()
    override fun isCanceled(): Boolean = delegate.isCanceled
    override fun request(): Request = delegate.request()
    override fun timeout(): Timeout = delegate.timeout()
}
