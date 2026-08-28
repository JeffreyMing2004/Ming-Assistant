package com.ming.mingassistant.data

import java.io.IOException

/** Runs a suspend API call and normalizes failures into Result with a user-friendly message. */
object ApiCall {

    private fun messageFrom(e: Exception): String = when (e) {
        is retrofit2.HttpException -> {
            val body = e.response()?.errorBody()?.string()
            val msg = body?.let {
                try {
                    ApiClient.json.decodeFromString<ApiError>(it).message
                } catch (_: Exception) {
                    null
                }
            }
            msg ?: "请求失败(${e.code()})"
        }
        is IOException -> "网络连接失败，请检查网络"
        else -> "出错了：${e.message ?: e.javaClass.simpleName}"
    }

    suspend fun <T> call(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(IllegalStateException(messageFrom(e as? Exception ?: e), e))
    }
}