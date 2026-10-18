package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.util.LocalizationManager
import java.util.Calendar

object WirdCompanionScheduler {

    const val CHANNEL_ID = "wird_companion_channel"
    const val NOTIFICATION_ID_MORNING = 3001
    const val NOTIFICATION_ID_EVENING = 3002

    const val ACTION_OPEN_WIRD = "com.example.quran.ACTION_OPEN_WIRD"
    const val EXTRA_IS_EVENING = "extra_is_evening"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "أوراد الصباح والمساء (١٠ ص و ٥ م)"
            val descriptionText = "تذكير يومي مبارك بأذكار الصباح والمساء وسورة الكرسي بصوت الشيخ إسلام صبحي"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleDailyWirds(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // 1. Morning Wird at 10:00 AM
        scheduleAt(context, alarmManager, 10, 0, false, 8001)

        // 2. Evening Wird at 5:00 PM (17:00)
        scheduleAt(context, alarmManager, 17, 0, true, 8002)
    }

    private fun scheduleAt(
        context: Context,
        alarmManager: AlarmManager,
        hour: Int,
        minute: Int,
        isEvening: Boolean,
        requestCode: Int
    ) {
        val intent = Intent(context, WirdCompanionAlarmReceiver::class.java).apply {
            putExtra(EXTRA_IS_EVENING, isEvening)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pendingIntent
            )
        }
    }

    fun showNotification(context: Context, isEvening: Boolean, isTest: Boolean = false) {
        createNotificationChannel(context)

        val userLang = LocalizationManager.getSavedLanguage(context)
        val notifId = if (isEvening) NOTIFICATION_ID_EVENING else NOTIFICATION_ID_MORNING

        // Open Wird Screen Intent
        val openIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_WIRD
            putExtra(EXTRA_IS_EVENING, isEvening)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            if (isEvening) 8101 else 8102,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss Intent
        val dismissIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DISMISS
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            if (isEvening) 8201 else 8202,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isEvening) {
            "ورد المساء المبارك 🌙 (٥:٠٠ م)"
        } else {
            "ورد الصباح المبارك ☀️ (١٠:٠٠ ص)"
        }

        val body = if (isEvening) {
            "حان موعد أذكار المساء وسورة الكرسي تلاوة خاشعة بصوت الشيخ إسلام صبحي مع ما تيسر من القرآن"
        } else {
            "حان موعد أذكار الصباح وسورة الكرسي تلاوة خاشعة بصوت الشيخ إسلام صبحي مع ما تيسر من القرآن"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_media_play,
                "فتح الورد المبارك 📖",
                openPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                LocalizationManager.getActionDismiss(userLang),
                dismissPendingIntent
            )

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
