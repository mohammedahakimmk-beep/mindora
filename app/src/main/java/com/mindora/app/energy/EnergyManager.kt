package com.mindora.app.energy

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mindora.app.data.models.EnergyState
import java.util.Calendar
import java.util.TimeZone

class EnergyManager {
    companion object {
        const val MAX_ENERGY = 25
        const val ENERGY_RESET_ACTION = "com.mindora.app.ENERGY_RESET_CHECK"
    }

    fun checkAndResetIfNeeded(state: EnergyState): EnergyState {
        val lastResetDay = utcDayOfYear(state.lastResetUtc)
        val currentDay = utcDayOfYear(System.currentTimeMillis())
        return if (lastResetDay != currentDay || state.lastResetUtc == 0L) {
            state.copy(
                current = MAX_ENERGY,
                max = MAX_ENERGY,
                lastResetUtc = System.currentTimeMillis()
            )
        } else {
            state
        }
    }

    fun millisUntilNextReset(): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val next = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return next.timeInMillis - utc.timeInMillis
    }

    fun formatCountdown(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun scheduleResetAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EnergyResetReceiver::class.java).apply {
            action = ENERGY_RESET_ACTION
        }
        val pending = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextMidnight.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pending
        )
    }

    private fun utcDayOfYear(millis: Long): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = millis
        return cal.get(Calendar.DAY_OF_YEAR) + cal.get(Calendar.YEAR) * 1000
    }
}

class EnergyResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as? com.mindora.app.MindoraApp ?: return
        app.resetEnergyIfNeeded()
    }
}
