package com.example.prayer

import android.icu.util.Calendar as IcuCalendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.tan

/**
 * Astronomical solar calculations for precise Islamic Prayer Times,
 * based on standard solar position algorithms (Jean Meeus astronomical models).
 */
object PrayerTimesCalculator {

    /**
     * Calculates all prayer times for a given latitude, longitude, and date.
     */
    fun calculate(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance(),
        method: CalculationMethod = CalculationMethod.EGYPTIAN,
        timeZoneOffsetHours: Double? = null
    ): Map<PrayerType, PrayerRawTime> {
        val tzOffset = timeZoneOffsetHours
            ?: (calendar.timeZone.getOffset(calendar.timeInMillis).toDouble() / 3600000.0)

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Date
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        val dJul = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * dJul)
        val q = fixAngle(280.459 + 0.98564736 * dJul)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2.0 * g)))
        val e = 23.439 - 0.00000036 * dJul

        var ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        ra = fixHour(ra)
        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))

        var eqT = (q / 15.0) - ra
        if (eqT > 12.0) eqT -= 24.0
        if (eqT < -12.0) eqT += 24.0

        // Solar Noon (Dhuhr)
        val dhuhrHours = 12.0 + tzOffset - (longitude / 15.0) - eqT

        fun sunHourAngle(angle: Double, isSunrise: Boolean): Double {
            val latR = Math.toRadians(latitude)
            val decR = Math.toRadians(declination)
            val cosH = (-sin(Math.toRadians(angle)) - sin(latR) * sin(decR)) / (cos(latR) * decR.let { cos(it) })
            if (cosH > 1.0 || cosH < -1.0) return dhuhrHours
            val h = Math.toDegrees(acos(cosH)) / 15.0
            return if (isSunrise) dhuhrHours - h else dhuhrHours + h
        }

        fun asrHourAngle(): Double {
            val latR = Math.toRadians(latitude)
            val decR = Math.toRadians(declination)
            val acot = atan(1.0 / (1.0 + tan(Math.abs(latR - decR))))
            val cosH = (sin(acot) - sin(latR) * sin(decR)) / (cos(latR) * cos(decR))
            val h = Math.toDegrees(acos(cosH.coerceIn(-1.0, 1.0))) / 15.0
            return dhuhrHours + h
        }

        val fajrHours = sunHourAngle(method.fajrAngle, isSunrise = true)
        val sunriseHours = sunHourAngle(0.833, isSunrise = true)
        val asrHours = asrHourAngle()
        val sunsetHours = sunHourAngle(0.833, isSunrise = false)
        val maghribHours = sunsetHours
        val ishaHours = if (method.ishaFixedMinutes != null) {
            maghribHours + (method.ishaFixedMinutes.toDouble() / 60.0)
        } else {
            sunHourAngle(method.ishaAngle, isSunrise = false)
        }

        return mapOf(
            PrayerType.FAJR to createRawTime(calendar, fajrHours),
            PrayerType.SUNRISE to createRawTime(calendar, sunriseHours),
            PrayerType.DHUHR to createRawTime(calendar, dhuhrHours),
            PrayerType.ASR to createRawTime(calendar, asrHours),
            PrayerType.SUNSET to createRawTime(calendar, sunsetHours),
            PrayerType.MAGHRIB to createRawTime(calendar, maghribHours),
            PrayerType.ISHA to createRawTime(calendar, ishaHours)
        )
    }

    private fun createRawTime(baseCalendar: Calendar, fractionalHours: Double): PrayerRawTime {
        val normalized = fixHour(fractionalHours)
        var h = normalized.toInt()
        var m = round((normalized - h) * 60.0).toInt()
        if (m >= 60) {
            h = (h + 1) % 24
            m = 0
        }

        val cal = baseCalendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, h)
        cal.set(Calendar.MINUTE, m)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val time24 = String.format(Locale.US, "%02d:%02d", h, m)

        // 12-hour format in Arabic
        val periodShort = if (h < 12) "ص" else "م"
        val periodLong = if (h < 12) "صباحاً" else "مساءً"
        val hour12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        val time12 = String.format(Locale.US, "%d:%02d %s", hour12, m, periodShort)
        val time12Long = String.format(Locale.US, "%d:%02d %s", hour12, m, periodLong)

        return PrayerRawTime(
            hour24 = h,
            minute = m,
            formatted12h = time12,
            formatted12hLong = time12Long,
            periodArabic = periodLong,
            formatted24h = time24,
            timestampMillis = cal.timeInMillis
        )
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0.0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour % 24.0
        if (h < 0.0) h += 24.0
        return h
    }

    /**
     * Formats current Gregorian date in Arabic (e.g. الثلاثاء، 13 أكتوبر 2026).
     */
    fun formatGregorianDateArabic(calendar: Calendar = Calendar.getInstance()): String {
        val sdf = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
        return sdf.format(calendar.time)
    }

    /**
     * Formats current Hijri date in Arabic.
     */
    fun formatHijriDateArabic(calendar: Calendar = Calendar.getInstance()): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uLocale = ULocale("@calendar=islamic-umalqura")
                val islamicCal = IcuCalendar.getInstance(uLocale) as IslamicCalendar
                islamicCal.time = calendar.time

                val day = islamicCal.get(IslamicCalendar.DAY_OF_MONTH)
                val month = islamicCal.get(IslamicCalendar.MONTH)
                val year = islamicCal.get(IslamicCalendar.YEAR)

                val monthsArabic = arrayOf(
                    "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
                    "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
                    "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
                )
                val monthName = if (month in monthsArabic.indices) monthsArabic[month] else ""
                "$day $monthName $year هـ"
            } else {
                fallbackHijriDate()
            }
        } catch (_: Exception) {
            fallbackHijriDate()
        }
    }

    private fun fallbackHijriDate(): String {
        return "الموافق بالتقويم الهجري"
    }
}

data class PrayerRawTime(
    val hour24: Int,
    val minute: Int,
    val formatted12h: String,
    val formatted12hLong: String = "",
    val periodArabic: String = "",
    val formatted24h: String,
    val timestampMillis: Long
)
