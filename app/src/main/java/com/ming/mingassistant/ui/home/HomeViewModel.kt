package com.ming.mingassistant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
)

class HomeViewModel(private val repo: LiveRepository = LiveRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(loading = true))
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
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
                .onSuccess { status -> _uiState.value = HomeUiState(status = status) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(loading = false, refreshing = false, error = e.message)
                }
        }
    }
}