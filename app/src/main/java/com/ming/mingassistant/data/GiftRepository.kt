package com.ming.mingassistant.data

class GiftRepository(
    private val api: ApiService = ApiClient.service,
) {
    suspend fun list(): Result<List<GiftRecord>> = ApiCall.call { api.gifts() }

    suspend fun create(nickname: String, bilibiliUid: String, giftType: String): Result<GiftRecord> =
        ApiCall.call { api.createGift(GiftRequest(nickname, bilibiliUid, giftType)) }

    suspend fun delete(id: Long): Result<Unit> = ApiCall.call { api.deleteGift(id) }
}