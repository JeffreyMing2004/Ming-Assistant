package com.ming.mingassistant.work

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ming.mingassistant.MingApplication
import com.ming.mingassistant.R
import com.ming.mingassistant.data.ApiClient
import com.ming.mingassistant.data.AuthHolder
import com.ming.mingassistant.data.SessionStore
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/**
 * 周期轮询明明Uncle的直播状态，从未开播变为开播时发送系统通知。
 * 周期下限为 15 分钟（WorkManager 系统限制），Doze 省电下并不严格准时。
 */
class LiveStatusWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val sessionStore = SessionStore(applicationContext)
        val token = sessionStore.loadTokenOnce() ?: return Result.success()
        AuthHolder.token = token
        val previous = sessionStore.liveStatusFlag.first()
        return try {
            val status = ApiClient.service.liveStatus()
            sessionStore.saveLiveStatus(status.liveStatus)
            if (status.liveStatus == 1 && previous != 1) {
                notifyLive(status.title.ifBlank { "快来看看吧" }, status.url)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun notifyLive(text: String, url: String) {
        val trimmedUrl = if (url.startsWith("//")) "https:$url" else url
        val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(trimmedUrl))
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pending = PendingIntent.getActivity(applicationContext, 0, openIntent, flags)

        val notification = NotificationCompat.Builder(applicationContext, MingApplication.CHANNEL_LIVE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("明明Uncle 开播了！")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val WORK_NAME = "live-status-polling"
        private const val NOTIFICATION_ID = 1001

        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<LiveStatusWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}