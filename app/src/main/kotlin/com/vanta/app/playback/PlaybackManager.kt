package com.vanta.app.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.vanta.app.data.model.PlaybackState
import com.vanta.app.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * App-wide facade over the Media3 [MediaController] connected to
 * [PlaybackService]. ViewModels talk to this instead of touching the
 * controller directly, so every screen (music, player, and later the
 * floating widget) shares one transport and one [PlaybackState].
 */
class PlaybackManager(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var currentQueue: List<Song> = emptyList()
    private var positionTickerStarted = false

    fun connect() {
        if (controller != null) return
        val sessionToken = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val future = MediaController.Builder(appContext, sessionToken).buildAsync()
        future.addListener(
            {
                controller = future.get().also { attachListener(it) }
            },
            MoreExecutors.directExecutor()
        )
    }

    fun release() {
        controller?.release()
        controller = null
    }

    /**
     * Suspends until the [MediaController] is bound, connecting first if
     * needed. Used by callers that must issue a command right away (e.g.
     * [com.vanta.app.morning.MorningAlarmReceiver] starting playback from a
     * cold start) rather than relying on [connect] having already resolved.
     */
    suspend fun awaitConnected() {
        if (controller != null) return
        suspendCancellableCoroutine<Unit> { cont ->
            val sessionToken = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
            val future = MediaController.Builder(appContext, sessionToken).buildAsync()
            future.addListener(
                {
                    if (controller == null) {
                        controller = future.get().also { attachListener(it) }
                    }
                    if (cont.isActive) cont.resume(Unit)
                },
                MoreExecutors.directExecutor()
            )
        }
    }

    /** Starts playback of [queue] beginning at [startIndex]. */
    fun playQueue(queue: List<Song>, startIndex: Int) {
        val c = controller ?: return
        if (queue.isEmpty() || startIndex !in queue.indices) return
        currentQueue = queue
        val items = queue.map { it.toMediaItem() }
        c.setMediaItems(items, startIndex, 0L)
        c.prepare()
        c.play()
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun skipNext() = controller?.seekToNextMediaItem()
    fun skipPrevious() = controller?.seekToPreviousMediaItem()
    fun seekTo(positionMs: Long) = controller?.seekTo(positionMs)

    fun playSongNow(song: Song, queueContext: List<Song> = listOf(song)) {
        val index = queueContext.indexOf(song).takeIf { it >= 0 } ?: 0
        playQueue(queueContext, index)
    }

    private fun attachListener(c: MediaController) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = pushState()
            override fun onPlaybackStateChanged(playbackState: Int) = pushState()
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = pushState()
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _state.value = _state.value.copy(error = error.message)
            }
        }
        c.addListener(listener)
        pushState()
        startPositionTicker(c)
    }

    private fun startPositionTicker(c: MediaController) {
        if (positionTickerStarted) return
        positionTickerStarted = true
        scope.launch {
            while (isActive) {
                if (c.isPlaying) pushState()
                delay(500)
            }
        }
    }

    private fun pushState() {
        val c = controller ?: return
        val index = c.currentMediaItemIndex
        val current = currentQueue.getOrNull(index)
        _state.value = PlaybackState(
            currentSong = current,
            isPlaying = c.isPlaying,
            isBuffering = c.playbackState == Player.STATE_BUFFERING,
            positionMs = c.currentPosition.coerceAtLeast(0),
            durationMs = c.duration.coerceAtLeast(0),
            queue = currentQueue,
            queueIndex = index,
            error = null
        )
    }
}

private fun Song.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setUri(mediaStoreUri)
        .setMediaId(id.toString())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artworkUri?.let { android.net.Uri.parse(it) })
                .build()
        )
        .build()
