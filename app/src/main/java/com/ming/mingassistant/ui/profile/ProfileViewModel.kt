package com.ming.mingassistant.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ming.mingassistant.data.Session
import com.ming.mingassistant.data.SessionStore
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val sessionStore: SessionStore,
) : ViewModel() {

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            sessionStore.clear()
            onDone()
        }
    }

    fun Session.uidLabel(): String = bilibiliUid.ifBlank { "未填写" }
}