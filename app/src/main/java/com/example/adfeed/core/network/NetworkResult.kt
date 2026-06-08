package com.example.adfeed.core.network

/**
 * 统一网络请求结果封装
 *
 * 将每次 HTTP 请求的结果归为三种情况：
 * - [Success]：请求成功，携带解析后的业务数据
 * - [ApiError]：服务器返回了错误响应（4xx/5xx），包含状态码和错误信息
 * - [NetworkError]：网络层面的异常（超时、DNS 解析失败等）
 *
 * 调用方可以通过 when 表达式精确处理每种情况：
 * ```kotlin
 * when (result) {
 *     is NetworkResult.Success -> showData(result.data)
 *     is NetworkResult.ApiError -> showServerError(result.code, result.message)
 *     is NetworkResult.NetworkError -> showNetworkError(result.message)
 * }
 * ```
 */
sealed class NetworkResult<out T> {

    /** 请求成功，携带解析后的业务数据 */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * 服务器返回了错误响应
     * @param code HTTP 状态码（如 400、401、500）
     * @param message 错误描述信息
     * @param body 原始错误响应体（调试用，可能为 null）
     */
    data class ApiError(
        val code: Int,
        val message: String,
        val body: String? = null
    ) : NetworkResult<Nothing>()

    /**
     * 网络层面的异常（连接超时、DNS 解析失败、无网络等）
     * @param throwable 原始异常对象
     * @param message 用户可读的错误描述
     */
    data class NetworkError(
        val throwable: Throwable,
        val message: String = DEFAULT_NETWORK_ERROR_MSG
    ) : NetworkResult<Nothing>()

    companion object {
        const val DEFAULT_NETWORK_ERROR_MSG = "网络连接失败，请稍后重试"
    }
}
