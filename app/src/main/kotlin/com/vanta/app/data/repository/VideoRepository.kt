package com.vanta.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.vanta.app.data.db.dao.VideoDao
import com.vanta.app.data.db.entity.VideoEntity
import com.vanta.app.data.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Mirrors [MusicRepository]'s scan/cache pattern for on-device video files:
 * MediaStore is ground truth, Room is the fast observable local copy the UI
 * reads from.
 */
class VideoRepository(
    private val context: Context,
    private val videoDao: VideoDao
) {

    fun observeVideos(): Flow<List<Video>> =
        videoDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun searchVideos(query: String): Flow<List<Video>> =
        videoDao.search(query).map { list -> list.map { it.toDomain() } }

    suspend fun getVideoById(id: Long): Video? = withContext(Dispatchers.IO) {
        videoDao.getById(id)?.toDomain()
    }

    suspend fun refreshFromMediaStore() = withContext(Dispatchers.IO) {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val scanned = mutableListOf<VideoEntity>()
        context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(collection, id)
                scanned += VideoEntity(
                    id = id,
                    mediaStoreUri = contentUri.toString(),
                    title = cursor.getString(titleCol) ?: "Untitled video",
                    durationMs = cursor.getLong(durationCol),
                    sizeBytes = cursor.getLong(sizeCol),
                    width = cursor.getInt(widthCol),
                    height = cursor.getInt(heightCol),
                    dateAddedMs = cursor.getLong(dateAddedCol) * 1000L
                )
            }
        }
        videoDao.replaceLibraryScan(scanned)
    }
}

private fun VideoEntity.toDomain() = Video(
    id = id,
    mediaStoreUri = mediaStoreUri,
    title = title,
    durationMs = durationMs,
    sizeBytes = sizeBytes,
    width = width,
    height = height,
    dateAddedMs = dateAddedMs,
    // Coil's VideoFrameDecoder (registered app-wide in VantaApplication) decodes
    // the first frame directly from the content URI, so no separate thumbnail
    // file/URI needs to be generated or stored.
    thumbnailUri = mediaStoreUri
)
