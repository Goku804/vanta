package com.vanta.app.ui.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanta.app.data.model.PlaybackState
import com.vanta.app.data.model.Song
import com.vanta.app.data.repository.MusicRepository
import com.vanta.app.playback.PlaybackManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MusicLibraryUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val searchQuery: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModel(
    private val repository: MusicRepository,
    val playbackManager: PlaybackManager
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val isLoading = MutableStateFlow(true)

    val uiState: StateFlow<MusicLibraryUiState> = combine(
        query.flatMapLatest { q -> if (q.isBlank()) repository.observeSongs() else repository.searchSongs(q) },
        query,
        isLoading
    ) { songs, q, loading ->
        MusicLibraryUiState(
            songs = songs,
            isLoading = loading,
            isEmpty = songs.isEmpty() && !loading,
            searchQuery = q
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MusicLibraryUiState())

    val playbackState: StateFlow<PlaybackState> = playbackManager.state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            repository.refreshFromMediaStore()
            isLoading.value = false
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun playSong(song: Song) {
        val queue = uiState.value.songs
        playbackManager.playSongNow(song, queue)
    }

    fun togglePlayPause() = playbackManager.togglePlayPause()
    fun skipNext() = playbackManager.skipNext()
}
