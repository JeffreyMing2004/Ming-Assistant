package com.ming.mingassistant.data

class SongRepository(
    private val api: ApiService = ApiClient.service,
) {
    suspend fun list(): Result<List<Song>> = ApiCall.call { api.songs() }
}