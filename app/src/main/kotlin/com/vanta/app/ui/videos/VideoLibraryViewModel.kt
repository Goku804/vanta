package com.vanta.app.ui.videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanta.app.data.model.Video
import com.vanta.app.data.repository.VideoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VideoLibraryUiState(
    val videos: List<Video> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val searchQuery: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class VideoLibraryViewModel(private val repository: VideoRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val isLoading = MutableStateFlow(true)

    val uiState: StateFlow<VideoLibraryUiState> = combine(
        query.flatMapLatest { q -> if (q.isBlank()) repository.observeVideos() else repository.searchVideos(q) },
        query,
        isLoading
    ) { videos, q, loading ->
        VideoLibraryUiState(
            videos = videos,
            isLoading = loading,
            isEmpty = videos.isEmpty() && !loading,
            searchQuery = q
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VideoLibraryUiState())

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
}
