package com.ming.mingassistant.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ming.mingassistant.data.LiveStatus
import com.ming.mingassistant.ui.theme.BilibiliPink

@Composable
fun HomeScreen(factory: androidx.lifecycle.ViewModelProvider.Factory) {
    val vm: HomeViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("首页", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "明明Uncle 直播状态",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        when {
            state.loading && state.status == null -> {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.status != null -> {
                LiveStatusCard(
                    status = state.status!!,
                    onOpenLive = { url -> openBrowser(context, url) },
                )
            }
        }

        state.error?.let { error ->
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = vm::refresh,
            enabled = !state.refreshing,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.refreshing) "刷新中..." else "刷新状态")
        }

        Spacer(Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("关于后台提醒", style = MaterialTheme.typography.titleSmall)
                Text(
                    "登录后 App 会每隔约15分钟自动检查一次直播间状态，检测到开播时发送系统通知。\n" +
                            "后台轮询受 Android 省电策略影响，属于尽力而为，建议以首页手动刷新为准。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LiveStatusCard(status: LiveStatus, onOpenLive: (String) -> Unit) {
    val live = status.liveStatus == 1
    val textColor = if (live) BilibiliPink else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (live) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (live) BilibiliPink else Color.Gray),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    when (status.liveStatus) {
                        1 -> "直播中"
                        2 -> "轮播中"
                        else -> "未开播"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
            }

            if (status.cover.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = status.cover,
                    contentDescription = "直播间封面",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                status.title.ifBlank { "暂无直播标题" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "房间号 ${status.roomId} · 人气 ${status.online}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (live && status.url.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = { onOpenLive(status.url) }, modifier = Modifier.fillMaxWidth()) {
                    Text("进入直播间")
                }
            }
        }
    }
}

private fun openBrowser(context: android.content.Context, url: String) {
    val trimmed = if (url.startsWith("//")) "https:$url" else url
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)))
    }.onFailure { e ->
        android.util.Log.e("HomeScreen", "无法打开直播间链接", e)
    }
}