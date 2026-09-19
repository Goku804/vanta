package com.vanta.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.vanta.app.data.db.dao.MorningConfigDao
import com.vanta.app.data.db.dao.PlaylistDao
import com.vanta.app.data.db.dao.SongDao
import com.vanta.app.data.db.entity.PlaylistEntity
import com.vanta.app.data.db.entity.SongEntity
import com.vanta.app.data.model.Playlist
import com.vanta.app.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Single source of truth for the on-device music library.
 *
 * MediaStore is the ground truth for what audio files exist; Room caches
 * their metadata so the UI has a fast, observable local copy and so
 * converted (video->audio) tracks — which don't live in MediaStore's own
 * "Music" bucket in the same way — can sit alongside scanned ones.
 */
class MusicRepository(
    private val context: Context,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {

    fun observeSongs(): Flow<List<Song>> =
        songDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun searchSongs(query: String): Flow<List<Song>> =
        songDao.search(query).map { list -> list.map { it.toDomain() } }

    fun observePlaylists(): Flow<List<Playlist>> =
        playlistDao.observeAllWithCounts().map { list ->
            list.map { Playlist(it.id, it.name, it.createdAtMs, it.songCount) }
        }

    fun observePlaylistSongs(playlistId: Long): Flow<List<Song>> =
        playlistDao.observeSongsInPlaylist(playlistId).map { list -> list.map { it.toDomain() } }

    suspend fun getSongsByIds(ids: List<Long>): List<Song> = withContext(Dispatchers.IO) {
        songDao.getByIds(ids).map { it.toDomain() }
    }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.createPlaylist(PlaylistEntity(name = name, createdAtMs = System.currentTimeMillis()))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.addSongToPlaylist(playlistId, songId)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeCrossRef(playlistId, songId)
    }

    /** Records a song produced by the video-to-audio converter (Phase 6 hook). */
    suspend fun registerConvertedSong(song: Song) = withContext(Dispatchers.IO) {
        songDao.upsert(song.toEntity(isConverted = true))
    }

    /**
     * Re-scans MediaStore's audio table and upserts results into Room,
     * pruning entries for files that no longer exist. Safe to call
     * repeatedly (e.g. on app start and pull-to-refresh).
     */
    suspend fun refreshFromMediaStore() = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.IS_MUSIC
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val scanned = mutableListOf<SongEntity>()
        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(collection, id)
                val albumId = cursor.getLong(albumIdCol)
                val artworkUri = ContentUris.withAppendedId(ARTWORK_BASE_URI, albumId)

                scanned += SongEntity(
                    id = id,
                    mediaStoreUri = contentUri.toString(),
                    title = cursor.getString(titleCol) ?: "Unknown title",
                    artist = cursor.getString(artistCol),
                    album = cursor.getString(albumCol),
                    durationMs = cursor.getLong(durationCol),
                    artworkUri = artworkUri.toString(),
                    dateAddedMs = cursor.getLong(dateAddedCol) * 1000L,
                    isConverted = false
                )
            }
        }
        songDao.replaceLibraryScan(scanned)
    }

    companion object {
        private val ARTWORK_BASE_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    }
}

private fun SongEntity.toDomain() = Song(
    id = id,
    mediaStoreUri = mediaStoreUri,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    artworkUri = artworkUri,
    dateAddedMs = dateAddedMs,
    isConverted = isConverted
)

private fun Song.toEntity(isConverted: Boolean = this.isConverted) = SongEntity(
    id = id,
    mediaStoreUri = mediaStoreUri,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    artworkUri = artworkUri,
    dateAddedMs = dateAddedMs,
    isConverted = isConverted
)
