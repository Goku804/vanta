package com.vanta.app.di

import android.content.Context
import com.vanta.app.conversion.VideoAudioConverter
import com.vanta.app.data.db.VantaDatabase
import com.vanta.app.data.repository.MorningRepository
import com.vanta.app.data.repository.MusicRepository
import com.vanta.app.data.repository.VideoRepository
import com.vanta.app.morning.MorningScheduler
import com.vanta.app.playback.PlaybackManager

/**
 * Hand-rolled dependency container. The app is small enough that a DI
 * framework (Hilt/Koin) would add build complexity without much payoff yet;
 * this can be swapped in later without touching call sites much since
 * everything is already constructor-injected from here.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: VantaDatabase by lazy { VantaDatabase.get(appContext) }

    val musicRepository: MusicRepository by lazy {
        MusicRepository(appContext, database.songDao(), database.playlistDao())
    }

    val videoRepository: VideoRepository by lazy {
        VideoRepository(appContext, database.videoDao())
    }

    val morningRepository: MorningRepository by lazy {
        MorningRepository(database.morningConfigDao())
    }

    val playbackManager: PlaybackManager by lazy { PlaybackManager(appContext) }

    val morningScheduler: MorningScheduler by lazy { MorningScheduler(appContext) }

    val videoAudioConverter: VideoAudioConverter by lazy { VideoAudioConverter(appContext) }
}
