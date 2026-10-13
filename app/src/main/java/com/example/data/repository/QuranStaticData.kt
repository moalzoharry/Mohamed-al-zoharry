package com.example.data.repository

import com.example.data.model.JuzInfo
import com.example.data.model.RevelationType
import com.example.data.model.SurahInfo

object QuranStaticData {

    /**
     * Comprehensive Arabic normalization for fast, flexible search without Tashkeel (diacritics).
     * Intelligently aligns Uthmani Quranic script and modern search queries:
     * - Strips all Harakat (Fathah, Dammah, Kasrah, Sukoon, Shaddah, Tanween, etc.)
     * - Adapts Uthmani spellings (e.g. الصلوة -> الصلاة, الزكوة -> الزكاة, الحيوة -> الحياة)
     * - Normalizes dagger alef (الرحمن, ذلك, هذا, هذه, هؤلاء, لكن, أولئك, الصراط, إبراهيم, إسماعيل, السماوات)
     * - Normalizes all Alef forms (أ, إ, آ, ٱ, ءا -> ا)
     * - Normalizes interchangeable letters (ة / ه, ى / ي, ؤ / و, ئ / ي)
     */
    fun normalizeArabic(text: String): String {
        if (text.isBlank()) return ""
        var t = text

        // 1. Quranic Uthmani specific spelling adaptations:
        // 'صلوة', 'زكوة', 'حيوة' -> صلاة, زكاة, حياة
        t = t.replace(Regex("و[\\u0670]?ة"), "اة")

        // Words with dagger alef written without alef in standard modern Arabic:
        t = t.replace(Regex("رَّ?حْمَ?[\\u0670]ن"), "رحمن")
        t = t.replace(Regex("إِلَ?[\\u0670]ه"), "اله")
        t = t.replace(Regex("ذَ?[\\u0670]لِ?ك"), "ذلك")
        t = t.replace(Regex("هَ?[\\u0670]ذَا"), "هذا")
        t = t.replace(Regex("هَ?[\\u0670]ذِ"), "هذ")
        t = t.replace(Regex("هَ?[\\u0670]ؤُ"), "هؤ")
        t = t.replace(Regex("لَ?[\\u0670]كِ?ن"), "لكن")
        t = t.replace(Regex("أُ?و?لَ?[\\u0670]"), "اول")

        // 2. Dagger alef (\u0670) in all other words represents long vowel alef (الصراط, ابراهيم, اسماعيل, سماوات, عالمين)
        t = t.replace('\u0670', 'ا')
        t = t.replace('\u06E6', 'ي') // Uthmani small ya
        t = t.replace('\u06E5', 'و') // Uthmani small waw

        // 3. Remove all Tashkeel, diacritics, Quranic stop marks, pause signs, and ornamentations
        t = t.replace(Regex("[\\u064B-\\u065F\\u06D6-\\u06ED\\u0610-\\u061A\\u08F0-\\u08FE\\u06DF-\\u06E4\\u06E7-\\u06EE]"), "")

        // 4. Tatweel / Kashida
        t = t.replace("ـ", "")

        // 5. Quranic hamza before alef (ءا -> ا)
        t = t.replace("ءا", "ا")

        // 6. Normalize all forms of Alef (أ, إ, آ, ٱ -> ا)
        t = t.replace(Regex("[إأآٱ]"), "ا")

        // 7. Normalize Ya and Alef Maqsura (ى -> ي)
        t = t.replace('ى', 'ي')

        // 8. Normalize Ta Marbuta and Ha (ة -> ه) for universal matching
        t = t.replace('ة', 'ه')

        // 9. Normalize Hamza on Waw and Ya
        t = t.replace('ؤ', 'و')
        t = t.replace('ئ', 'ي')

        // 10. Trim and collapse whitespace
        return t.replace(Regex("\\s+"), " ").trim()
    }

