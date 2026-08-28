package com.ming.mingassistant

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.ming.mingassistant.data.SessionStore
import com.ming.mingassistant.ui.AppRoot
import com.ming.mingassistant.ui.theme.MingAssistantTheme
import com.ming.mingassistant.work.LiveStatusWorker
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {

    private val sessionStore by lazy { SessionStore(this) }

    private val notificationPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        requestNotificationPermission()
        scheduleLiveWorker()

        setContent {
            MingAssistantTheme {
                val session by sessionStore.session
                    .map { it }
                    .distinctUntilChanged()
                    .collectAsState(initial = null)

                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(session = session)
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun scheduleLiveWorker() {
        LiveStatusWorker.schedulePeriodic(this)
    }
}