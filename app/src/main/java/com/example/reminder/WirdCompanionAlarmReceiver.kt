package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WirdCompanionAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            WirdCompanionScheduler.scheduleDailyWirds(context)
            return
        }

        val isEvening = intent.getBooleanExtra(WirdCompanionScheduler.EXTRA_IS_EVENING, false)
        WirdCompanionScheduler.showNotification(context, isEvening = isEvening)

        // Reschedule for next day
        WirdCompanionScheduler.scheduleDailyWirds(context)
    }
}
