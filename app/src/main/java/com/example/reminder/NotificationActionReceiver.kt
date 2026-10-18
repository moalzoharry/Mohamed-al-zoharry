package com.example.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.MainActivity
import com.example.util.LocalizationManager

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        val lang = LocalizationManager.getSavedLanguage(context)

        when (action) {
            ACTION_DONE -> {
                // Dismiss the notification
                if (notificationId != -1) {
                    nm?.cancel(notificationId)
                }

                // Show localized congratulations/acknowledgement toast
                val toastMessage = when (lang) {
                    com.example.util.AppLanguage.ARABIC -> "تقبّل الله طاعتكم وبارك في وردكم وصلاتكم 🤲"
                    com.example.util.AppLanguage.ENGLISH -> "May Allah accept your worship and prayers 🤲"
                    com.example.util.AppLanguage.FRENCH -> "Qu'Allah accepte vos prières et votre dévotion 🤲"
                    com.example.util.AppLanguage.TURKISH -> "Allah ibadetinizi ve dualarınızı kabul etsin 🤲"
                    com.example.util.AppLanguage.GERMAN -> "Möge Allah deine Gebete und Taten annehmen 🤲"
                }
                Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
            }

            ACTION_DISMISS -> {
                // Simply dismiss the notification
                if (notificationId != -1) {
                    nm?.cancel(notificationId)
                }
            }

            ACTION_SETTINGS -> {
                // Dismiss notification and launch app to settings
                if (notificationId != -1) {
                    nm?.cancel(notificationId)
                }
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("OPEN_SETTINGS", true)
                }
                context.startActivity(launchIntent)
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.example.quran.ACTION_NOTIFICATION_DONE"
        const val ACTION_DISMISS = "com.example.quran.ACTION_NOTIFICATION_DISMISS"
        const val ACTION_SETTINGS = "com.example.quran.ACTION_NOTIFICATION_SETTINGS"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
