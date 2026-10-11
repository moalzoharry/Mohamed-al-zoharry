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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.model.DailyWirdReminder
import com.example.data.model.WirdGoalType
import java.util.Calendar

object DailyWirdScheduler {

    const val TAG = "DailyWirdScheduler"
    const val CHANNEL_ID = "daily_quran_wird_channel"
    const val NOTIFICATION_ID = 1001
    const val TEST_NOTIFICATION_ID = 1002
    const val ACTION_DAILY_WIRD = "com.example.quran.ACTION_DAILY_WIRD_ALARM"
    private const val REQUEST_CODE = 9001

    /**
     * Creates the notification channel for Android 8.0 (API 26) and above.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "الورد القرآني اليومي"
            val descriptionText = "تنبيهات يومية لتذكيرك بموعد قراءة الورد اليومي من القرآن الكريم"
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

    /**
     * Schedules the next daily alarm based on the user's hour and minute preferences.
     */
    fun schedule(context: Context, reminder: DailyWirdReminder) {
        if (!reminder.isEnabled) {
            cancel(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyWirdAlarmReceiver::class.java).apply {
            action = ACTION_DAILY_WIRD
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next trigger time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time today has already elapsed, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val triggerAtMillis = calendar.timeInMillis
        Log.d(TAG, "Scheduling next daily wird alarm for: ${calendar.time}")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission not granted, falling back to standard alarm", e)
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to schedule alarm", ex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule exact alarm", e)
        }
    }

    /**
     * Cancels any scheduled daily wird alarm.
     */
    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyWirdAlarmReceiver::class.java).apply {
            action = ACTION_DAILY_WIRD
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Daily wird alarm cancelled.")
        }
    }

    /**
     * Constructs and dispatches the daily reminder notification.
     */
    fun showNotification(context: Context, reminder: DailyWirdReminder, isTest: Boolean = false) {
        createNotificationChannel(context)

        val targetSurah = when (reminder.wirdGoalType) {
            WirdGoalType.SURAH_MULK -> 67
            else -> null
        }

        // Deep link/intent to open app directly to the Quran reader or home
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (targetSurah != null) {
                putExtra("TARGET_SURAH_NUMBER", targetSurah)
            }
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            if (isTest) 101 else 102,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check if Friday and Kahf reminder applies
        val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val titleText = if (isTest) {
            "🔔 اختبار منبه الورد القرآني"
        } else if (isFriday && reminder.remindOnFridaySurahKahf) {
            "🕌 الجمعة مباركة: سورة الكهف والورد اليومي"
        } else {
            "📖 موعد الورد القرآني اليومي"
        }

        val goalDescription = "الهدف المخصص: ${reminder.wirdGoalType.titleArabic}"
        val bodyText = if (isTest) {
            "تنبيه تجريبي ناجح! سيصلك الإشعار يومياً في تمام الساعة ${reminder.formatTimeArabic()}."
        } else {
            reminder.customMessage
        }

        val bigTextContent = buildString {
            append(bodyText)
            append("\n\n")
            append("🎯 $goalDescription")
            append("\n✨ {أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ}")
        }

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titleText)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigTextContent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_agenda,
                "ابدأ القراءة الآن ➔",
                contentPendingIntent
            )

        if (reminder.soundEnabled) {
            builder.setSound(defaultSound)
        }
        if (reminder.vibrateEnabled) {
            builder.setVibrate(longArrayOf(0, 300, 200, 300))
        }

        val notificationId = if (isTest) TEST_NOTIFICATION_ID else NOTIFICATION_ID
        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
            Log.d(TAG, "Notification displayed successfully (ID: $notificationId)")
        } catch (e: SecurityException) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted yet", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display notification", e)
        }
    }
}
