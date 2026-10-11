package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.repository.QuranRepository

class DailyWirdAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("DailyWirdAlarmReceiver", "Received broadcast action: $action")

        val repository = QuranRepository(context)
        val reminder = repository.loadDailyWirdReminder()

        when (action) {
            DailyWirdScheduler.ACTION_DAILY_WIRD -> {
                if (reminder.isEnabled) {
                    // Show the notification to remind user to read their daily wird
                    DailyWirdScheduler.showNotification(context, reminder, isTest = false)

                    // Automatically reschedule for tomorrow at the same configured time
                    DailyWirdScheduler.schedule(context, reminder)
                }
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                // Device rebooted: restore alarm schedule if enabled
                if (reminder.isEnabled) {
                    Log.d("DailyWirdAlarmReceiver", "Restoring daily wird alarm after reboot...")
                    DailyWirdScheduler.schedule(context, reminder)
                }
            }
        }
    }
}
