package com.ming.mingassistant.ui.gifts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ming.mingassistant.data.GiftRecord

@Composable
fun GiftsScreen(
    factory: androidx.lifecycle.ViewModelProvider.Factory,
    ownUid: String,
) {
    val vm: GiftsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val canAddGift = ownUid.isNotBlank()

    Scaffold(
        floatingActionButton = {
            if (canAddGift) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "登记") },
                    text = { Text("登记舰礼") },
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            Text("舰礼收集", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "记录展会 / 直播中收到的舰长提督等",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            if (!canAddGift) {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        "无法登记舰礼：您注册时未填写本人B站UID。\n请使用填写了B站UID的账号重新注册后再登记舰礼。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            if (state.gifts.isNotEmpty()) {
                Text(
                    "共 ${state.gifts.size} 条记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            if (state.loading && state.gifts.isEmpty()) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.gifts, key = { it.id }) { gift ->
                        GiftRow(gift = gift, onDelete = { vm.delete(gift) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGiftDialog(
            submitting = state.submitting,
            onDismiss = { showAddDialog = false },
            onConfirm = { nickname, uid, type ->
                vm.add(nickname, uid, type) { error ->
                    if (error == null) showAddDialog = false
                }
            },
        )
    }
}

@Composable
private fun GiftRow(gift: GiftRecord, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(gift.nickname, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Text(
                        gift.giftType,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                gift.bilibiliUid.takeIf { it.isNotBlank() }?.let {
                    Text(
                        "UID $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddGiftDialog(
    submitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
) {
    var nickname by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf("") }
    var giftType by remember { mutableStateOf("舰长") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("登记舰礼") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("用户名 / 昵称 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = uid,
                    onValueChange = { uid = it },
                    label = { Text("B站UID（可选）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = giftType,
                    onValueChange = { giftType = it },
                    label = { Text("礼物类型 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nickname.isNotBlank() && giftType.isNotBlank()) {
                        onConfirm(nickname.trim(), uid.trim(), giftType.trim())
                    }
                },
                enabled = nickname.isNotBlank() && giftType.isNotBlank() && !submitting,
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) { Text("取消") }
        },
    )
}