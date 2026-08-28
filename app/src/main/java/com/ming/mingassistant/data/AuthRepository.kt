package com.ming.mingassistant.data

/** Wraps auth persistence so Ui/login/register logic stays in the repository. */
class AuthRepository(
    private val sessionStore: SessionStore,
    private val api: ApiService = ApiClient.service,
) {

    suspend fun login(username: String, password: String): Result<Session> {
        val result = ApiCall.call { api.login(LoginRequest(username, password)) }
        return result.map { persist(it) }
    }

    suspend fun register(
        bilibiliUid: String,
        username: String,
        password: String,
        confirmPassword: String,
    ): Result<Session> {
        val result = ApiCall.call {
            api.register(RegisterRequest(bilibiliUid, username, password, confirmPassword))
        }
        return result.map { persist(it) }
    }

    suspend fun logout() = sessionStore.clear()

    private suspend fun persist(response: AuthResponse): Session {
        sessionStore.saveLogin(response)
        val token = response.token ?: ""
        return Session(token, response.userId, response.username, response.bilibiliUid)
    }
}