    val surahs: List<SurahInfo> = listOf(
        SurahInfo(1, "الفَاتِحَة", "Al-Fatihah", "The Opening", RevelationType.MAKKI, 7, 1, 1),
        SurahInfo(2, "البَقَرَة", "Al-Baqarah", "The Cow", RevelationType.MADANI, 286, 1, 2),
        SurahInfo(3, "آل عِمْرَان", "Ali 'Imran", "Family of Imran", RevelationType.MADANI, 200, 3, 50),
        SurahInfo(4, "النِّسَاء", "An-Nisa", "The Women", RevelationType.MADANI, 176, 4, 77),
        SurahInfo(5, "المَائِدَة", "Al-Ma'idah", "The Table Spread", RevelationType.MADANI, 120, 6, 106),
        SurahInfo(6, "الأَنْعَام", "Al-An'am", "The Cattle", RevelationType.MAKKI, 165, 7, 128),
        SurahInfo(7, "الأَعْرَاف", "Al-A'raf", "The Heights", RevelationType.MAKKI, 206, 8, 151),
        SurahInfo(8, "الأَنْفَال", "Al-Anfal", "The Spoils of War", RevelationType.MADANI, 75, 9, 177),
        SurahInfo(9, "التَّوْبَة", "At-Tawbah", "The Repentance", RevelationType.MADANI, 129, 10, 187),
        SurahInfo(10, "يُونُس", "Yunus", "Jonah", RevelationType.MAKKI, 109, 11, 208),
        SurahInfo(11, "هُود", "Hud", "Hud", RevelationType.MAKKI, 123, 11, 221),
        SurahInfo(12, "يُوسُف", "Yusuf", "Joseph", RevelationType.MAKKI, 111, 12, 235),
        SurahInfo(13, "الرَّعْد", "Ar-Ra'd", "The Thunder", RevelationType.MADANI, 43, 13, 249),
        SurahInfo(14, "إِبْرَاهِيم", "Ibrahim", "Abraham", RevelationType.MAKKI, 52, 13, 255),
        SurahInfo(15, "الحِجْر", "Al-Hijr", "The Rocky Tract", RevelationType.MAKKI, 99, 14, 262),
        SurahInfo(16, "النَّحْل", "An-Nahl", "The Bee", RevelationType.MAKKI, 128, 14, 267),
        SurahInfo(17, "الإِسْرَاء", "Al-Isra", "The Night Journey", RevelationType.MAKKI, 111, 15, 282),
        SurahInfo(18, "الكَهْف", "Al-Kahf", "The Cave", RevelationType.MAKKI, 110, 15, 293),
        SurahInfo(19, "مَرْيَم", "Maryam", "Mary", RevelationType.MAKKI, 98, 16, 305),
        SurahInfo(20, "طه", "Ta-Ha", "Ta-Ha", RevelationType.MAKKI, 135, 16, 312),
        SurahInfo(21, "الأَنْبِيَاء", "Al-Anbiya", "The Prophets", RevelationType.MAKKI, 112, 17, 322),
        SurahInfo(22, "الحَجّ", "Al-Hajj", "The Pilgrimage", RevelationType.MADANI, 78, 17, 332),
        SurahInfo(23, "المُؤْمِنُون", "Al-Mu'minun", "The Believers", RevelationType.MAKKI, 118, 18, 342),
        SurahInfo(24, "النُّور", "An-Nur", "The Light", RevelationType.MADANI, 64, 18, 350),
        SurahInfo(25, "الفُرْقَان", "Al-Furqan", "The Criterion", RevelationType.MAKKI, 77, 18, 359),
        SurahInfo(26, "الشُّعَرَاء", "Ash-Shu'ara", "The Poets", RevelationType.MAKKI, 227, 19, 367),
        SurahInfo(27, "النَّمْل", "An-Naml", "The Ants", RevelationType.MAKKI, 93, 19, 377),
        SurahInfo(28, "القَصَص", "Al-Qasas", "The Stories", RevelationType.MAKKI, 88, 20, 385),
        SurahInfo(29, "العَنْكَبُوت", "Al-'Ankabut", "The Spider", RevelationType.MAKKI, 69, 20, 396),
        SurahInfo(30, "الرُّوم", "Ar-Rum", "The Romans", RevelationType.MAKKI, 60, 21, 404),
        SurahInfo(31, "لُقْمَان", "Luqman", "Luqman", RevelationType.MAKKI, 34, 21, 411),
        SurahInfo(32, "السَّجْدَة", "As-Sajdah", "The Prostration", RevelationType.MAKKI, 30, 21, 415),
        SurahInfo(33, "الأَحْزَاب", "Al-Ahzab", "The Combined Forces", RevelationType.MADANI, 73, 21, 418),
        SurahInfo(34, "سَبَأ", "Saba", "Sheba", RevelationType.MAKKI, 54, 22, 428),
        SurahInfo(35, "فَاطِر", "Fatir", "Originator", RevelationType.MAKKI, 45, 22, 434),
        SurahInfo(36, "يس", "Ya-Sin", "Ya-Sin", RevelationType.MAKKI, 83, 22, 440),
        SurahInfo(37, "الصَّافَّات", "As-Saffat", "Those who set the Ranks", RevelationType.MAKKI, 182, 23, 446),
        SurahInfo(38, "ص", "Sad", "The Letter Sad", RevelationType.MAKKI, 88, 23, 453),
        SurahInfo(39, "الزُّمَر", "Az-Zumar", "The Troops", RevelationType.MAKKI, 75, 23, 458),
        SurahInfo(40, "غَافِر", "Ghafir", "The Forgiver", RevelationType.MAKKI, 85, 24, 467),
        SurahInfo(41, "فُصِّلَت", "Fussilat", "Explained in Detail", RevelationType.MAKKI, 54, 24, 477),
        SurahInfo(42, "الشُّورَى", "Ash-Shura", "The Consultation", RevelationType.MAKKI, 53, 25, 483),
        SurahInfo(43, "الزُّخْرُف", "Az-Zukhruf", "The Ornaments of Gold", RevelationType.MAKKI, 89, 25, 489),
        SurahInfo(44, "الدُّخَان", "Ad-Dukhan", "The Smoke", RevelationType.MAKKI, 59, 25, 496),
        SurahInfo(45, "الجَاثِيَة", "Al-Jathiyah", "The Crouching", RevelationType.MAKKI, 37, 25, 499),
        SurahInfo(46, "الأَحْقَاف", "Al-Ahqaf", "The Wind-Curved Sandhills", RevelationType.MAKKI, 35, 26, 502),
        SurahInfo(47, "مُحَمَّد", "Muhammad", "Muhammad", RevelationType.MADANI, 38, 26, 507),
        SurahInfo(48, "الفَتْح", "Al-Fath", "The Victory", RevelationType.MADANI, 29, 26, 511),
        SurahInfo(49, "الحُجُرَات", "Al-Hujurat", "The Rooms", RevelationType.MADANI, 18, 26, 515),
        SurahInfo(50, "ق", "Qaf", "The Letter Qaf", RevelationType.MAKKI, 45, 26, 518),
        SurahInfo(51, "الذَّارِيَات", "Adh-Dhariyat", "The Winnowing Winds", RevelationType.MAKKI, 60, 26, 520),
        SurahInfo(52, "الطُّور", "At-Tur", "The Mount", RevelationType.MAKKI, 49, 27, 523),
        SurahInfo(53, "النَّجْم", "An-Najm", "The Star", RevelationType.MAKKI, 62, 27, 526),
        SurahInfo(54, "القَمَر", "Al-Qamar", "The Moon", RevelationType.MAKKI, 55, 27, 528),
        SurahInfo(55, "الرَّحْمَن", "Ar-Rahman", "The Beneficent", RevelationType.MADANI, 78, 27, 531),
        SurahInfo(56, "الوَاقِعَة", "Al-Waqi'ah", "The Inevitable", RevelationType.MAKKI, 96, 27, 534),
        SurahInfo(57, "الحَدِيد", "Al-Hadid", "The Iron", RevelationType.MADANI, 29, 27, 537),
        SurahInfo(58, "المُجَادِلَة", "Al-Mujadila", "The Pleading Woman", RevelationType.MADANI, 22, 28, 542),
        SurahInfo(59, "الحَشْر", "Al-Hashr", "The Exile", RevelationType.MADANI, 24, 28, 545),
        SurahInfo(60, "المُمْتَحَنَة", "Al-Mumtahanah", "She that is to be examined", RevelationType.MADANI, 13, 28, 549),
        SurahInfo(61, "الصَّفّ", "As-Saff", "The Ranks", RevelationType.MADANI, 14, 28, 551),
        SurahInfo(62, "الجُمُعَة", "Al-Jumu'ah", "The Congregation", RevelationType.MADANI, 11, 28, 553),
        SurahInfo(63, "المُنَافِقُون", "Al-Munafiqun", "The Hypocrites", RevelationType.MADANI, 11, 28, 554),
        SurahInfo(64, "التَّغَابُن", "At-Taghabun", "The Mutual Disillusion", RevelationType.MADANI, 18, 28, 556),
        SurahInfo(65, "الطَّلَاق", "At-Talaq", "The Divorce", RevelationType.MADANI, 12, 28, 558),
        SurahInfo(66, "التَّحْرِيم", "At-Tahrim", "The Prohibition", RevelationType.MADANI, 12, 28, 560),
        SurahInfo(67, "المُلْك", "Al-Mulk", "The Sovereignty", RevelationType.MAKKI, 30, 29, 562),
        SurahInfo(68, "القَلَم", "Al-Qalam", "The Pen", RevelationType.MAKKI, 52, 29, 564),
        SurahInfo(69, "الحَاقَّة", "Al-Haqqah", "The Reality", RevelationType.MAKKI, 52, 29, 566),
        SurahInfo(70, "المَعَارِج", "Al-Ma'arij", "The Ascending Stairways", RevelationType.MAKKI, 44, 29, 568),
        SurahInfo(71, "نُوح", "Nuh", "Noah", RevelationType.MAKKI, 28, 29, 570),
        SurahInfo(72, "الجِنّ", "Al-Jinn", "The Jinn", RevelationType.MAKKI, 28, 29, 572),
        SurahInfo(73, "المُزَّمِّل", "Al-Muzzammil", "The Enshrouded One", RevelationType.MAKKI, 20, 29, 574),
        SurahInfo(74, "المُدَّثِّر", "Al-Muddaththir", "The Cloaked One", RevelationType.MAKKI, 56, 29, 575),
        SurahInfo(75, "القِيَامَة", "Al-Qiyamah", "The Resurrection", RevelationType.MAKKI, 40, 29, 577),
        SurahInfo(76, "الإِنْسَان", "Al-Insan", "Man", RevelationType.MADANI, 31, 29, 578),
        SurahInfo(77, "المُرْسَلَات", "Al-Mursalat", "The Emissaries", RevelationType.MAKKI, 50, 29, 580),
        SurahInfo(78, "النَّبَأ", "An-Naba", "The Tidings", RevelationType.MAKKI, 40, 30, 582),
        SurahInfo(79, "النَّازِعَات", "An-Nazi'at", "Those who drag forth", RevelationType.MAKKI, 46, 30, 583),
        SurahInfo(80, "عَبَسَ", "'Abasa", "He Frowned", RevelationType.MAKKI, 42, 30, 585),
        SurahInfo(81, "التَّكْوِير", "At-Takwir", "The Overthrowing", RevelationType.MAKKI, 29, 30, 586),
        SurahInfo(82, "الانْفِطَار", "Al-Infitar", "The Cleaving", RevelationType.MAKKI, 19, 30, 587),
        SurahInfo(83, "المُطَفِّفِين", "Al-Mutaffifin", "Defrauding", RevelationType.MAKKI, 36, 30, 587),
        SurahInfo(84, "الانْشِقَاق", "Al-Inshiqaq", "The Splitting Open", RevelationType.MAKKI, 25, 30, 589),
        SurahInfo(85, "البُرُوج", "Al-Buruj", "The Mansions of the Stars", RevelationType.MAKKI, 22, 30, 590),
        SurahInfo(86, "الطَّارِق", "At-Tariq", "The Morning Star", RevelationType.MAKKI, 17, 30, 591),
        SurahInfo(87, "الأَعْلَى", "Al-A'la", "The Most High", RevelationType.MAKKI, 19, 30, 591),
        SurahInfo(88, "الغَاشِيَة", "Al-Ghashiyah", "The Overwhelming", RevelationType.MAKKI, 26, 30, 592),
        SurahInfo(89, "الفَجْر", "Al-Fajr", "The Dawn", RevelationType.MAKKI, 30, 30, 593),
        SurahInfo(90, "البَلَد", "Al-Balad", "The City", RevelationType.MAKKI, 20, 30, 594),
        SurahInfo(91, "الشَّمْس", "Ash-Shams", "The Sun", RevelationType.MAKKI, 15, 30, 595),
        SurahInfo(92, "اللَّيْل", "Al-Layl", "The Night", RevelationType.MAKKI, 21, 30, 595),
        SurahInfo(93, "الضُّحَى", "Ad-Duha", "The Morning Brightness", RevelationType.MAKKI, 11, 30, 596),
        SurahInfo(94, "الشَّرْح", "Ash-Sharh", "The Relief", RevelationType.MAKKI, 8, 30, 596),
        SurahInfo(95, "التِّين", "At-Tin", "The Fig", RevelationType.MAKKI, 8, 30, 597),
        SurahInfo(96, "العَلَق", "Al-'Alaq", "The Clot", RevelationType.MAKKI, 19, 30, 597),
        SurahInfo(97, "القَدْر", "Al-Qadr", "The Night of Power", RevelationType.MAKKI, 5, 30, 598),
        SurahInfo(98, "البَيِّنَة", "Al-Bayyinah", "The Clear Proof", RevelationType.MADANI, 8, 30, 598),
        SurahInfo(99, "الزَّلْزَلَة", "Az-Zalzalah", "The Earthquake", RevelationType.MADANI, 8, 30, 599),
        SurahInfo(100, "العَادِيَات", "Al-'Adiyat", "The Coursers", RevelationType.MAKKI, 11, 30, 599),
        SurahInfo(101, "القَارِعَة", "Al-Qari'ah", "The Calamity", RevelationType.MAKKI, 11, 30, 600),
        SurahInfo(102, "التَّكَاثُر", "At-Takathur", "Competition in Increase", RevelationType.MAKKI, 8, 30, 600),
        SurahInfo(103, "العَصْر", "Al-'Asr", "The Declining Day", RevelationType.MAKKI, 3, 30, 601),
        SurahInfo(104, "الهُمَزَة", "Al-Humazah", "The Traducer", RevelationType.MAKKI, 9, 30, 601),
        SurahInfo(105, "الفِيل", "Al-Fil", "The Elephant", RevelationType.MAKKI, 5, 30, 601),
        SurahInfo(106, "قُرَيْش", "Quraysh", "Quraysh", RevelationType.MAKKI, 4, 30, 602),
        SurahInfo(107, "المَاعُون", "Al-Ma'un", "The Small Kindness", RevelationType.MAKKI, 7, 30, 602),
        SurahInfo(108, "الكَوْثَر", "Al-Kawthar", "Abundance", RevelationType.MAKKI, 3, 30, 602),
        SurahInfo(109, "الكَافِرُون", "Al-Kafirun", "The Disbelievers", RevelationType.MAKKI, 6, 30, 603),
        SurahInfo(110, "النَّصْر", "An-Nasr", "The Divine Support", RevelationType.MADANI, 3, 30, 603),
        SurahInfo(111, "المَسَد", "Al-Masad", "The Palm Fiber", RevelationType.MAKKI, 5, 30, 603),
        SurahInfo(112, "الإِخْلَاص", "Al-Ikhlas", "The Sincerity", RevelationType.MAKKI, 4, 30, 604),
        SurahInfo(113, "الفَلَق", "Al-Falaq", "The Daybreak", RevelationType.MAKKI, 5, 30, 604),
        SurahInfo(114, "النَّاس", "An-Nas", "Mankind", RevelationType.MAKKI, 6, 30, 604)
    )

