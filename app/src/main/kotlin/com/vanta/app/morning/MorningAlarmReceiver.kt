package com.vanta.app.morning

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.vanta.app.VantaApplication
import com.vanta.app.data.model.MorningPlaybackMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Fires once at the scheduled morning time (see [MorningScheduler]).
 *
 * Because AlarmManager alarms are one-shot, this is also responsible for
 * re-arming tomorrow's alarm immediately after starting playback, so the
 * schedule is self-renewing as long as [MorningConfig.enabled] stays true.
 */
class MorningAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as VantaApplication
        val container = app.container
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Main.immediate).launch {
            try {
                val config = container.morningRepository.getConfig()
                if (config.enabled && config.hasSongs) {
                    val songs = container.musicRepository.getSongsByIds(config.songIds)
                    // Preserve the user's configured order; getByIds does not
                    // guarantee it, so re-sort against songIds explicitly.
                    val ordered = config.songIds.mapNotNull { id -> songs.find { it.id == id } }
                    val playOrder = if (config.playbackMode == MorningPlaybackMode.SHUFFLE) {
                        ordered.shuffled()
                    } else {
                        ordered
                    }

                    if (playOrder.isNotEmpty()) {
                        withTimeoutOrNull(5000) { container.playbackManager.awaitConnected() }
                        container.playbackManager.playQueue(playOrder, 0)
                    }
                }

                // Re-arm tomorrow regardless, so the system stays in sync
                // with the persisted config without requiring the app to be
                // opened again.
                container.morningScheduler.sync(config)
            } catch (t: Throwable) {
                Log.e("MorningAlarmReceiver", "Failed to start morning playback", t)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
