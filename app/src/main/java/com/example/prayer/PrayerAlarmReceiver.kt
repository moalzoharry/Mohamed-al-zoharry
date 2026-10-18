package com.example.prayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerId = intent.getStringExtra(EXTRA_PRAYER_ID) ?: return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: "مدينتك"
        val alertModeStr = intent.getStringExtra(EXTRA_ALERT_MODE) ?: AdhanAlertMode.FULL_ADHAN.name
        val alertMode = try { AdhanAlertMode.valueOf(alertModeStr) } catch (_: Exception) { AdhanAlertMode.FULL_ADHAN }

        if (alertMode == AdhanAlertMode.MUTED) return

        showPrayerNotification(context, prayerId, prayerName, cityName)

        if (alertMode == AdhanAlertMode.FULL_ADHAN || alertMode == AdhanAlertMode.TAKBEER_ONLY) {
            playAdhanAudio(context, prayerName, cityName)
        }

        // Reschedule alarms for upcoming prayers
        PrayerAlarmScheduler.rescheduleAll(context)
    }

    private fun showPrayerNotification(
        context: Context,
        prayerId: String,
        prayerName: String,
        cityName: String
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "prayer_adhan_channel_v2"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "مواقيت الصلاة والأذان",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات مواقيت الصلاة وصوت الأذان المبارك"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val lang = com.example.util.LocalizationManager.getSavedLanguage(context)
        val notifId = prayerId.hashCode()

        // 1. Done Action (تمت الصلاة)
        val doneIntent = Intent(context, com.example.reminder.NotificationActionReceiver::class.java).apply {
            action = com.example.reminder.NotificationActionReceiver.ACTION_DONE
            putExtra(com.example.reminder.NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 1,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Dismiss Action (إغلاق التنبيه)
        val dismissIntent = Intent(context, com.example.reminder.NotificationActionReceiver::class.java).apply {
            action = com.example.reminder.NotificationActionReceiver.ACTION_DISMISS
            putExtra(com.example.reminder.NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 2,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Settings Action (الإعدادات)
        val settingsIntent = Intent(context, com.example.reminder.NotificationActionReceiver::class.java).apply {
            action = com.example.reminder.NotificationActionReceiver.ACTION_SETTINGS
            putExtra(com.example.reminder.NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val settingsPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 3,
            settingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(com.example.util.LocalizationManager.getPrayerNotificationTitle(lang, prayerName, cityName))
            .setContentText(com.example.util.LocalizationManager.getPrayerNotificationBody(lang, prayerName, cityName))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "${com.example.util.LocalizationManager.getPrayerNotificationTitle(lang, prayerName, cityName)}\n" +
                    "${com.example.util.LocalizationManager.getPrayerNotificationBody(lang, prayerName, cityName)}\n\n" +
                    "«اللهم رب هذه الدعوة التامة والصلاة القائمة، آت سيدنا محمداً الوسيلة والفضيلة وابعثه مقاماً محموداً الذي وعدته»"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                com.example.util.LocalizationManager.getActionDone(lang),
                donePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                com.example.util.LocalizationManager.getActionDismiss(lang),
                dismissPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_preferences,
                com.example.util.LocalizationManager.getActionSettings(lang),
                settingsPendingIntent
            )
            .build()

        nm.notify(notifId, notification)
    }

    private fun playAdhanAudio(context: Context, prayerName: String, cityName: String) {
        try {
            val prefs = context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
            val isCustom = prefs.getBoolean("custom_muezzin_active", false)
            val customPath = prefs.getString("custom_muezzin_path", null)
            val customName = prefs.getString("custom_muezzin_name", "مؤذن مخصص من الهاتف") ?: "مؤذن مخصص"

            val muezzinId = prefs.getString("selected_muezzin", AdhanMuezzin.MAKKAH.name) ?: AdhanMuezzin.MAKKAH.name
            val muezzin = try { AdhanMuezzin.valueOf(muezzinId) } catch (_: Exception) { AdhanMuezzin.MAKKAH }

            val customFile = if (isCustom && !customPath.isNullOrEmpty()) customPath else null
            val audioUrl = if (customFile != null) null else muezzin.audioUrl
            val muezzinTitle = if (customFile != null) customName else muezzin.nameArabic

            AdhanPlaybackService.start(
                context = context,
                prayerName = prayerName,
                cityName = cityName,
                muezzinName = muezzinTitle,
                audioUrl = audioUrl,
                customAudioPath = customFile
            )
        } catch (_: Exception) {}
    }

    companion object {
        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_ALERT_MODE = "extra_alert_mode"
    }
}
