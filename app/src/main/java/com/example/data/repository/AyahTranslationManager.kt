package com.example.data.repository

import com.example.data.remote.QuranApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TranslationLanguage(
    val code: String,
    val nameArabic: String,
    val nameNative: String,
    val flagEmoji: String
)

object AyahTranslationManager {

    val supportedLanguages = listOf(
        TranslationLanguage("en", "الإنجليزية", "English (Sahih Int.)", "🇬🇧"),
        TranslationLanguage("fr", "الفرنسية", "Français (Hamidullah)", "🇫🇷"),
        TranslationLanguage("tr", "التركية", "Türkçe (Diyanet)", "🇹🇷"),
        TranslationLanguage("ur", "الأردية", "اردو (Jalandhry)", "🇵🇰"),
        TranslationLanguage("de", "الألمانية", "Deutsch (Bubenheim)", "🇩🇪")
    )

    private val apiClient = QuranApiClient()
    private val memoryCache = mutableMapOf<String, String>()

    // Offline built-in translations for prominent Ayahs
    private val offlineTranslations = mapOf(
        // Al-Fatihah
        "1:1:en" to "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
        "1:2:en" to "[All] praise is [due] to Allah, Lord of the worlds -",
        "1:3:en" to "The Entirely Merciful, the Especially Merciful,",
        "1:4:en" to "Sovereign of the Day of Recompense.",
        "1:5:en" to "It is You we worship and You we ask for help.",
        "1:6:en" to "Guide us to the straight path -",
        "1:7:en" to "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.",

        // Ayat Al-Kursi (2:255)
        "2:255:en" to "Allah - there is no deity except Him, the Ever-Living, the Sustainer of [all] existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is [presently] before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",

        // Al-Ikhlas
        "112:1:en" to "Say, \"He is Allah, [who is] One,",
        "112:2:en" to "Allah, the Eternal Refuge.",
        "112:3:en" to "He neither begets nor is born,",
        "112:4:en" to "Nor is there to Him any equivalent.\"",

        // Al-Falaq
        "113:1:en" to "Say, \"I seek refuge in the Lord of daybreak",
        "113:2:en" to "From the evil of that which He created",
        "113:3:en" to "And from the evil of darkness when it settles",
        "113:4:en" to "And from the evil of the blowers in knots",
        "113:5:en" to "And from the evil of an envier when he envies.\"",

        // An-Nas
        "114:1:en" to "Say, \"I seek refuge in the Lord of mankind,",
        "114:2:en" to "The Sovereign of mankind,",
        "114:3:en" to "The God of mankind,",
        "114:4:en" to "From the evil of the retreating whisperer -",
        "114:5:en" to "Who whispers [evil] into the breasts of mankind -",
        "114:6:en" to "From among the jinn and mankind.\"",

        // Al-Asr
        "103:1:en" to "By time,",
        "103:2:en" to "Indeed, mankind is in loss,",
        "103:3:en" to "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.",

        // Al-Kawthar
        "108:1:en" to "Indeed, We have granted you, [O Muhammad], al-Kawthar.",
        "108:2:en" to "So pray to your Lord and sacrifice [to Him alone].",
        "108:3:en" to "Indeed, your enemy is the one cut off."
    )

    suspend fun getTranslation(
        surahNumber: Int,
        ayahNumber: Int,
        languageCode: String = "en"
    ): String = withContext(Dispatchers.IO) {
        val cacheKey = "$surahNumber:$ayahNumber:$languageCode"

        // 1. Check memory cache
        memoryCache[cacheKey]?.let { return@withContext it }

        // 2. Check offline preloaded
        offlineTranslations[cacheKey]?.let {
            memoryCache[cacheKey] = it
            return@withContext it
        }

        // 3. Fetch from Quran API
        val fetched = apiClient.fetchAyahTranslation(surahNumber, ayahNumber, languageCode)
        if (!fetched.isNullOrBlank()) {
            memoryCache[cacheKey] = fetched
            return@withContext fetched
        }

        // 4. Fallback if offline and not in cache
        return@withContext when (languageCode) {
            "en" -> "Translation requires internet connection for Surah $surahNumber Ayah $ayahNumber."
            "fr" -> "La traduction nécessite une connexion Internet pour la sourate $surahNumber verset $ayahNumber."
            "tr" -> "Bu ayet için tercüme internet bağlantısı gerektirir."
            else -> "الترجمة تتطلب اتصالاً بالإنترنت لهذه الآية الكريمة."
        }
    }
}
