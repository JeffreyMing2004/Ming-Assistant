package com.ming.mingassistant.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ming.mingassistant.data.ApiClient
import com.ming.mingassistant.data.SessionStore
import com.ming.mingassistant.data.UpdateUidRequest
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val sessionStore: SessionStore,
) : ViewModel() {

    var deleting by mutableStateOf(false)
        private set
    var deleteError by mutableStateOf<String?>(null)
        private set

    /** B站头像 URL；未填 UID 或获取失败时为 null（显示默认头像）。 */
    var avatarUrl by mutableStateOf<String?>(null)
        private set

    var saving by mutableStateOf(false)
        private set
    var saveError by mutableStateOf<String?>(null)
        private set
    var saved by mutableStateOf(false)
        private set

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            sessionStore.clear()
            onDone()
        }
    }

    /** 注销账号：调用后端删除账号及全部数据（不可恢复），成功后清空本地会话并回到登录页。 */
    fun deleteAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            deleting = true
            deleteError = null
            try {
                ApiClient.service.deleteAccount()
                sessionStore.clear()
                onDone()
            } catch (e: Exception) {
                deleteError = e.message ?: "注销失败，请稍后重试"
            } finally {
                deleting = false
            }
        }
    }

    /** 按 B站UID 尝试获取头像链接；未填写 UID 或获取失败 → 置空（显示默认头像）。 */
    fun refreshAvatar(uid: String) {
        if (uid.isBlank()) {
            avatarUrl = null
            return
        }
        viewModelScope.launch {
            avatarUrl = try {
                ApiClient.service.bilibiliAvatar(uid.trim()).face.ifBlank { null }
            } catch (e: Exception) {
                null
            }
        }
    }

    /** 保存 B站UID：更新服务器与本地会话，成功后刷新头像。 */
    fun updateBilibiliUid(newUid: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            saving = true
            saveError = null
            saved = false
            try {
                val uid = newUid.trim()
                ApiClient.service.updateBilibiliUid(UpdateUidRequest(uid))
                sessionStore.saveBilibiliUid(uid)
                saved = true
                refreshAvatar(uid)
                onDone(true)
            } catch (e: Exception) {
                saveError = e.message ?: "保存失败，请稍后重试"
                onDone(false)
            } finally {
                saving = false
            }
        }
    }
}