package com.ming.mingassistant.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    var showUpdateDialog by remember { mutableStateOf(false) }

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
        Spacer(Modifier.height(12.dp))

        // 顶部滚动提示条（原「关于后台提醒」）
        TickerStrip(
            text = "后台开播提醒：登录后每15分钟自动检查直播间状态，开播即推送系统通知；" +
                    "后台轮询受系统省电策略影响，请以手动刷新为准。",
        )
        Spacer(Modifier.height(12.dp))

        // 今日公告（服务端/管理页维护，用于通知今日是否开播）
        AnnouncementCard(text = state.announcement, loaded = state.announcementLoaded)
        Spacer(Modifier.height(16.dp))

        // 在线版本检测：启动时检测，有新版本显示提示条
        if (state.hasUpdate && !state.updateDismissed) {
            UpdateBanner(
                latestVersion = state.latestVersion,
                onDismiss = vm::dismissUpdate,
                onClick = { showUpdateDialog = true },
            )
            Spacer(Modifier.height(16.dp))
        }

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
    }

    if (showUpdateDialog && state.hasUpdate) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("发现新版本 v${state.latestVersion}") },
            text = { Text(state.updateNote.ifBlank { "当前有新版本可用，建议更新到最新版。" }) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUpdateDialog = false
                        if (state.apkUrl.isNotBlank()) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.apkUrl)))
                        }
                    },
                ) {
                    Text(if (state.apkUrl.isNotBlank()) "去下载" else "知道了")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) { Text("稍后") }
            },
        )
    }
}

/** 顶部滚动文本条（跑马灯）：内容复制两份无缝循环左移。 */
@Composable
private fun TickerStrip(text: String) {
    val density = LocalDensity.current
    // 单份内容宽度（px），用于计算动画位移与时长
    var contentWidth by remember { mutableStateOf(0) }
    val transition = rememberInfiniteTransition(label = "ticker")
    // 移动「半份内容」后正好无缝接上（第二份即第一份的复本是时对齐）
    val fullDistance = contentWidth.toFloat() / 2f
    val durationMillis = if (fullDistance > 0f) {
        ((fullDistance / density.density) / 40f * 1000f).toInt() // 约 40dp/s
    } else {
        10_000
    }
    val offsetX by transition.animateFloat(
        initialValue = 0f,
        targetValue = -fullDistance,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "tickerOffset",
    )

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = null,
                tint = BilibiliPink,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .weight(1f)
                    .clipToBounds(),
            ) {
                Row(
                    Modifier
                        .onGloballyPositioned { contentWidth = it.size.width }
                        .graphicsLayer { translationX = offsetX },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.width(32.dp))
                    Text(
                        text,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

/** 今日公告卡：内容来自管理页在线编辑，用于通知今日是否开播。 */
@Composable
private fun AnnouncementCard(text: String, loaded: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("今日公告", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    !loaded -> "公告加载中…"
                    text.isBlank() -> "暂无公告"
                    else -> text
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 新版本提示条：点击弹出版本详情对话框，可关闭（本会话）。 */
@Composable
private fun UpdateBanner(
    latestVersion: String,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "发现新版本 v$latestVersion，点击查看",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "关闭提示",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
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