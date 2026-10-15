package com.example.prayer

/**
 * Supported prayers and astronomical moments in the daily cycle.
 */
enum class PrayerType(
    val id: String,
    val titleArabic: String,
    val hasAdhan: Boolean
) {
    FAJR("fajr", "الفجر", true),
    SUNRISE("sunrise", "الشروق", false),
    DHUHR("dhuhr", "الظهر", true),
    ASR("asr", "العصر", true),
    SUNSET("sunset", "الغروب", false),
    MAGHRIB("maghrib", "المغرب", true),
    ISHA("isha", "العشاء", true)
}

/**
 * Alert options for each prayer.
 */
enum class AdhanAlertMode(val titleArabic: String) {
    FULL_ADHAN("أذان كامل بصوت المؤذن"),
    TAKBEER_ONLY("تكبيرات وتنبيه هادئ"),
    NOTIFICATION_ONLY("إشعار صامت فقط"),
    MUTED("إيقاف التنبيه")
}

/**
 * Beautiful and authentic Adhan recordings from renowned mosques and muezzins.
 */
enum class AdhanMuezzin(
    val id: String,
    val nameArabic: String,
    val locationOrSheikh: String,
    val audioUrl: String,
    val isFajrSpecial: Boolean
) {
    MAKKAH(
        id = "makkah",
        nameArabic = "أذان الحرم المكي الشريف",
        locationOrSheikh = "مكة المكرمة (الشيخ علي أحمد ملا)",
        audioUrl = "https://archive.org/download/TvQuran.com__Athan/TvQuran.com__01.athan.mp3",
        isFajrSpecial = false
    ),
    MADINAH(
        id = "madinah",
        nameArabic = "أذان المسجد النبوي الشريف",
        locationOrSheikh = "المدينة المنورة",
        audioUrl = "https://archive.org/download/TvQuran.com__Athan/TvQuran.com__02.athan.mp3",
        isFajrSpecial = false
    ),
    ABDUL_BASIT(
        id = "abdul_basit",
        nameArabic = "أذان الشيخ عبد الباسط عبد الصمد",
        locationOrSheikh = "تسجيل تاريخي خاشع ونادر",
        audioUrl = "https://archive.org/download/TvQuran.com__Athan/TvQuran.com__03.athan.mp3",
        isFajrSpecial = false
    ),
    ALAFASY(
        id = "alafasy",
        nameArabic = "أذان الشيخ مشاري راشد العفاسي",
        locationOrSheikh = "صوت ندي وعذب",
        audioUrl = "https://archive.org/download/TvQuran.com__Athan/TvQuran.com__04.athan.mp3",
        isFajrSpecial = false
    ),
    AL_AQSA(
        id = "al_aqsa",
        nameArabic = "أذان المسجد الأقصى المبارك",
        locationOrSheikh = "القدس الشريف",
        audioUrl = "https://archive.org/download/TvQuran.com__Athan/TvQuran.com__05.athan.mp3",
        isFajrSpecial = false
    ),
    EGYPT(
        id = "egypt",
        nameArabic = "أذان إذاعة القرآن الكريم بمصر",
        locationOrSheikh = "جامع الأزهر الشريف والقاهرة",
        audioUrl = "https://archive.org/download/TvQuran.com__Athan/TvQuran.com__06.athan.mp3",
        isFajrSpecial = false
    ),
    FAJR_SPECIAL(
        id = "fajr_special",
        nameArabic = "أذان الفجر (الصلاة خير من النوم)",
        locationOrSheikh = "أذان الفجر المخصوص بتثويب الفجر",
        audioUrl = "https://archive.org/download/TvQuran.com__Athan/TvQuran.com__07.athan-fjer.mp3",
        isFajrSpecial = true
    ),
    TAKBEER(
        id = "takbeer",
        nameArabic = "تكبيرات الأذان والتنبيه السريع",
        locationOrSheikh = "تكبيرات لطيفة للتنبيه الهادئ",
        audioUrl = "https://archive.org/download/TvQuran.com__Athan/TvQuran.com__takbir.mp3",
        isFajrSpecial = false
    )
}

/**
 * Custom Muezzin audio loaded directly from user's phone storage.
 */
data class CustomMuezzin(
    val isEnabled: Boolean = false,
    val name: String = "أذان مخصص من الهاتف",
    val filePath: String = ""
)


/**
 * Standard Islamic calculation methods.
 */
enum class CalculationMethod(
    val id: String,
    val nameArabic: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaFixedMinutes: Int? = null
) {
    EGYPTIAN(
        id = "egyptian",
        nameArabic = "الهيئة المصرية العامة للمساحة",
        fajrAngle = 19.5,
        ishaAngle = 17.5,
        ishaFixedMinutes = null
    ),
    UMM_AL_QURA(
        id = "umm_al_qura",
        nameArabic = "أم القرى - مكة المكرمة",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaFixedMinutes = 90
    ),
    MWL(
        id = "mwl",
        nameArabic = "رابطة العالم الإسلامي",
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        ishaFixedMinutes = null
    ),
    KARACHI(
        id = "karachi",
        nameArabic = "جامعة العلوم الإسلامية بكراتشي",
        fajrAngle = 18.0,
        ishaAngle = 18.0,
        ishaFixedMinutes = null
    ),
    DUBAI(
        id = "dubai",
        nameArabic = "دبي والإمارات العربية المتحدة",
        fajrAngle = 18.2,
        ishaAngle = 18.2,
        ishaFixedMinutes = null
    )
}

