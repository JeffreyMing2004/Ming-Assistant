package com.ming.mingassistant.data

class SongRepository(
    private val api: ApiService = ApiClient.service,
) {
    suspend fun list(): Result<List<Song>> = ApiCall.call { api.songs() }

    suspend fun create(title: String, artist: String, note: String): Result<Song> =
        ApiCall.call { api.createSong(SongRequest(title, artist, note)) }

    suspend fun delete(id: Long): Result<Unit> = ApiCall.call { api.deleteSong(id) }
}