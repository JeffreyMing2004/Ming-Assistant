package com.ming.mingassistant.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ming.mingassistant.data.Song
import com.ming.mingassistant.data.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SongsUiState(
    val songs: List<Song> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
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
}