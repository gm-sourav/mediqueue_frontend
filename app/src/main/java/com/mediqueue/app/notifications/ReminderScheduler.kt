package com.mediqueue.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mediqueue.app.data.AppointmentDto
import com.mediqueue.app.data.SessionManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Schedules a phone notification [LEAD_MINUTES] before every upcoming (PENDING / CONFIRMED)
 * appointment. Uses an inexact alarm, so no special permission is needed (it can be a few
 * minutes late). Call [sync] every time the appointment list is loaded: it adds new reminders
 * and removes the ones whose appointment was cancelled or finished.
 */
object ReminderScheduler {

    const val LEAD_MINUTES = 60L
    const val EXTRA_DOCTOR = "doctor"
    const val EXTRA_TIME = "time"
    const val EXTRA_OWNER = "owner"
    const val EXTRA_ID = "id"
    const val KEY_FIRED = "fired"

    private const val PREFS = "mediqueue_reminders"
    private const val KEY_IDS = "ids"

    private fun pendingIntent(context: Context, id: Long, extras: Intent.() -> Unit = {}): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.mediqueue.app.REMINDER"
            extras()
        }
        return PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun sync(context: Context, appointments: List<AppointmentDto>) {
        val app = context.applicationContext
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val owner = SessionManager(app).userId
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now()

        val fired = prefs.getStringSet(KEY_FIRED, emptySet()).orEmpty()
        val wanted = HashSet<String>()
        val stillUpcoming = HashSet<String>()
        for (a in appointments) {
            if (a.status != "PENDING" && a.status != "CONFIRMED") continue
            val start = runCatching {
                LocalDateTime.of(LocalDate.parse(a.slotDate), LocalTime.parse(a.startTime))
            }.getOrNull() ?: continue
            if (!start.isAfter(now)) continue
            stillUpcoming.add(a.id.toString())
            if (a.id.toString() in fired) continue   // already notified once
            // If the 1-hour mark already passed but the visit is still ahead, remind in a few seconds
            val fireAt = start.minusMinutes(LEAD_MINUTES).let {
                if (it.isAfter(now)) it else now.plusSeconds(5)
            }

            val timeLabel = a.startTime.take(5)
            val pi = pendingIntent(app, a.id) {
                putExtra(EXTRA_DOCTOR, a.doctorName)
                putExtra(EXTRA_TIME, timeLabel)
                putExtra(EXTRA_OWNER, owner)
                putExtra(EXTRA_ID, a.id)
            }
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                fireAt.atZone(zone).toInstant().toEpochMilli(),
                pi
            )
            wanted.add(a.id.toString())
        }

        // Cancel reminders of appointments that are no longer upcoming
        val old = prefs.getStringSet(KEY_IDS, emptySet()).orEmpty()
        for (id in old - wanted) {
            val longId = id.toLongOrNull() ?: continue
            val pi = pendingIntent(app, longId)
            alarmManager.cancel(pi)
            pi.cancel()
        }
        prefs.edit()
            .putStringSet(KEY_IDS, wanted)
            .putStringSet(KEY_FIRED, fired.filter { it in stillUpcoming }.toSet())
            .apply()
    }

    fun cancelAll(context: Context) {
        sync(context, emptyList())
    }
}
