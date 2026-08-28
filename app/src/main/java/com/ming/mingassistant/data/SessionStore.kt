package com.ming.mingassistant.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class Session(
    val token: String,
    val userId: Long,
    val username: String,
    val bilibiliUid: String,
)

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val BILIBILI_UID = stringPreferencesKey("bilibili_uid")
        val LIVE_STATUS = stringPreferencesKey("live_status")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }

    val session: Flow<Session?> = context.dataStore.data.map { prefs ->
        val token = prefs[Keys.TOKEN] ?: return@map null
        val userId = prefs[Keys.USER_ID]?.toLongOrNull() ?: return@map null
        Session(
            token = token,
            userId = userId,
            username = prefs[Keys.USERNAME].orEmpty(),
            bilibiliUid = prefs[Keys.BILIBILI_UID].orEmpty(),
        )
    }

    val liveStatusFlag: Flow<Int> = context.dataStore.data.map { it[Keys.LIVE_STATUS]?.toIntOrNull() ?: -1 }

    suspend fun saveLogin(response: AuthResponse) {
        val token = response.token ?: return
        AuthHolder.token = token
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USER_ID] = response.userId.toString()
            prefs[Keys.USERNAME] = response.username
            prefs[Keys.BILIBILI_UID] = response.bilibiliUid
        }
    }

    suspend fun saveLiveStatus(status: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.LIVE_STATUS] = status.toString() }
    }

    suspend fun loadTokenOnce(): String? = token.first()

    suspend fun clear() {
        AuthHolder.token = null
        context.dataStore.edit { it.clear() }
    }
}