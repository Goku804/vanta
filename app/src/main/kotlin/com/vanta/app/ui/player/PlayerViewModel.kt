package com.vanta.app.ui.player

import androidx.lifecycle.ViewModel
import com.vanta.app.data.model.PlaybackState
import com.vanta.app.playback.PlaybackManager
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel(private val playbackManager: PlaybackManager) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackManager.state

    fun togglePlayPause() = playbackManager.togglePlayPause()
    fun skipNext() = playbackManager.skipNext()
    fun skipPrevious() = playbackManager.skipPrevious()
    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)
}
