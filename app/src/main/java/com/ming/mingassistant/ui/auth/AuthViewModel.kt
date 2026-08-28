package com.ming.mingassistant.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ming.mingassistant.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isRegister: Boolean = false,
    val bilibiliUid: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isRegister = !_uiState.value.isRegister,
            error = null,
        )
    }

    fun onBilibiliUid(v: String) = _uiState.update { it.copy(bilibiliUid = v) }
    fun onUsername(v: String) = _uiState.update { it.copy(username = v) }
    fun onPassword(v: String) = _uiState.update { it.copy(password = v) }
    fun onConfirmPassword(v: String) = _uiState.update { it.copy(confirmPassword = v) }

    fun submit(onSuccess: () -> Unit) {
        val s = _uiState.value
        val localError = when {
            s.isRegister && s.bilibiliUid.isNotBlank() && !s.bilibiliUid.all { it.isDigit() } -> "B站UID必须为数字"
            s.username.isBlank() -> "请输入用户名"
            s.password.length < 6 -> "密码至少6位"
            s.isRegister && s.password != s.confirmPassword -> "两次输入的密码不一致"
            s.isRegister && s.confirmPassword.isBlank() -> "请再次输入密码"
            else -> null
        }
        if (localError != null) {
            _uiState.value = s.copy(error = localError)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val result = if (s.isRegister) {
                repo.register(s.bilibiliUid, s.username, s.password, s.confirmPassword)
            } else {
                repo.login(s.username, s.password)
            }
            _uiState.value = _uiState.value.copy(loading = false)
            result
                .onSuccess { onSuccess() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "操作失败") }
        }
    }

    private fun MutableStateFlow<AuthUiState>.update(block: (AuthUiState) -> AuthUiState) {
        value = block(value)
    }
}