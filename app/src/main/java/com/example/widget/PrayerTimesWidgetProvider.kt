package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.prayer.PrayerTimesRepository
import com.example.prayer.PrayerType

class PrayerTimesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.quran.ACTION_REFRESH_PRAYER_WIDGET"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, PrayerTimesWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName) ?: return
            for (widgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            try {
                val repository = PrayerTimesRepository(context)
                val schedule = repository.buildScheduleNow()

                val views = RemoteViews(context.packageName, R.layout.widget_prayer_times)

                // 1. City & Hijri Date
                views.setTextViewText(R.id.widget_text_city, schedule.cityName)
                views.setTextViewText(R.id.widget_text_hijri, schedule.dateHijriFormatted)

                // 2. Next Prayer info
                val nextPrayer = schedule.nextPrayer
                if (nextPrayer != null) {
                    views.setTextViewText(R.id.widget_text_next_prayer_name, "الصلاة القادمة: صلاة ${nextPrayer.type.titleArabic}")
                    val timeStr = if (nextPrayer.timeFormattedLong.isNotEmpty()) nextPrayer.timeFormattedLong else nextPrayer.timeFormatted12h
                    views.setTextViewText(R.id.widget_text_next_prayer_time, timeStr)
                    views.setTextViewText(R.id.widget_text_countdown, schedule.countdownFormatted)
                } else {
                    views.setTextViewText(R.id.widget_text_next_prayer_name, "مواقيت الصلاة")
                    views.setTextViewText(R.id.widget_text_next_prayer_time, "--:--")
                    views.setTextViewText(R.id.widget_text_countdown, "--:--")
                }

                // 3. Populate 6 prayer times
                val itemsMap = schedule.items.associateBy { it.type }
                itemsMap[PrayerType.FAJR]?.let { views.setTextViewText(R.id.widget_time_fajr, it.timeFormatted12h) }
                itemsMap[PrayerType.SUNRISE]?.let { views.setTextViewText(R.id.widget_time_sunrise, it.timeFormatted12h) }
                itemsMap[PrayerType.DHUHR]?.let { views.setTextViewText(R.id.widget_time_dhuhr, it.timeFormatted12h) }
                itemsMap[PrayerType.ASR]?.let { views.setTextViewText(R.id.widget_time_asr, it.timeFormatted12h) }
                itemsMap[PrayerType.MAGHRIB]?.let { views.setTextViewText(R.id.widget_time_maghrib, it.timeFormatted12h) }
                itemsMap[PrayerType.ISHA]?.let { views.setTextViewText(R.id.widget_time_isha, it.timeFormatted12h) }

                // 4. Click Root -> Open MainActivity
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

                // 5. Click Refresh Button -> Broadcast refresh action
                val refreshIntent = Intent(context, PrayerTimesWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH_WIDGET
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    1,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

                // Update Widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
