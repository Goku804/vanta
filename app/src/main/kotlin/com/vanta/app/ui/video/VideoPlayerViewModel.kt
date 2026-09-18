package com.vanta.app.ui.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanta.app.conversion.ConversionState
import com.vanta.app.conversion.VideoAudioConverter
import com.vanta.app.data.model.Video
import com.vanta.app.data.repository.MusicRepository
import com.vanta.app.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoPlayerViewModel(
    private val videoRepository: VideoRepository,
    private val musicRepository: MusicRepository,
    private val converter: VideoAudioConverter
) : ViewModel() {

    private val _video = MutableStateFlow<Video?>(null)
    val video: StateFlow<Video?> = _video.asStateFlow()

    private val _conversionState = MutableStateFlow<ConversionState>(ConversionState.Idle)
    val conversionState: StateFlow<ConversionState> = _conversionState.asStateFlow()

    private var loadedId: Long? = null

    fun loadVideo(id: Long) {
        if (loadedId == id) return
        loadedId = id
        viewModelScope.launch {
            _video.value = videoRepository.getVideoById(id)
        }
    }

    fun convertToAudio() {
        val current = _video.value ?: return
        if (_conversionState.value is ConversionState.Converting) return

        viewModelScope.launch {
            converter.convert(current).collect { state ->
                _conversionState.value = state
                if (state is ConversionState.Success) {
                    musicRepository.registerConvertedSong(state.song)
                }
            }
        }
    }

    fun dismissConversionResult() {
        _conversionState.value = ConversionState.Idle
    }
}
