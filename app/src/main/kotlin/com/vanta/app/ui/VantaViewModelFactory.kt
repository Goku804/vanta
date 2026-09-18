package com.vanta.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.vanta.app.di.AppContainer
import com.vanta.app.ui.morning.MorningViewModel
import com.vanta.app.ui.music.MusicViewModel
import com.vanta.app.ui.player.PlayerViewModel
import com.vanta.app.ui.playlists.PlaylistsViewModel

/**
 * Single factory for every screen ViewModel, backed by [AppContainer].
 * Keeps the manual-DI approach (see AppContainer's own doc comment)
 * consistent all the way up to the UI layer.
 */
class VantaViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            MusicViewModel::class.java ->
                MusicViewModel(container.musicRepository, container.playbackManager) as T

            PlayerViewModel::class.java ->
                PlayerViewModel(container.playbackManager) as T

            MorningViewModel::class.java ->
                MorningViewModel(container.morningRepository, container.musicRepository, container.morningScheduler) as T

            PlaylistsViewModel::class.java ->
                PlaylistsViewModel(container.musicRepository) as T

            com.vanta.app.ui.videos.VideoLibraryViewModel::class.java ->
                com.vanta.app.ui.videos.VideoLibraryViewModel(container.videoRepository) as T

            com.vanta.app.ui.video.VideoPlayerViewModel::class.java ->
                com.vanta.app.ui.video.VideoPlayerViewModel(
                    container.videoRepository,
                    container.musicRepository,
                    container.videoAudioConverter
                ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