    fun getSurah(number: Int): SurahInfo? = surahs.find { it.number == number }

    val juzList: List<JuzInfo> = listOf(
        JuzInfo(1, "الجزء الأول", 1, "الفاتحة", 1, "البقرة 141"),
        JuzInfo(2, "الجزء الثاني (سَيَقُولُ)", 2, "البقرة", 142, "البقرة 252"),
        JuzInfo(3, "الجزء الثالث (تِلْكَ الرُّسُلُ)", 2, "البقرة", 253, "آل عمران 92"),
        JuzInfo(4, "الجزء الرابع (لَنْ تَنَالُوا)", 3, "آل عمران", 93, "النساء 23"),
        JuzInfo(5, "الجزء الخامس (وَالمُحْصَنَاتُ)", 4, "النساء", 24, "النساء 147"),
        JuzInfo(6, "الجزء السادس (لَا يُحِبُّ اللَّهُ)", 4, "النساء", 148, "المائدة 81"),
        JuzInfo(7, "الجزء السابع (وَإِذَا سَمِعُوا)", 5, "المائدة", 82, "الأنعام 110"),
        JuzInfo(8, "الجزء الثامن (وَلَوْ أَنَّنَا)", 6, "الأنعام", 111, "الأعراف 87"),
        JuzInfo(9, "الجزء التاسع (قَالَ المَلَأُ)", 7, "الأعراف", 88, "الأنفال 40"),
        JuzInfo(10, "الجزء العاشر (وَاعْلَمُوا)", 8, "الأنفال", 41, "التوبة 92"),
        JuzInfo(11, "الجزء الحادي عشر (يَعْتَذِرُونَ)", 9, "التوبة", 93, "هود 5"),
        JuzInfo(12, "الجزء الثاني عشر (وَمَا مِنْ دَابَّةٍ)", 11, "هود", 6, "يوسف 52"),
        JuzInfo(13, "الجزء الثالث عشر (وَمَا أُبَرِّئُ)", 12, "يوسف", 53, "إبراهيم 52"),
        JuzInfo(14, "الجزء الرابع عشر (رُبَمَا)", 15, "الحجر", 1, "النحل 128"),
        JuzInfo(15, "الجزء الخامس عشر (سُبْحَانَ الَّذِي)", 17, "الإسراء", 1, "الكهف 74"),
        JuzInfo(16, "الجزء السادس عشر (قَالَ أَلَمْ)", 18, "الكهف", 75, "طه 135"),
        JuzInfo(17, "الجزء السابع عشر (اقْتَرَبَ)", 21, "الأنبياء", 1, "الحج 78"),
        JuzInfo(18, "الجزء الثامن عشر (قَدْ أَفْلَحَ)", 23, "المؤمنون", 1, "الفرقان 20"),
        JuzInfo(19, "الجزء التاسع عشر (وَقَالَ الَّذِينَ)", 25, "الفرقان", 21, "النمل 55"),
        JuzInfo(20, "الجزء العشرون (أَمَّنْ خَلَقَ)", 27, "النمل", 56, "العنكبوت 45"),
        JuzInfo(21, "الجزء الحادي والعشرون (اتْلُ مَا أُوحِيَ)", 29, "العنكبوت", 46, "الأحزاب 30"),
        JuzInfo(22, "الجزء الثاني والعشرون (وَمَنْ يَقْنُتْ)", 33, "الأحزاب", 31, "يس 27"),
        JuzInfo(23, "الجزء الثالث والعشرون (وَمَا أَنْزَلْنَا)", 36, "يس", 28, "الزمر 31"),
        JuzInfo(24, "الجزء الرابع والعشرون (فَمَنْ أَظْلَمُ)", 39, "الزمر", 32, "فصلت 46"),
        JuzInfo(25, "الجزء الخامس والعشرون (إِلَيْهِ يُرَدُّ)", 41, "فصلت", 47, "الجاثية 37"),
        JuzInfo(26, "الجزء السادس والعشرون (حم)", 46, "الأحقاف", 1, "الذاريات 30"),
        JuzInfo(27, "الجزء السابع والعشرون (قَالَ فَمَا خَطْبُكُمْ)", 51, "الذاريات", 31, "الحديد 29"),
        JuzInfo(28, "الجزء الثامن والعشرون (قَدْ سَمِعَ اللَّهُ)", 58, "المجادلة", 1, "التحريم 12"),
        JuzInfo(29, "الجزء التاسع والعشرون (تَبَارَكَ الَّذِي)", 67, "الملك", 1, "المرسلات 50"),
        JuzInfo(30, "الجزء الثلاثون (عَمَّ يَتَسَاءَلُونَ)", 78, "النبأ", 1, "الناس 6")
    )
}
