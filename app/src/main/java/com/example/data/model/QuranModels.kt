package com.example.data.model

enum class RevelationType(val titleArabic: String) {
    MAKKI("مكية"),
    MADANI("مدنية")
}

data class SurahInfo(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val englishTranslation: String,
    val revelationType: RevelationType,
    val ayahCount: Int,
    val startJuz: Int,
    val startPage: Int
)

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textWithTashkeel: String,
    val textClean: String,
    val tafsirJalalayn: String,
    val tafsirMuyassar: String,
    val juzNumber: Int = 1,
    val pageNumber: Int = 1
)

enum class TafsirSource(val title: String, val author: String, val description: String) {
    JALALAYN(
        title = "تفسير الجلالين",
        author = "جلال الدين المحلي وجلال الدين السيوطي",
        description = "تفسير كلاسيكي موجز ومشهور، يركز على بيان المعاني المفردة والوجوه الإعرابية والقراءات بإيجاز بليغ."
    ),
    MUYASSAR(
        title = "التفسير الميسر",
        author = "نخبة من علماء التفسير (مجمع الملك فهد)",
        description = "تفسير معاصر صاغه علماء مجمع الملك فهد بلغة سهلة واضحة ومباشرة لتيسير فهم الآيات لعامة المسلمين."
    )
}

enum class ReadingTheme(
    val titleArabic: String,
    val backgroundColorHex: Long,
    val surfaceColorHex: Long,
    val textColorHex: Long,
    val secondaryTextHex: Long,
    val accentColorHex: Long,
    val borderHex: Long,
    val isDark: Boolean
) {
    PARCHMENT(
        titleArabic = "ورق مصحف كريمي",
        backgroundColorHex = 0xFFFAF6EC,
        surfaceColorHex = 0xFFF3ECDB,
        textColorHex = 0xFF2A2118,
        secondaryTextHex = 0xFF6D5D4B,
        accentColorHex = 0xFF1B6A45,
        borderHex = 0xFFE2D6C0,
        isDark = false
    ),
    WHITE(
        titleArabic = "أبيض ناصع",
        backgroundColorHex = 0xFFFFFFFF,
        surfaceColorHex = 0xFFF6F7F9,
        textColorHex = 0xFF191C1E,
        secondaryTextHex = 0xFF5F656B,
        accentColorHex = 0xFF117A4D,
        borderHex = 0xFFE0E3E7,
        isDark = false
    ),
    SEPIA(
        titleArabic = "عتيق دافئ",
        backgroundColorHex = 0xFFF1E7D0,
        surfaceColorHex = 0xFFE5D7BC,
        textColorHex = 0xFF352613,
        secondaryTextHex = 0xFF6E5639,
        accentColorHex = 0xFF8A531B,
        borderHex = 0xFFD4C19F,
        isDark = false
    ),
    NIGHT(
        titleArabic = "ليلي داكن (OLED)",
        backgroundColorHex = 0xFF121417,
        surfaceColorHex = 0xFF1B1F24,
        textColorHex = 0xFFE4E7EB,
        secondaryTextHex = 0xFF9BA3AF,
        accentColorHex = 0xFF4ADE80,
        borderHex = 0xFF2D333B,
        isDark = true
    ),
    EMERALD(
        titleArabic = "أخضر الروضة",
        backgroundColorHex = 0xFF0D231A,
        surfaceColorHex = 0xFF143528,
        textColorHex = 0xFFE8F5E9,
        secondaryTextHex = 0xFFA5D6A7,
        accentColorHex = 0xFFF6C344,
        borderHex = 0xFF23533F,
        isDark = true
    )
}

enum class ReadingMode(val titleArabic: String) {
    AYAH_BY_AYAH("آية بآية مع التفسير والخيارات"),
    CONTINUOUS("قراءة متصلة كصفحات المصحف")
}

