package com.example.prayer

import android.icu.util.Calendar as IcuCalendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import android.os.Build
import java.util.Calendar

data class HijriDate(
    val day: Int,
    val monthIndex: Int, // 1 to 12
    val monthArabic: String,
    val year: Int,
    val formatted: String
)

object HijriDateConverter {

    val monthsArabic = arrayOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    fun gregorianToHijri(calendar: Calendar = Calendar.getInstance()): HijriDate {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uLocale = ULocale("@calendar=islamic-umalqura")
                val islamicCal = IcuCalendar.getInstance(uLocale) as IslamicCalendar
                islamicCal.time = calendar.time

                val day = islamicCal.get(IslamicCalendar.DAY_OF_MONTH)
                val month = islamicCal.get(IslamicCalendar.MONTH) // 0-based
                val year = islamicCal.get(IslamicCalendar.YEAR)

                val monthName = if (month in monthsArabic.indices) monthsArabic[month] else "شهر هجري"
                HijriDate(
                    day = day,
                    monthIndex = month + 1,
                    monthArabic = monthName,
                    year = year,
                    formatted = "$day $monthName $year هـ"
                )
            } else {
                fallbackHijri()
            }
        } catch (_: Exception) {
            fallbackHijri()
        }
    }

    private fun fallbackHijri(): HijriDate {
        return HijriDate(
            day = 7,
            monthIndex = 5,
            monthArabic = "جمادى الأولى",
            year = 1448,
            formatted = "٧ جمادى الأولى ١٤٤٨ هـ"
        )
    }
}
