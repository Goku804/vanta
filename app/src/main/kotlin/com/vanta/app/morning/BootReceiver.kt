package com.vanta.app.morning

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vanta.app.VantaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AlarmManager alarms do not survive a reboot, so we re-derive and re-arm
 * the morning alarm from the persisted [com.vanta.app.data.model.MorningConfig]
 * as soon as the device finishes booting.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val container = (context.applicationContext as VantaApplication).container
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Main.immediate).launch {
            try {
                val config = container.morningRepository.getConfig()
                container.morningScheduler.sync(config)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