enum class QuranReciter(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val riwayah: String,
    val baseUrl: String
) {
    ALAFASY(
        id = "alafasy",
        nameArabic = "الشيخ مشاري راشد العفاسي",
        nameEnglish = "Mishary Alafasy",
        riwayah = "حفص عن عاصم",
        baseUrl = "https://everyayah.com/data/Alafasy_128kbps/"
    ),
    ABDUL_BASIT(
        id = "abdul_basit",
        nameArabic = "الشيخ عبد الباسط عبد الصمد",
        nameEnglish = "Abdul Basit Murattal",
        riwayah = "مرتل - حفص عن عاصم",
        baseUrl = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/"
    ),
    AL_HUSARY(
        id = "al_husary",
        nameArabic = "الشيخ محمود خليل الحصري",
        nameEnglish = "Mahmoud Khalil Al-Husary",
        riwayah = "مرتل - حفص عن عاصم",
        baseUrl = "https://everyayah.com/data/Husary_128kbps/"
    ),
    AL_MINSHAWI(
        id = "al_minshawi",
        nameArabic = "الشيخ محمد صديق المنشاوي",
        nameEnglish = "Mohamed Siddiq Al-Minshawi",
        riwayah = "مرتل - حفص عن عاصم",
        baseUrl = "https://everyayah.com/data/Minshawy_Murattal_128kbps/"
    ),
    AL_GHAMDI(
        id = "al_ghamdi",
        nameArabic = "الشيخ سعد الغامدي",
        nameEnglish = "Saad Al-Ghamdi",
        riwayah = "حفص عن عاصم",
        baseUrl = "https://everyayah.com/data/Ghamadi_40kbps/"
    ),
    AL_MUAIQLY(
        id = "al_muaiqly",
        nameArabic = "الشيخ ماهر المعيقلي",
        nameEnglish = "Maher Al-Muaiqly",
        riwayah = "حفص عن عاصم",
        baseUrl = "https://everyayah.com/data/MaherAlMuaiqly128kbps/"
    ),
    ASH_SHATRI(
        id = "ash_shatri",
        nameArabic = "الشيخ أبو بكر الشاطري",
        nameEnglish = "Abu Bakr Ash-Shatri",
        riwayah = "حفص عن عاصم",
        baseUrl = "https://everyayah.com/data/Abu_Bakr_Ash-Shatri_128kbps/"
    )
}

enum class RepeatMode(val count: Int, val labelArabic: String, val badge: String) {
    ONCE(1, "بدون تكرار (مرة واحدة)", "١x"),
    TWICE(2, "تكرار مرتان", "٢x"),
    THREE_TIMES(3, "تكرار ٣ مرات", "٣x"),
    FIVE_TIMES(5, "تكرار ٥ مرات", "٥x"),
    INFINITE(-1, "تكرار مستمر للآية (للحفظ)", "∞")
}

enum class AutoNightModeOption(val titleArabic: String, val subtitleArabic: String) {
    DISABLED("معطل", "التبديل اليدوي فقط عبر الزر السريع"),
    AUTO_TIME("تلقائي حسب المساء (٧م - ٦ص)", "تفعيل الوضع الليلي تلقائياً من المساء حتى الفجر"),
    AUTO_LIGHT_SENSOR("تلقائي بحساس الإضاءة", "تفعيل فوري عند القراءة في إضاءة منخفضة أو مظلمة"),
    FOLLOW_SYSTEM("اتباع مظهر النظام", "مزامنة تلقائية مع الوضع الداكن لنظام الهاتف")
}

data class ReadingSettings(
    val quranFontSize: Float = 27f,
    val tafsirFontSize: Float = 16f,
    val theme: ReadingTheme = ReadingTheme.PARCHMENT,
    val previousDayTheme: ReadingTheme = ReadingTheme.PARCHMENT,
    val readingMode: ReadingMode = ReadingMode.AYAH_BY_AYAH,
    val showTashkeel: Boolean = true,
    val selectedTafsir: TafsirSource = TafsirSource.JALALAYN,
    val selectedReciter: QuranReciter = QuranReciter.ALAFASY,
    val repeatMode: RepeatMode = RepeatMode.ONCE,
    val autoScrollAudio: Boolean = true,
    val autoNightMode: AutoNightModeOption = AutoNightModeOption.AUTO_TIME,
    val eyeComfortWarmFilter: Boolean = false
)

enum class MatchType(val titleArabic: String) {
    QURAN("نص الآية الكريمة"),
    JALALAYN("تفسير الجلالين"),
    MUYASSAR("التفسير الميسر")
}

data class SearchResult(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val ayahText: String,
    val snippet: String,
    val matchType: MatchType,
    val highlightQuery: String
)

data class JuzInfo(
    val juzNumber: Int,
    val nameArabic: String,
    val startSurahNumber: Int,
    val startSurahName: String,
    val startAyahNumber: Int,
    val endSurahName: String
)
