package com.ming.mingassistant.data

import kotlinx.serialization.Serializable

// ---- Auth ----
@Serializable
data class RegisterRequest(
    val bilibiliUid: String,
    val username: String,
    val password: String,
    val confirmPassword: String,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val userId: Long,
    val username: String,
    val bilibiliUid: String,
    val token: String? = null,
)

/** 更新用户 B站UID（设置页填写，用于获取 B站头像）。 */
@Serializable
data class UpdateUidRequest(
    val bilibiliUid: String,
)

/** B站用户头像 URL，获取失败/未填 UID 时为空串。 */
@Serializable
data class AvatarResponse(
    val face: String = "",
)

// ---- Live ----
@Serializable
data class LiveStatus(
    val roomId: Long,
    val liveStatus: Int,
    val title: String,
    val cover: String = "",
    val online: Long,
    val url: String,
    val checkedAt: String,
    val live: Boolean = false,
)

// ---- Gifts ----
@Serializable
data class GiftRequest(
    val nickname: String,
    val bilibiliUid: String = "",
    val phone: String = "",
    val address: String = "",
    val giftType: String,
)

@Serializable
data class GiftRecord(
    val id: Long,
    val userId: Long,
    val nickname: String,
    val bilibiliUid: String = "",
    val phone: String = "",
    val address: String = "",
    val giftType: String,
    val trackingNumber: String? = "",
    val createdAt: String,
)

// ---- Songs ----
@Serializable
data class SongRequest(
    val title: String,
    val artist: String = "",
    val note: String = "",
)

@Serializable
data class Song(
    val id: Long,
    val userId: Long,
    val title: String,
    val artist: String = "",
    val note: String = "",
    val createdAt: String,
)

// ---- Error ----
@Serializable
data class ApiError(val message: String? = null)

/** Holds the current JWT in memory for the OkHttp interceptor. */
object AuthHolder {
    @Volatile
    var token: String? = null
}