package com.ming.mingassistant.ui.gifts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ming.mingassistant.data.GiftRecord
import com.ming.mingassistant.data.GiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GiftsUiState(
    val gifts: List<GiftRecord> = emptyList(),
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val error: String? = null,
)

class GiftsViewModel(private val repo: GiftRepository = GiftRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(GiftsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            repo.list()
                .onSuccess { list -> _uiState.value = GiftsUiState(gifts = list) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(loading = false, error = e.message) }
        }
    }

    fun add(nickname: String, bilibiliUid: String, giftType: String, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(submitting = true, error = null)
            repo.create(nickname, bilibiliUid, giftType)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onDone(null)
                    load()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onDone(e.message ?: "登记失败")
                }
        }
    }

    fun delete(record: GiftRecord) {
        viewModelScope.launch {
            repo.delete(record.id).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            load()
        }
    }
}