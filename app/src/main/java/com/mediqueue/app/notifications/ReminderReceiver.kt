package com.mediqueue.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mediqueue.app.MainActivity
import com.mediqueue.app.R
import com.mediqueue.app.data.SessionManager

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Only for the account that scheduled it, and only while logged in
        val session = SessionManager(context)
        val owner = intent.getLongExtra(ReminderScheduler.EXTRA_OWNER, -1L)
        if (!session.isLoggedIn || owner != session.userId) return

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        // remember it was shown, so the next list refresh does not notify again
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_ID, -1L)
        val prefs = context.getSharedPreferences("mediqueue_reminders", Context.MODE_PRIVATE)
        val fired = prefs.getStringSet(ReminderScheduler.KEY_FIRED, emptySet()).orEmpty() + id.toString()
        prefs.edit().putStringSet(ReminderScheduler.KEY_FIRED, fired).apply()

        val doctor = intent.getStringExtra(ReminderScheduler.EXTRA_DOCTOR).orEmpty()
        val time = intent.getStringExtra(ReminderScheduler.EXTRA_TIME).orEmpty()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_channel),
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_text, doctor, time))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.reminder_text, doctor, time)))
            .setAutoCancel(true)
            .setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    private companion object {
        const val CHANNEL_ID = "appointment_reminders"
    }
}
