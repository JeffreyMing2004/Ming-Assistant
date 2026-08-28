package com.ming.mingassistant.data

class LiveRepository(
    private val api: ApiService = ApiClient.service,
) {
    suspend fun fetchStatus(): Result<LiveStatus> = ApiCall.call { api.liveStatus() }
}