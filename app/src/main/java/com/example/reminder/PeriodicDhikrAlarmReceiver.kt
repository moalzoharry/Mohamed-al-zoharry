package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.repository.QuranRepository

class PeriodicDhikrAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("PeriodicDhikrAlarmReceiver", "Received broadcast action: $action")

        val repository = QuranRepository(context)
        val reminder = repository.loadPeriodicDhikrReminder()

        when (action) {
            PeriodicDhikrScheduler.ACTION_PERIODIC_DHIKR -> {
                if (reminder.isEnabled) {
                    // Show the notification with the custom / rotating dhikr
                    PeriodicDhikrScheduler.showNotification(context, reminder, isTest = false)

                    // Re-schedule for the next interval (e.g. 5 minutes from now)
                    PeriodicDhikrScheduler.schedule(context, reminder)
                }
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                if (reminder.isEnabled) {
                    Log.d("PeriodicDhikrAlarmReceiver", "Restoring periodic dhikr alarm after reboot...")
                    PeriodicDhikrScheduler.schedule(context, reminder)
                }
            }
        }
    }
}
