package com.ming.mingassistant.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ming.mingassistant.data.ApiClient
import com.ming.mingassistant.data.SessionStore
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val sessionStore: SessionStore,
) : ViewModel() {

    var deleting by mutableStateOf(false)
        private set
    var deleteError by mutableStateOf<String?>(null)
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
}