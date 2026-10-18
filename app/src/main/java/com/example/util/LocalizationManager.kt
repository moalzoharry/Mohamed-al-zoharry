package com.example.util

import android.content.Context
import com.example.prayer.PrayerType

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val localizedArabic: String,
    val flagEmoji: String,
    val isRtl: Boolean
) {
    ARABIC("ar", "العربية", "العربية", "🇸🇦", true),
    ENGLISH("en", "English", "الإنجليزية", "🇬🇧", false),
    FRENCH("fr", "Français", "الفرنسية", "🇫🇷", false),
    TURKISH("tr", "Türkçe", "التركية", "🇹🇷", false),
    GERMAN("de", "Deutsch", "الألمانية", "🇩🇪", false);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ARABIC
        }
    }
}

object LocalizationManager {

    private const val PREFS_NAME = "app_localization_prefs"
    private const val KEY_LANG = "selected_app_language"

    fun getSavedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANG, AppLanguage.ARABIC.code) ?: AppLanguage.ARABIC.code
        return AppLanguage.fromCode(code)
    }

    fun saveLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, language.code).apply()
    }

    // ================= NOTIFICATION ACTION STRINGS =================

    fun getActionDone(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تمّ ✓"
        AppLanguage.ENGLISH -> "Done ✓"
        AppLanguage.FRENCH -> "Terminé ✓"
        AppLanguage.TURKISH -> "Tamamlandı ✓"
        AppLanguage.GERMAN -> "Erledigt ✓"
    }

    fun getActionDismiss(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إغلاق التذكير ✕"
        AppLanguage.ENGLISH -> "Dismiss ✕"
        AppLanguage.FRENCH -> "Fermer ✕"
        AppLanguage.TURKISH -> "Kapat ✕"
        AppLanguage.GERMAN -> "Schließen ✕"
    }

    fun getActionSettings(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الإعدادات ⚙"
        AppLanguage.ENGLISH -> "Settings ⚙"
        AppLanguage.FRENCH -> "Paramètres ⚙"
        AppLanguage.TURKISH -> "Ayarlar ⚙"
        AppLanguage.GERMAN -> "Einstellungen ⚙"
    }

    // ================= PRAYER & REMINDER STRINGS =================

    fun getPrayerNotificationTitle(lang: AppLanguage, prayerName: String, cityName: String): String = when (lang) {
        AppLanguage.ARABIC -> "حان الآن موعد أذان $prayerName"
        AppLanguage.ENGLISH -> "Time for $prayerName Prayer ($cityName)"
        AppLanguage.FRENCH -> "Heure de la prière de $prayerName ($cityName)"
        AppLanguage.TURKISH -> "$prayerName Ezan Vakti ($cityName)"
        AppLanguage.GERMAN -> "Zeit für das $prayerName Gebet ($cityName)"
    }

    fun getPrayerNotificationBody(lang: AppLanguage, prayerName: String, cityName: String): String = when (lang) {
        AppLanguage.ARABIC -> "أقم صلاتك تنعم بحياتك • حسب توقيت $cityName"
        AppLanguage.ENGLISH -> "Establish your prayer, find your peace • According to $cityName time"
        AppLanguage.FRENCH -> "Accomplissez votre prière • Heure de $cityName"
        AppLanguage.TURKISH -> "Namazını kıl, huzur bul • $cityName vakti"
        AppLanguage.GERMAN -> "Verrichte dein Gebet und finde Frieden • Nach $cityName Zeit"
    }

    fun getDailyWirdTitle(lang: AppLanguage, isFriday: Boolean): String = when (lang) {
        AppLanguage.ARABIC -> if (isFriday) "🕌 الجمعة مباركة: سورة الكهف والورد اليومي" else "📖 موعد الورد القرآني اليومي"
        AppLanguage.ENGLISH -> if (isFriday) "🕌 Blessed Friday: Surah Al-Kahf & Daily Wird" else "📖 Daily Quran Wird Reminder"
        AppLanguage.FRENCH -> if (isFriday) "🕌 Vendredi béni: Sourate Al-Kahf & Wird quotidien" else "📖 Rappel du Wird coranique quotidien"
        AppLanguage.TURKISH -> if (isFriday) "🕌 Hayırlı Cumalar: Kehf Suresi ve Günlük Vird" else "📖 Günlük Kuran Virdi Hatırlatıcısı"
        AppLanguage.GERMAN -> if (isFriday) "🕌 Gesegneter Freitag: Sure Al-Kahf & Tägliches Wird" else "📖 Tägliche Koran-Erinnerung"
    }

    fun getDailyWirdBody(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "لا تهجر كتاب ربك، نور قلبك بآيات من القرآن الكريم اليوم."
        AppLanguage.ENGLISH -> "Illuminate your heart with the Holy Quran today. Do not abandon your Wird."
        AppLanguage.FRENCH -> "Illuminez votre cœur avec le Saint Coran aujourd'hui."
        AppLanguage.TURKISH -> "Bugün kalbini Kur'an-ı Kerim ile aydınlat. Virdini aksatma."
        AppLanguage.GERMAN -> "Erleuchte dein Herz heute mit dem Heiligen Koran. Vernachlässige dein Wird nicht."
    }

    fun getPrayerTypeName(type: PrayerType, lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> type.titleArabic
        AppLanguage.ENGLISH -> when (type) {
            PrayerType.FAJR -> "Fajr"
            PrayerType.SUNRISE -> "Sunrise"
            PrayerType.DHUHR -> "Dhuhr"
            PrayerType.ASR -> "Asr"
            PrayerType.SUNSET -> "Sunset"
            PrayerType.MAGHRIB -> "Maghrib"
            PrayerType.ISHA -> "Isha"
        }
        AppLanguage.FRENCH -> when (type) {
            PrayerType.FAJR -> "Fajr"
            PrayerType.SUNRISE -> "Lever du soleil"
            PrayerType.DHUHR -> "Dhuhr"
            PrayerType.ASR -> "Asr"
            PrayerType.SUNSET -> "Coucher du soleil"
            PrayerType.MAGHRIB -> "Maghrib"
            PrayerType.ISHA -> "Isha"
        }
        AppLanguage.TURKISH -> when (type) {
            PrayerType.FAJR -> "İmsak (Sabah)"
            PrayerType.SUNRISE -> "Güneş"
            PrayerType.DHUHR -> "Öğle"
            PrayerType.ASR -> "İkindi"
            PrayerType.SUNSET -> "Akşam Üstü"
            PrayerType.MAGHRIB -> "Akşam"
            PrayerType.ISHA -> "Yatsı"
        }
        AppLanguage.GERMAN -> when (type) {
            PrayerType.FAJR -> "Fadschr"
            PrayerType.SUNRISE -> "Sonnenaufgang"
            PrayerType.DHUHR -> "Dhuhr"
            PrayerType.ASR -> "Asr"
            PrayerType.SUNSET -> "Sonnenuntergang"
            PrayerType.MAGHRIB -> "Maghrib"
            PrayerType.ISHA -> "Ischa"
        }
    }
}
