package com.vanta.app.morning

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vanta.app.data.model.MorningConfig
import java.util.Calendar

/**
 * Owns the single OS-level alarm behind the morning music system.
 *
 * [MorningRepository] is the source of truth for *what* the user configured;
 * this class is only responsible for making sure an [AlarmManager] alarm
 * exists (or doesn't) that matches it. Call [sync] any time the config
 * changes and after boot (see [BootReceiver]).
 */
class MorningScheduler(private val context: Context) {

    private val alarmManager: AlarmManager?
        get() = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun sync(config: MorningConfig) {
        cancel()
        if (config.enabled && config.hasSongs) {
            schedule(config.hour, config.minute)
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        val am = alarmManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
    }

    private fun schedule(hour: Int, minute: Int) {
        val am = alarmManager ?: return
        val triggerAt = nextTriggerTimeMillis(hour, minute)
        val pendingIntent = alarmPendingIntent()

        val canBeExact = canScheduleExactAlarms()
        if (canBeExact) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } else {
            // Fall back to an inexact alarm rather than silently doing nothing;
            // the UI should also surface canScheduleExactAlarms() == false so
            // the user can grant the permission for precise timing.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun cancel() {
        alarmManager?.cancel(alarmPendingIntent())
    }

    private fun alarmPendingIntent(): PendingIntent {
        val intent = Intent(context, MorningAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun nextTriggerTimeMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    companion object {
        private const val ALARM_REQUEST_CODE = 4200
    }
}
