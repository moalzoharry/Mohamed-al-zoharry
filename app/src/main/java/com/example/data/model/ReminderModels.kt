package com.example.data.model

import java.util.Locale

/**
 * Type of reading goal / wird for the daily Quran reminder.
 */
enum class WirdGoalType(
    val titleArabic: String,
    val subtitleArabic: String,
    val iconEmoji: String,
    val defaultSurahNumber: Int? = null
) {
    ONE_PAGE(
        titleArabic = "صفحة واحدة يومياً",
        subtitleArabic = "قراءة وجه واحد من المصحف (أجر عظيم بخطوة ميسرة)",
        iconEmoji = "📄"
    ),
    HALF_HIZB(
        titleArabic = "نصف حزب يومياً",
        subtitleArabic = "قراءة نصف حزب (وجهان تقريباً)",
        iconEmoji = "📖"
    ),
    ONE_HIZB(
        titleArabic = "حزب كامل يومياً",
        subtitleArabic = "قراءة حزب كامل (٤ صفحات تقريباً)",
        iconEmoji = "📚"
    ),
    ONE_JUZ(
        titleArabic = "جزء كامل يومياً (ختمة شهرية)",
        subtitleArabic = "قراءة جزء يومياً لختم القرآن الكريم كاملاً كل شهر",
        iconEmoji = "🌟"
    ),
    SURAH_MULK(
        titleArabic = "سورة الملك (المنجية)",
        subtitleArabic = "تذكير بقراءة سورة الملك يومياً قبل النوم",
        iconEmoji = "👑",
        defaultSurahNumber = 67
    ),
    CONTINUE_LAST_READ(
        titleArabic = "متابعة من آخر موضع",
        subtitleArabic = "الاستمرار مباشرة من السورة والآية التي توقفت عندها",
        iconEmoji = "🔖"
    )
}

/**
 * Configuration and state of the daily Quran reading reminder alarm.
 */
data class DailyWirdReminder(
    val isEnabled: Boolean = false,
    val hour: Int = 20, // 8 PM default
    val minute: Int = 30, // 8:30 PM
    val wirdGoalType: WirdGoalType = WirdGoalType.ONE_PAGE,
    val customMessage: String = "حان موعد وردك القرآني اليومي.. نوّر قلبك ويومك بآيات من الذكر الحكيم 📖",
    val remindOnFridaySurahKahf: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true
) {
    /**
     * Formats time into human-friendly Arabic string (e.g. ٨:٣٠ مساءً).
     */
    fun formatTimeArabic(): String {
        val isPm = hour >= 12
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val period = if (isPm) "مساءً" else "صباحاً"
        val minStr = if (minute < 10) "0$minute" else "$minute"
        return String.format(Locale.getDefault(), "%d:%s %s", displayHour, minStr, period)
    }

    /**
     * Formats 24h digital time (e.g. 20:30).
     */
    fun format24h(): String {
        val hStr = if (hour < 10) "0$hour" else "$hour"
        val mStr = if (minute < 10) "0$minute" else "$minute"
        return "$hStr:$mStr"
    }
}