/**
 * City preset with geographical coordinates and default method.
 */
data class CityPreset(
    val nameArabic: String,
    val countryArabic: String,
    val latitude: Double,
    val longitude: Double,
    val defaultMethod: CalculationMethod = CalculationMethod.EGYPTIAN
)

object PresetCities {
    val list: List<CityPreset> = listOf(
        CityPreset("القاهرة", "مصر", 30.0444, 31.2357, CalculationMethod.EGYPTIAN),
        CityPreset("الإسكندرية", "مصر", 31.2001, 29.9187, CalculationMethod.EGYPTIAN),
        CityPreset("الجيزة", "مصر", 30.0131, 31.2089, CalculationMethod.EGYPTIAN),
        CityPreset("المنصورة", "مصر", 31.0409, 31.3785, CalculationMethod.EGYPTIAN),
        CityPreset("طنطا", "مصر", 30.7865, 31.0004, CalculationMethod.EGYPTIAN),
        CityPreset("الزقازيق", "مصر", 30.5877, 31.5020, CalculationMethod.EGYPTIAN),
        CityPreset("أسيوط", "مصر", 27.1809, 31.1837, CalculationMethod.EGYPTIAN),
        CityPreset("سوهاج", "مصر", 26.5569, 31.6948, CalculationMethod.EGYPTIAN),
        CityPreset("قنا", "مصر", 26.1551, 32.7160, CalculationMethod.EGYPTIAN),
        CityPreset("الأقصر", "مصر", 25.6872, 32.6396, CalculationMethod.EGYPTIAN),
        CityPreset("أسوان", "مصر", 24.0889, 32.8998, CalculationMethod.EGYPTIAN),
        CityPreset("بورسعيد", "مصر", 31.2653, 32.3019, CalculationMethod.EGYPTIAN),
        CityPreset("السويس", "مصر", 29.9668, 32.5498, CalculationMethod.EGYPTIAN),
        CityPreset("الإسماعيلية", "مصر", 30.5965, 32.2715, CalculationMethod.EGYPTIAN),
        CityPreset("مكة المكرمة", "المملكة العربية السعودية", 21.3891, 39.8579, CalculationMethod.UMM_AL_QURA),
        CityPreset("المدينة المنورة", "المملكة العربية السعودية", 24.5247, 39.5692, CalculationMethod.UMM_AL_QURA),
        CityPreset("الرياض", "المملكة العربية السعودية", 24.7136, 46.6753, CalculationMethod.UMM_AL_QURA),
        CityPreset("جدة", "المملكة العربية السعودية", 21.5433, 39.1728, CalculationMethod.UMM_AL_QURA),
        CityPreset("الدمام", "المملكة العربية السعودية", 26.4207, 50.0888, CalculationMethod.UMM_AL_QURA),
        CityPreset("القدس الشريف", "فلسطين", 31.7683, 35.2137, CalculationMethod.MWL),
        CityPreset("دبي", "الإمارات العربية المتحدة", 25.2048, 55.2708, CalculationMethod.DUBAI),
        CityPreset("أبوظبي", "الإمارات العربية المتحدة", 24.4539, 54.3773, CalculationMethod.DUBAI),
        CityPreset("عَمّان", "الأردن", 31.9454, 35.9284, CalculationMethod.MWL),
        CityPreset("بيروت", "لبنان", 33.8938, 35.5018, CalculationMethod.MWL),
        CityPreset("دمشق", "سوريا", 33.5138, 36.2765, CalculationMethod.MWL),
        CityPreset("بغداد", "العراق", 33.3152, 44.3661, CalculationMethod.MWL),
        CityPreset("الكويت", "الكويت", 29.3759, 47.9774, CalculationMethod.UMM_AL_QURA),
        CityPreset("الدوحة", "قطر", 25.2854, 51.5310, CalculationMethod.UMM_AL_QURA),
        CityPreset("المنامة", "البحرين", 26.2285, 50.5860, CalculationMethod.UMM_AL_QURA),
        CityPreset("مسقط", "عُمان", 23.5880, 58.3829, CalculationMethod.MWL),
        CityPreset("إسطنبول", "تركيا", 41.0082, 28.9784, CalculationMethod.MWL)
    )

    val defaultCity: CityPreset = list.first()
}

/**
 * Display item for a specific prayer or astronomical moment.
 */
data class PrayerTimeItem(
    val type: PrayerType,
    val timeFormatted12h: String,
    val time24h: String,
    val timestampMillis: Long,
    val alertMode: AdhanAlertMode,
    val isNext: Boolean = false,
    val isCurrent: Boolean = false
)

/**
 * Complete prayer schedule with real-time countdown tracking.
 */
data class PrayerSchedule(
    val dateGregorianFormatted: String,
    val dateHijriFormatted: String,
    val cityName: String,
    val items: List<PrayerTimeItem>,
    val nextPrayer: PrayerTimeItem?,
    val remainingSeconds: Long,
    val countdownFormatted: String,
    val progressFraction: Float
)
