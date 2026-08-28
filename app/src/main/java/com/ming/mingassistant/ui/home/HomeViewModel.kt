package com.ming.mingassistant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ming.mingassistant.BuildConfig
import com.ming.mingassistant.data.LiveRepository
import com.ming.mingassistant.data.LiveStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val status: LiveStatus? = null,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: String? = null,
    // 首页公告
    val announcement: String = "",
    val announcementLoaded: Boolean = false,
    // 在线版本检测
    val latestVersion: String = "",
    val latestCode: Int = 0,
    val apkUrl: String = "",
    val updateNote: String = "",
    val hasUpdate: Boolean = false,
    val updateChecked: Boolean = false,
    val updateDismissed: Boolean = false,
)

class HomeViewModel(private val repo: LiveRepository = LiveRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(loading = true))
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
        loadHomeInfo()
    }

    /** 启动/进入首页时拉取公告并检测是否有新版本（公开接口，无需登录）。 */
    private fun loadHomeInfo() {
        viewModelScope.launch {
            val announcement = repo.fetchAnnouncement().getOrNull()?.text.orEmpty()
            val version = repo.fetchAppVersion().getOrNull()
            val hasUpdate = version != null && version.latestCode > BuildConfig.VERSION_CODE
            _uiState.value = _uiState.value.copy(
                announcement = announcement,
                announcementLoaded = true,
                latestVersion = version?.latestVersion ?: "",
                latestCode = version?.latestCode ?: 0,
                apkUrl = version?.apkUrl ?: "",
                updateNote = version?.updateNote ?: "",
                hasUpdate = hasUpdate,
                updateChecked = true,
            )
        }
    }

    /** 关闭首页的新版本提示条（本次会话内不再显示，下次启动仍会检测）。 */
    fun dismissUpdate() {
        _uiState.value = _uiState.value.copy(updateDismissed = true)
    }

    fun refresh() {
        viewModelScope.launch {
            val wasLoaded = _uiState.value.status != null
            _uiState.value = if (wasLoaded) {
                _uiState.value.copy(refreshing = true, error = null)
            } else {
                _uiState.value.copy(loading = true, error = null)
            }
            repo.fetchStatus()
                .onSuccess { status -> _uiState.value = _uiState.value.copy(status = status, loading = false, refreshing = false, error = null) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(loading = false, refreshing = false, error = e.message)
                }
        }
    }
}