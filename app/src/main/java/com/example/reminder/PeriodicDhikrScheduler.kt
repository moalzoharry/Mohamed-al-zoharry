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
import com.example.data.model.DhikrPresets
import com.example.data.model.PeriodicDhikrReminder
import com.example.util.LocalizationManager
import kotlin.random.Random

object PeriodicDhikrScheduler {

    const val TAG = "PeriodicDhikrScheduler"
    const val CHANNEL_ID = "periodic_dhikr_channel"
    const val NOTIFICATION_ID = 2001
    const val TEST_NOTIFICATION_ID = 2002
    const val ACTION_PERIODIC_DHIKR = "com.example.quran.ACTION_PERIODIC_DHIKR_ALARM"
    private const val REQUEST_CODE = 9002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "التذكير الدوري بالأذكار"
            val descriptionText = "تنبيهات دورية منتظمة لترطيب اللسان بالذكر والصلاة على النبي ﷺ"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
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

    fun schedule(context: Context, reminder: PeriodicDhikrReminder) {
        if (!reminder.isEnabled) {
            cancel(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PeriodicDhikrAlarmReceiver::class.java).apply {
            action = ACTION_PERIODIC_DHIKR
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMs = (reminder.intervalMinutes.coerceAtLeast(1)) * 60 * 1000L
        val triggerAtMillis = System.currentTimeMillis() + intervalMs

        Log.d(TAG, "Scheduling periodic dhikr alarm in ${reminder.intervalMinutes} minutes (at $triggerAtMillis)")

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
                Log.e(TAG, "Failed to schedule periodic dhikr alarm", ex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule exact periodic dhikr alarm", e)
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PeriodicDhikrAlarmReceiver::class.java).apply {
            action = ACTION_PERIODIC_DHIKR
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
            Log.d(TAG, "Periodic dhikr alarm cancelled.")
        }
    }

    fun showNotification(context: Context, reminder: PeriodicDhikrReminder, isTest: Boolean = false) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val notificationId = if (isTest) TEST_NOTIFICATION_ID else NOTIFICATION_ID
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            if (isTest) 201 else 202,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val lang = LocalizationManager.getSavedLanguage(context)

        // Select the Dhikr text
        val dhikrText = when {
            isTest -> "🔔 تنبيه تجريبي: «${reminder.selectedDhikrText}»"
            reminder.isRandomPresetRotation -> {
                val list = DhikrPresets.defaultList
                list[Random.nextInt(list.size)]
            }
            reminder.selectedDhikrText.isNotBlank() -> reminder.selectedDhikrText
            else -> "«اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى سَيِّدِنَا مُحَمَّدٍ ﷺ»"
        }

        val titleText = when (lang) {
            com.example.util.AppLanguage.ARABIC -> "📿 ذكر وتسبيح • الفرقان بلس"
            com.example.util.AppLanguage.ENGLISH -> "📿 Dhikr & Remembrance • Al-Furqan Plus"
            com.example.util.AppLanguage.FRENCH -> "📿 Évocation & Dhikr • Al-Furqan Plus"
            com.example.util.AppLanguage.TURKISH -> "📿 Zikir ve Salavat • Al-Furqan Plus"
            com.example.util.AppLanguage.GERMAN -> "📿 Dhikr & Gedenken • Al-Furqan Plus"
        }

        val bigTextContent = buildString {
            append(dhikrText)
            append("\n\n")
            append("✨ رطّب لسانك بذكر الله والصلاة على النبي ﷺ")
            append("\n⏱ يتكرر التنبيه: ${reminder.formatIntervalArabic()}")
        }

        // Action 1: Done (تمّ الذكر)
        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DONE
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 10,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Dismiss (إغلاق التذكير)
        val dismissIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DISMISS
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 20,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 3: Settings (تعديل التنبيه)
        val settingsIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SETTINGS
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val settingsPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 30,
            settingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titleText)
            .setContentText(dhikrText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigTextContent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.checkbox_on_background,
                LocalizationManager.getActionDone(lang),
                donePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                LocalizationManager.getActionDismiss(lang),
                dismissPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_preferences,
                LocalizationManager.getActionSettings(lang),
                settingsPendingIntent
            )

        if (reminder.soundEnabled) {
            builder.setSound(defaultSound)
        }
        if (reminder.vibrateEnabled) {
            builder.setVibrate(longArrayOf(0, 250, 150, 250))
        }

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
            Log.d(TAG, "Periodic Dhikr notification displayed successfully (ID: $notificationId)")
        } catch (e: SecurityException) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted yet", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display periodic dhikr notification", e)
        }
    }
}
