package com.ming.mingassistant.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // ---- Auth ----
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): AuthResponse

    @DELETE("api/auth/account")
    suspend fun deleteAccount()

    // ---- Live ----
    @GET("api/live/status")
    suspend fun liveStatus(): LiveStatus

    // ---- Gifts ----
    @GET("api/gifts")
    suspend fun gifts(): List<GiftRecord>

    @POST("api/gifts")
    suspend fun createGift(@Body body: GiftRequest): GiftRecord

    @DELETE("api/gifts/{id}")
    suspend fun deleteGift(@Path("id") id: Long)

    // ---- Songs ----
    @GET("api/songs")
    suspend fun songs(): List<Song>

    @POST("api/songs")
    suspend fun createSong(@Body body: SongRequest): Song

    @DELETE("api/songs/{id}")
    suspend fun deleteSong(@Path("id") id: Long)

    @POST("api/songs/qq/preview")
    suspend fun qqPreview(@Body body: QqPlaylistRequest): QqPreview

    @POST("api/songs/qq/import")
    suspend fun qqImport(@Body body: QqPlaylistRequest): QqImportResult
}