package com.ming.mingassistant.ui.gifts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextAlign
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
                "在直播间开通大航海（舰长 / 提督 / 总督）后登记收货地址，发货后可在此查询快递单号",
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
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                }
            }

            when {
                state.loading && state.gifts.isEmpty() -> {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "正在加载舰礼记录…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                state.gifts.isEmpty() -> {
                    EmptyState(
                        isError = state.error != null,
                        message = state.error,
                        onRetry = { vm.load() },
                    )
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.gifts, key = { it.id }) { gift ->
                            GiftRow(gift = gift, onDelete = { vm.delete(gift) })
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGiftDialog(
            submitting = state.submitting,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, uid, phone, address, type ->
                vm.add(name, uid, phone, address, type) { error ->
                    if (error == null) showAddDialog = false
                }
            },
        )
    }
}

@Composable
private fun EmptyState(
    isError: Boolean,
    message: String?,
    onRetry: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            if (isError) Icons.Filled.Warning else Icons.Filled.List,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (isError) "加载失败" else "暂无提交记录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (isError && !message.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onRetry) { Text("重试") }
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
                gift.phone.takeIf { it.isNotBlank() }?.let {
                    Text(
                        "联系电话 $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                gift.address.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                gift.trackingNumber?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        "快递单号 $it",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
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
    onConfirm: (String, String, String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var giftType by remember { mutableStateOf("舰长") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("登记舰礼") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名 *") },
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
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("联系电话 *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("住址 *") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("大航海 *", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DeliveryTier.values().forEach { tier ->
                        FilterChip(
                            selected = giftType == tier.label,
                            onClick = { giftType = tier.label },
                            label = { Text(tier.label) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && address.isNotBlank() && giftType.isNotBlank()) {
                        onConfirm(name.trim(), uid.trim(), phone.trim(), address.trim(), giftType.trim())
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank() && address.isNotBlank() && giftType.isNotBlank() && !submitting,
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) { Text("取消") }
        },
    )
}

/** 大航海档位：舰长 / 提督 / 总督 */
private enum class DeliveryTier(val label: String) {
    CAPTAIN("舰长"),
    ADMIRAL("提督"),
    GOVERNOR("总督"),
}