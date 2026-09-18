package com.vanta.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached metadata for a song discovered in MediaStore (or produced by conversion).
 * The primary key mirrors the MediaStore _ID so re-scans upsert cleanly.
 */
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val mediaStoreUri: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val artworkUri: String?,
    val dateAddedMs: Long,
    val isConverted: Boolean = false
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAtMs: Long
)

/**
 * Join row between playlists and songs. `position` preserves manual ordering
 * within a playlist independent of insertion order.
 */
@Entity(tableName = "playlist_song_cross_ref", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val position: Int
)

/**
 * Singleton row (id is always 0) holding the morning music configuration.
 * Selected song order is stored as a comma-separated id list rather than a
 * separate table since it is small, always read/written as a whole, and
 * ordering matters for SEQUENTIAL playback.
 */
@Entity(tableName = "morning_config")
data class MorningConfigEntity(
    @PrimaryKey val id: Int = 0,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    val playbackMode: String,
    val songIdsCsv: String
)

/**
 * Cached metadata for a video discovered in MediaStore. Same upsert/prune
 * pattern as [SongEntity] so a re-scan stays cheap and stays in sync with
 * what's actually still on disk.
 */
@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: Long,
    val mediaStoreUri: String,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val dateAddedMs: Long
)
