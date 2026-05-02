package com.example.petDiary.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.petDiary.data.models.Event
import com.example.petDiary.ui.MainActivity
import java.util.Calendar

class NotificationService(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "pet_events_channel"
        const val EXTRA_EVENT_ID = "event_id"

        private const val TAG = "NotificationSvc"
        private const val REMINDER_BEFORE_MS = 15 * 60 * 1000L
        private const val FALLBACK_BEFORE_MS = 60 * 1000L
        private const val SOON_TOLERANCE_MS = 3_000L

        internal fun alarmRequestCode(eventId: Long): Int = steadyHash(eventId)

        @JvmStatic
        fun notifyId(eventId: Long): Int = steadyHash(eventId)

        private fun steadyHash(eventId: Long): Int =
            ((eventId ushr 32) xor eventId).toInt()
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "События питомца"
            val descriptionText = "Уведомления о запланированных событиях"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun syncNotificationsForEvents(events: List<Event>) {
        events.forEach { cancelNotification(it) }
        events
            .filter { !it.completed && it.id != null }
            .forEach { scheduleNotification(it) }
    }

    fun scheduleNotification(event: Event) {
        if (event.completed) return
        val eventId = event.id ?: return

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
                putExtra("event_title", event.title)
                putExtra("event_description", event.description ?: "")
            }

            val requestCode = alarmRequestCode(eventId)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = event.dateMillis
                set(Calendar.HOUR_OF_DAY, event.timeHour)
                set(Calendar.MINUTE, event.timeMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val eventStart = calendar.timeInMillis
            val now = System.currentTimeMillis()

            if (eventStart <= now) {
                Log.d(TAG, "skip schedule: событие уже в прошлом id=$eventId")
                return
            }

            var triggerAt = eventStart - REMINDER_BEFORE_MS
            if (triggerAt <= now) {
                triggerAt = (eventStart - FALLBACK_BEFORE_MS).coerceAtLeast(now + SOON_TOLERANCE_MS)
            }
            if (triggerAt >= eventStart) {
                triggerAt = now + SOON_TOLERANCE_MS
            }

            val showIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val showPi = PendingIntent.getActivity(
                context,
                alarmRequestCode(eventId) xor 0x53544152,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAt, showPi),
                pendingIntent
            )

            Log.d(TAG, "scheduled id=$eventId at $triggerAt (event at $eventStart)")
        } catch (e: Exception) {
            Log.e(TAG, "scheduleNotification failed id=${event.id}", e)
            e.printStackTrace()
        }
    }

    fun cancelNotification(event: Event) {
        val eventId = event.id ?: return
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmRequestCode(eventId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "cancelNotification failed id=$eventId", e)
            e.printStackTrace()
        }
    }
}