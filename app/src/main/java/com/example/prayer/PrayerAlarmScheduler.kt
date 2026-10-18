package com.example.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object PrayerAlarmScheduler {

    fun rescheduleAll(context: Context) {
        val prefs = context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
        val isMasterAlertEnabled = prefs.getBoolean("adhan_master_enabled", true)
        if (!isMasterAlertEnabled) return

        val lat = prefs.getFloat("city_lat", PresetCities.defaultCity.latitude.toFloat()).toDouble()
        val lng = prefs.getFloat("city_lng", PresetCities.defaultCity.longitude.toFloat()).toDouble()
        val cityName = prefs.getString("city_name", PresetCities.defaultCity.nameArabic) ?: PresetCities.defaultCity.nameArabic
        val methodName = prefs.getString("calc_method", CalculationMethod.EGYPTIAN.name) ?: CalculationMethod.EGYPTIAN.name
        val method = try { CalculationMethod.valueOf(methodName) } catch (_: Exception) { CalculationMethod.EGYPTIAN }
        val timeZoneId = prefs.getString("city_tz", PresetCities.defaultCity.timeZoneId) ?: PresetCities.defaultCity.timeZoneId
        val tz = try { java.util.TimeZone.getTimeZone(timeZoneId) } catch (_: Exception) { java.util.TimeZone.getDefault() }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Compute for today and tomorrow with city timezone
        val now = System.currentTimeMillis()
        val todayCal = Calendar.getInstance(tz)
        val todayTimes = PrayerTimesCalculator.calculate(lat, lng, todayCal, method)

        val tomorrowCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowTimes = PrayerTimesCalculator.calculate(lat, lng, tomorrowCal, method)

        val prayersWithAdhan = listOf(
            PrayerType.FAJR,
            PrayerType.DHUHR,
            PrayerType.ASR,
            PrayerType.MAGHRIB,
            PrayerType.ISHA
        )

        for (prayer in prayersWithAdhan) {
            val alertModeStr = prefs.getString("alert_mode_${prayer.id}", AdhanAlertMode.FULL_ADHAN.name)
            val alertMode = try { AdhanAlertMode.valueOf(alertModeStr!!) } catch (_: Exception) { AdhanAlertMode.FULL_ADHAN }

            if (alertMode == AdhanAlertMode.MUTED) {
                cancelAlarm(context, prayer.id)
                continue
            }

            var targetTimeMillis = todayTimes[prayer]?.timestampMillis ?: 0L
            if (targetTimeMillis <= now) {
                targetTimeMillis = tomorrowTimes[prayer]?.timestampMillis ?: 0L
            }

            if (targetTimeMillis > now) {
                scheduleExact(context, alarmManager, prayer, cityName, alertMode, targetTimeMillis)
            }
        }
    }

    private fun scheduleExact(
        context: Context,
        alarmManager: AlarmManager,
        prayer: PrayerType,
        cityName: String,
        alertMode: AdhanAlertMode,
        triggerAtMillis: Long
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "com.example.quran.ACTION_PRAYER_ALARM"
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_ID, prayer.id)
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayer.titleArabic)
            putExtra(PrayerAlarmReceiver.EXTRA_CITY_NAME, cityName)
            putExtra(PrayerAlarmReceiver.EXTRA_ALERT_MODE, alertMode.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayer.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (_: Exception) {
            // Graceful fallback
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context, prayerId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "com.example.quran.ACTION_PRAYER_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayerId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
