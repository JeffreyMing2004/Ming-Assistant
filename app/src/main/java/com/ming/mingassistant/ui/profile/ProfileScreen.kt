package com.ming.mingassistant.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ming.mingassistant.data.Session

// ---- 个人中心局部配色（不改全局主题） ----
private val PageBg = Color(0xFFFAF8FF)
private val CardWhite = Color(0xFFFFFFFF)
private val AccentPurple = Color(0xFF7C4DFF)
private val LightPurple = Color(0xFFF3EDFF)
private val TextPrimary = Color(0xFF1D1B20)
private val TextSecondary = Color(0xFF6F6B78)
private val DangerRed = Color(0xFFD93025)
private val LightRedBg = Color(0xFFFDECEC)
private val SuccessGreen = Color(0xFF2E7D32)
private val DividerVeryLight = Color(0xFFF0ECF6)

/** Document / Article 图标（自绘，material-icons-core 无此图形） */
private val DocumentIcon: ImageVector by lazy {
    ImageVector.Builder("ic_document", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 2f)
            horizontalLineToRelative(7f)
            lineToRelative(5f, 5f)
            verticalLineToRelative(13f)
            horizontalLineToRelative(-12f)
            close()
            moveTo(13f, 2f)
            verticalLineToRelative(5f)
            horizontalLineToRelative(5f)
            close()
            moveTo(7f, 13f)
            horizontalLineToRelative(10f)
            verticalLineToRelative(1.5f)
            horizontalLineToRelative(-10f)
            close()
            moveTo(7f, 9.5f)
            horizontalLineToRelative(10f)
            verticalLineToRelative(1.5f)
            horizontalLineToRelative(-10f)
            close()
        }
    }.build()
}

/** Security / Shield 图标（自绘） */
private val ShieldIcon: ImageVector by lazy {
    ImageVector.Builder("ic_shield", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 1.5f)
            lineToRelative(8.5f, 3.5f)
            verticalLineToRelative(6f)
            curveToRelative(0f, 5.5f, -3.6f, 10.4f, -8.5f, 11.5f)
            curveToRelative(-4.9f, -1.1f, -8.5f, -6f, -8.5f, -11.5f)
            verticalLineToRelative(-6f)
            close()
            moveTo(10.4f, 14.1f)
            lineToRelative(-1.9f, -2.4f)
            lineToRelative(-1.6f, 1.2f)
            lineToRelative(3.5f, 4.5f)
            lineToRelative(5.2f, -5.2f)
            lineToRelative(-1.6f, -1.6f)
            close()
        }
    }.build()
}

