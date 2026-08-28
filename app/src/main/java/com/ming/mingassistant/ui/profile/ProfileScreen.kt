package com.ming.mingassistant.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ming.mingassistant.data.ApiClient
import com.ming.mingassistant.data.Session

@Composable
fun ProfileScreen(
    session: Session,
    factory: androidx.lifecycle.ViewModelProvider.Factory,
) {
    val vm: ProfileViewModel = viewModel(factory = factory)
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("个人中心", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("账号信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                InfoRow("用户名", session.username)
                InfoRow("我的B站UID", session.bilibiliUid.ifBlank { "未填写" })
                InfoRow("账户ID", session.userId.toString())
                InfoRow("推送状态", "已登录，后台开播提醒每15分钟检查一次")
            }
        }

        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("合规与法律文件", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LinkRow("用户协议", onOpen = {
                    context.startActivity(openWeb("${privacyBase}/user-agreement.html"))
                })
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                LinkRow("隐私政策（含隐私设置、账号注销等）", onOpen = {
                    context.startActivity(openWeb("${privacyBase}/privacy-policy.html"))
                })
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                LinkRow("关于与全部合规文件（个人信息收集清单、第三方SDK清单、权限使用说明、第三方信息共享清单）", onOpen = {
                    context.startActivity(openWeb("${privacyBase}/index.html"))
                })
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = { vm.logout { } },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("退出登录", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("注销账号", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Ming-Assistant v1.0\n专门用于推送 明明Uncle 的开播状态、管理直播歌单与舰礼登记。\n开发者：个人开发者　联系邮箱：1640053235@qq.com",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!vm.deleting) showDeleteDialog = false },
            title = { Text("注销账号") },
            text = {
                Text(
                    "注销将删除您的账号及账号下的全部舰礼、歌单数据，且不可恢复。\n" +
                        "确认继续吗？${vm.deleteError?.let { "\n\n操作失败：$it" } ?: ""}",
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !vm.deleting,
                    onClick = {
                        vm.deleteAccount {
                            showDeleteDialog = false
                        }
                    },
                ) {
                    Text(if (vm.deleting) "注销中…" else "确认注销", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, enabled = !vm.deleting) {
                    Text("取消")
                }
            },
        )
    }
}

private val privacyBase: String get() = ApiClient.BASE_URL.trimEnd('/') + "/privacy"

private fun openWeb(url: String) = Intent(Intent.ACTION_VIEW, Uri.parse(url))

@Composable
private fun LinkRow(label: String, onOpen: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onOpen() }.padding(vertical = 10.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}