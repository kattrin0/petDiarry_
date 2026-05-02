package com.example.petDiary.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.petDiary.R
import com.example.petDiary.ui.MainActivity

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val ok = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!ok) {
                Log.w(TAG, "POST_NOTIFICATIONS not granted — уведомление не показано")
                return
            }
        }

        val eventId = intent.getLongExtra(NotificationService.EXTRA_EVENT_ID, 0L)
        val title = intent.getStringExtra("event_title") ?: "Напоминание"
        val description = intent.getStringExtra("event_description") ?: ""

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NotificationService.notifyId(eventId),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_paw1)
            .setContentTitle("🐾 $title")
            .setContentText(if (description.isNotEmpty()) description else "Через 15 минут!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                if (description.isNotEmpty()) "$description\n\nСобытие через 15 минут!"
                else "Событие через 15 минут!"
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationService.notifyId(eventId), notification)
    }

    companion object {
        private const val TAG = "NotificationReceiver"
    }
}