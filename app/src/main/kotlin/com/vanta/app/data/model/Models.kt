package com.vanta.app.data.model

/**
 * A single audio track, sourced either from MediaStore (device library)
 * or from a completed video-to-audio conversion (Phase 6).
 */
data class Song(
    val id: Long,
    val mediaStoreUri: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val artworkUri: String?,
    val dateAddedMs: Long,
    val isConverted: Boolean = false
)

/** A single video file, sourced from MediaStore. */
data class Video(
    val id: Long,
    val mediaStoreUri: String,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val dateAddedMs: Long,
    val thumbnailUri: String?
)

/** A user-created collection of songs. */
data class Playlist(
    val id: Long,
    val name: String,
    val createdAtMs: Long,
    val songCount: Int = 0
)

/** How the morning session moves between selected songs. */
enum class MorningPlaybackMode {
    SEQUENTIAL,
    SHUFFLE
}

/** Persisted configuration for the morning music system. */
data class MorningConfig(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    val playbackMode: MorningPlaybackMode,
    val songIds: List<Long>
) {
    val hasSongs: Boolean get() = songIds.isNotEmpty()
}

/** Current transport state of the shared playback engine. */
data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val error: String? = null
)
