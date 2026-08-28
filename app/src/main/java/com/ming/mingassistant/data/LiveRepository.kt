package com.ming.mingassistant.data

class LiveRepository(
    private val api: ApiService = ApiClient.service,
) {
    suspend fun fetchStatus(): Result<LiveStatus> = ApiCall.call { api.liveStatus() }

    suspend fun fetchAnnouncement(): Result<AnnouncementResponse> = ApiCall.call { api.announcement() }

    suspend fun fetchAppVersion(): Result<AppVersionResponse> = ApiCall.call { api.appVersion() }
}