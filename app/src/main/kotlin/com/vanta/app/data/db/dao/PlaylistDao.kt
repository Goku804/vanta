package com.vanta.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.vanta.app.data.db.entity.PlaylistEntity
import com.vanta.app.data.db.entity.PlaylistSongCrossRef
import com.vanta.app.data.db.entity.SongEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val createdAtMs: Long,
    val songCount: Int
)

@Dao
interface PlaylistDao {

    @Query(
        """
        SELECT p.id AS id, p.name AS name, p.createdAtMs AS createdAtMs,
               COUNT(x.songId) AS songCount
        FROM playlists p
        LEFT JOIN playlist_song_cross_ref x ON x.playlistId = p.id
        GROUP BY p.id
        ORDER BY p.createdAtMs DESC
        """
    )
    fun observeAllWithCounts(): Flow<List<PlaylistWithCount>>

    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref x ON x.songId = s.id
        WHERE x.playlistId = :playlistId
        ORDER BY x.position ASC
        """
    )
    fun observeSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Insert
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun nextPosition(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCrossRef(ref: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeCrossRef(playlistId: Long, songId: Long)

    @Transaction
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val position = nextPosition(playlistId)
        addCrossRef(PlaylistSongCrossRef(playlistId, songId, position))
    }
}
