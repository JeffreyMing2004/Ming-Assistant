package com.ming.mingassistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.ming.mingassistant.data.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.ming.mingassistant.data.AuthHolder

class MingApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        warmUpSession()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_LIVE,
            "直播提醒",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "明鸽子 开播提醒" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun warmUpSession() {
        val store = SessionStore(this)
        appScope.launch {
            store.token.collect { token -> AuthHolder.token = token }
        }
    }

    companion object {
        const val CHANNEL_LIVE = "live"
    }
}