@Composable
fun ProfileScreen(
    session: Session,
    factory: androidx.lifecycle.ViewModelProvider.Factory,
) {
    val vm: ProfileViewModel = viewModel(factory = factory)
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    // 原生文档查看器：导航栈支持从「关于与全部合规文件」进入各合规子页
    val docStack = remember { mutableStateListOf<DocPage>() }
    BackHandler(enabled = docStack.isNotEmpty() || showSettings) {
        when {
            docStack.isNotEmpty() -> if (docStack.size > 1) docStack.removeLast() else docStack.clear()
            showSettings -> showSettings = false
        }
    }
    // 按 B站UID 尝试获取头像；未填写 UID 则不获取（保持默认头像）
    LaunchedEffect(session.bilibiliUid) { vm.refreshAvatar(session.bilibiliUid) }

    Box(
        Modifier
            .fillMaxSize()
            .background(PageBg),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            ProfileHeader(onOpenSettings = { showSettings = true })
            Spacer(Modifier.height(20.dp))

            ProfileAccountCard(session, avatarUrl = vm.avatarUrl)
            Spacer(Modifier.height(20.dp))

            ProfileActionButtons(
                onLogout = { showLogoutDialog = true },
                onDelete = { showDeleteDialog = true },
            )
            Spacer(Modifier.height(24.dp))

            AppInfo()
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showSettings) {
        SettingsScreen(
            currentUid = session.bilibiliUid,
            vm = vm,
            onOpenDoc = { docStack.add(it) },
            onBack = { showSettings = false },
        )
    }

    // 文档查看器排在最后：从设置页打开文档时需盖在设置页之上
    if (docStack.isNotEmpty()) {
        DocumentViewerScreen(
            page = docStack.last(),
            onBack = {
                if (docStack.size > 1) docStack.removeLast() else docStack.clear()
            },
            onOpenDoc = { docStack.add(it) },
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出登录？", fontWeight = FontWeight.SemiBold) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        vm.logout { }
                    },
                ) { Text("退出登录", color = AccentPurple) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("取消") }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!vm.deleting) showDeleteDialog = false },
            title = { Text("注销账号", fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "注销账号后，您的账号及账号下的相关数据可能无法恢复。确定要继续吗？" +
                        (vm.deleteError?.let { "\n\n操作失败：$it" } ?: ""),
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
                    Text(if (vm.deleting) "注销中…" else "确认注销", color = DangerRed)
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

@Composable
private fun ProfileHeader(onOpenSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("个人中心", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("管理您的账户与服务信息", fontSize = 14.sp, color = TextSecondary)
        }
        Spacer(Modifier.width(16.dp))
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LightPurple)
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "设置", tint = AccentPurple, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun ProfileAccountCard(session: Session, avatarUrl: String?) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            // 顶部用户信息
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LightPurple),
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "头像",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = "头像", tint = AccentPurple, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            session.username.ifBlank { "未设置用户名" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(LightPurple)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text("用户", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AccentPurple)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "B站 UID：${session.bilibiliUid.ifBlank { "未填写" }} · 账户 ID：${session.userId}",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = DividerVeryLight)
            Spacer(Modifier.height(16.dp))

            // 2×2 数据区
            Row(Modifier.fillMaxWidth()) {
                ProfileInfoItem(
                    icon = Icons.Filled.AccountCircle,
                    title = "账户 ID",
                    value = session.userId.toString(),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                ProfileInfoItem(
                    icon = Icons.Filled.Star,
                    title = "B站 UID",
                    value = session.bilibiliUid.ifBlank { "未填写" },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                ProfileInfoItem(
                    icon = Icons.Filled.CheckCircle,
                    title = "登录状态",
                    value = "已登录",
                    valueColor = SuccessGreen,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                ProfileInfoItem(
                    icon = Icons.Filled.Notifications,
                    title = "推送状态",
                    value = "正常",
                    valueColor = SuccessGreen,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LightPurple),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = title, tint = AccentPurple, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 12.sp, color = TextSecondary)
            Text(
                value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProfileStatusNotice() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightPurple)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Notifications, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            "已登录，后台开播提醒每15分钟检查一次",
            fontSize = 13.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

/** 设置页：B站 UID 填写入口（用于获取 B站头像）+ 通知与推送说明（从个人中心移入）。 */
@Composable
private fun SettingsScreen(
    currentUid: String,
    vm: ProfileViewModel,
    onOpenDoc: (DocPage) -> Unit,
    onBack: () -> Unit,
) {
    var uid by remember(currentUid) { mutableStateOf(currentUid) }
    val uidError = remember(uid) {
        if (uid.isNotBlank() && !uid.matches(Regex("^\\d{0,20}$"))) "UID必须为数字（最多20位）" else null
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(PageBg),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(CardWhite)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = AccentPurple,
                    )
                }
                Text(
                    "设置",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(48.dp))
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("账号与信息", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(12.dp))
                        Text("B站 UID", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uid,
                            onValueChange = { uid = it },
                            singleLine = true,
                            isError = uidError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("例如 88888", color = TextSecondary) },
                            supportingText = {
                                Text(
                                    uidError ?: "填写后将在个人中心获取 B 站头像；未填写则不获取。",
                                    color = if (uidError != null) DangerRed else TextSecondary,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(12.dp))
                        vm.saveError?.let {
                            Text(it, fontSize = 13.sp, color = DangerRed)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (vm.saved && currentUid == uid.trim()) {
                            Text("已保存，正在更新头像…", fontSize = 13.sp, color = SuccessGreen)
                            Spacer(Modifier.height(8.dp))
                        }
                        Button(
                            onClick = {
                                vm.updateBilibiliUid(uid) { }
                            },
                            enabled = !vm.saving && uidError == null && uid.trim() != currentUid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPurple,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text(if (vm.saving) "保存中…" else "保存 B站 UID")
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("通知与推送", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(12.dp))
                        ProfileStatusNotice()
                    }
                }
                Spacer(Modifier.height(20.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text("法律与隐私", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        LegalDocumentItem(
                            icon = DocumentIcon,
                            title = "用户协议",
                            onClick = { onOpenDoc(userAgreementPage) },
                        )
                        HorizontalDivider(color = DividerVeryLight)
                        LegalDocumentItem(
                            icon = ShieldIcon,
                            title = "隐私政策",
                            subtitle = "含隐私设置、账号注销等",
                            onClick = { onOpenDoc(privacyPolicyPage) },
                        )
                        HorizontalDivider(color = DividerVeryLight)
                        LegalDocumentItem(
                            icon = DocumentIcon,
                            title = "关于与全部合规文件",
                            subtitle = "个人信息收集清单、第三方SDK清单、权限使用说明、第三方信息共享清单",
                            onClick = { onOpenDoc(compliancePage) },
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LegalDocumentItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 68.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(LightPurple),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = title, tint = AccentPurple, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            subtitle?.let {
                Text(it, fontSize = 12.sp, color = TextSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ProfileActionButtons(
    onLogout: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = LightPurple,
                contentColor = AccentPurple,
            ),
            border = BorderStroke(1.dp, AccentPurple),
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("退出登录", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = LightRedBg,
                contentColor = DangerRed,
            ),
            border = BorderStroke(1.dp, DangerRed),
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("注销账号", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AppInfo() {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Ming-Assistant v1.0", fontSize = 12.sp, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            "专门用于推送 明明Uncle 的开播状态、管理直播歌单与舰礼登记。",
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "开发者：个人开发者　联系邮箱：1640053235@qq.com",
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/** App 内文档查看器：纯 Compose 原生渲染，不加载网页 */
@Composable
private fun DocumentViewerScreen(
    page: DocPage,
    onBack: () -> Unit,
    onOpenDoc: (DocPage) -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(PageBg),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(CardWhite)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = AccentPurple,
                    )
                }
                Text(
                    page.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(48.dp))
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                page.meta?.let { DocMetaText(it) }
                page.blocks.forEach { block ->
                    when (block) {
                        is DocBlock.Heading -> DocHeadingText(block.text, block.level)
                        is DocBlock.Meta -> DocMetaText(block.text)
                        is DocBlock.Paragraph -> DocParagraphText(block.text)
                        is DocBlock.Bullets -> DocBulletsList(block.items)
                        is DocBlock.Table -> DocTableBlock(block)
                        is DocBlock.Links -> block.items.forEachIndexed { index, link ->
                            DocLinkItem(link = link, onClick = { onOpenDoc(link.target) })
                            if (index < block.items.lastIndex) Spacer(Modifier.height(6.dp))
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun DocHeadingText(text: String, level: Int) {
    val fontSize = if (level == 1) 18.sp else 15.sp
    val topPad = if (level == 1) 20.dp else 14.dp
    Text(
        text,
        fontSize = fontSize,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        modifier = Modifier.padding(top = topPad, bottom = 8.dp),
    )
}

@Composable
private fun DocMetaText(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

@Composable
private fun DocParagraphText(text: String) {
    Text(
        text,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = TextPrimary,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
private fun DocBulletsList(items: List<String>) {
    items.forEach { item ->
        Row(Modifier.padding(bottom = 6.dp)) {
            Text(
                "•",
                color = AccentPurple,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 4.dp, end = 8.dp),
            )
            Text(
                item,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun DocTableBlock(table: DocBlock.Table) {
    table.rows.forEach { row ->
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = LightPurple),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    row.firstOrNull() ?: "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                for (i in 1 until row.size) {
                    val value = row.getOrElse(i) { "" }
                    if (value.isBlank() || value == "—") continue
                    Row(Modifier.padding(top = 4.dp)) {
                        Text(
                            table.headers.getOrElse(i) { "" },
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.widthIn(max = 96.dp),
                        )
                        Text("：", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            value,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun DocLinkItem(link: DocLink, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(link.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    link.description,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}