package com.vanta.app.conversion

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.vanta.app.data.model.Song
import com.vanta.app.data.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Extracts the audio track from [video] into a standalone AAC file using
 * Media3's Transformer, reporting real progress rather than a fake timed
 * animation (spec section 8: show actual conversion progress and never
 * pretend success if the operation failed).
 *
 * Output is written to app-specific external storage
 * (`.../Music/VANTA_Converted/`), which needs no storage permission on any
 * supported API level and is immediately playable via a `file://` URI —
 * it doesn't need to round-trip through MediaStore to be usable inside
 * VANTA's own player.
 */
class VideoAudioConverter(private val context: Context) {

    fun convert(video: Video): Flow<ConversionState> = callbackFlow {
        trySend(ConversionState.Converting(0))

        val outputDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir,
            "VANTA_Converted"
        ).apply { mkdirs() }

        val safeName = video.title.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "video_${video.id}" }
        val outputFile = File(outputDir, "${safeName}_${video.id}.m4a")
        if (outputFile.exists()) outputFile.delete()

        val transformer = Transformer.Builder(context)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    val song = Song(
                        id = -video.id, // negative space: never collides with a positive MediaStore audio id
                        mediaStoreUri = Uri.fromFile(outputFile).toString(),
                        title = video.title,
                        artist = "Converted from video",
                        album = null,
                        durationMs = exportResult.durationMs.takeIf { it > 0 } ?: video.durationMs,
                        artworkUri = null,
                        dateAddedMs = System.currentTimeMillis(),
                        isConverted = true
                    )
                    trySend(ConversionState.Success(song))
                    close()
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    outputFile.delete()
                    trySend(ConversionState.Error(exportException.message ?: "Conversion failed"))
                    close()
                }
            })
            .build()

        val editedMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse(video.mediaStoreUri)))
            .setRemoveVideo(true)
            .build()

        // Transformer must be driven from a thread with a Looper (main thread).
        withContext(Dispatchers.Main) {
            transformer.start(editedMediaItem, outputFile.absolutePath)
        }

        val progressJob = launch(Dispatchers.Main) {
            val holder = ProgressHolder()
            while (isActive) {
                val state = transformer.getProgress(holder)
                if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                    trySend(ConversionState.Converting(holder.progress))
                }
                delay(250)
            }
        }

        awaitClose {
            progressJob.cancel()
            transformer.cancel()
        }
    }
}
