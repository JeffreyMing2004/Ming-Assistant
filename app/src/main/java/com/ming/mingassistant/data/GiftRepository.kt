package com.ming.mingassistant.data

class GiftRepository(
    private val api: ApiService = ApiClient.service,
) {
    suspend fun list(): Result<List<GiftRecord>> = ApiCall.call { api.gifts() }

    suspend fun create(nickname: String, bilibiliUid: String, phone: String, address: String, giftType: String): Result<GiftRecord> =
        ApiCall.call { api.createGift(GiftRequest(nickname, bilibiliUid, phone, address, giftType)) }

    suspend fun delete(id: Long): Result<Unit> = ApiCall.call { api.deleteGift(id) }
}