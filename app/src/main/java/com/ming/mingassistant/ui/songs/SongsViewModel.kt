package com.ming.mingassistant.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ming.mingassistant.data.QqImportResult
import com.ming.mingassistant.data.QqPreview
import com.ming.mingassistant.data.Song
import com.ming.mingassistant.data.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SongsUiState(
    val songs: List<Song> = emptyList(),
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val error: String? = null,
    val qqBusy: Boolean = false,
    val qqError: String? = null,
    val qqPreview: QqPreview? = null,
)

class SongsViewModel(private val repo: SongRepository = SongRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            repo.list()
                .onSuccess { list -> _uiState.value = SongsUiState(songs = list) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(loading = false, error = e.message) }
        }
    }

    fun add(title: String, artist: String, note: String, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(submitting = true, error = null)
            repo.create(title, artist, note)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onDone(null)
                    load()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onDone(e.message ?: "添加失败")
                }
        }
    }

    fun delete(song: Song) {
        viewModelScope.launch {
            repo.delete(song.id).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            load()
        }
    }

    fun queryQq(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(qqBusy = true, qqError = null, qqPreview = null)
            repo.qqPreview(url)
                .onSuccess { p -> _uiState.value = _uiState.value.copy(qqBusy = false, qqPreview = p) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(qqBusy = false, qqError = e.message) }
        }
    }

    fun importQq(url: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(qqBusy = true, qqError = null, qqPreview = null)
            repo.qqImport(url)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(qqBusy = false)
                    onDone(true)
                    load()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(qqBusy = false, qqError = e.message)
                    onDone(false)
                }
        }
    }
}