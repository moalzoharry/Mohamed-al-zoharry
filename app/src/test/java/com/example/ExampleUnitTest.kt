package com.example

import com.example.data.repository.QuranEmbeddedTafsirData
import com.example.data.repository.QuranStaticData
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testQuranSurahCatalogHas114Surahs() {
        assertEquals(114, QuranStaticData.surahs.size)
        val fatihah = QuranStaticData.getSurah(1)
        assertNotNull(fatihah)
        assertEquals("الفَاتِحَة", fatihah?.nameArabic)
        assertEquals(7, fatihah?.ayahCount)

        val nas = QuranStaticData.getSurah(114)
        assertNotNull(nas)
        assertEquals("النَّاس", nas?.nameArabic)
        assertEquals(6, nas?.ayahCount)
    }

    @Test
    fun testArabicNormalization() {
        val withTashkeel = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
        val normalized = QuranStaticData.normalizeArabic(withTashkeel)
        assertEquals("بسم الله الرحمن الرحيم", normalized)

        val withHamzas = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ"
        val normalizedHamzas = QuranStaticData.normalizeArabic(withHamzas)
        assertEquals("اياك نعبد واياك نستعين", normalizedHamzas)
    }

    @Test
    fun testEmbeddedTafsirDataCompleteness() {
        val fatihahAyahs = QuranEmbeddedTafsirData.getAyahsForSurah(1)
        assertNotNull(fatihahAyahs)
        assertEquals(7, fatihahAyahs?.size)

        fatihahAyahs?.forEach { ayah ->
            assertTrue(ayah.tafsirJalalayn.isNotBlank())
            assertTrue(ayah.tafsirMuyassar.isNotBlank())
            assertTrue(ayah.textWithTashkeel.isNotBlank())
        }
    }

    @Test
    fun testNightModeSettingsAndThemes() {
        val defaultSettings = com.example.data.model.ReadingSettings()
        assertFalse(defaultSettings.theme.isDark)
        assertEquals(com.example.data.model.ReadingTheme.PARCHMENT, defaultSettings.theme)
        assertEquals(com.example.data.model.AutoNightModeOption.AUTO_TIME, defaultSettings.autoNightMode)
        assertFalse(defaultSettings.eyeComfortWarmFilter)

        val nightTheme = com.example.data.model.ReadingTheme.NIGHT
        assertTrue(nightTheme.isDark)

        val emeraldTheme = com.example.data.model.ReadingTheme.EMERALD
        assertTrue(emeraldTheme.isDark)

        val sepiaTheme = com.example.data.model.ReadingTheme.SEPIA
        assertFalse(sepiaTheme.isDark)

        val whiteTheme = com.example.data.model.ReadingTheme.WHITE
        assertFalse(whiteTheme.isDark)

        assertEquals(4, com.example.data.model.AutoNightModeOption.values().size)
    }

    @Test
    fun testCairoPrayerTimesMaghribIsEvening() {
        val cairoCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Cairo"))
        val times = com.example.prayer.PrayerTimesCalculator.calculate(
            latitude = 30.0444,
            longitude = 31.2357,
            calendar = cairoCal,
            method = com.example.prayer.CalculationMethod.EGYPTIAN
        )
        val maghrib = times[com.example.prayer.PrayerType.MAGHRIB]
        assertNotNull(maghrib)
        assertEquals("مساءً", maghrib?.periodArabic)
        assertTrue("Maghrib must be evening hour (17 or 18), was ${maghrib?.hour24}", maghrib?.hour24 ?: 0 in 17..19)
        assertTrue(maghrib?.formatted12hLong?.contains("مساءً") == true)
        assertFalse(maghrib?.formatted12hLong?.contains("صباحاً") == true)
    }

    @Test
    fun testSermonAudioQualityPresets() {
        val qualities = com.example.sermon.SermonAudioQuality.values()
        assertEquals(3, qualities.size)
        val recommended = qualities.first { it.isRecommended }
        assertEquals(com.example.sermon.SermonAudioQuality.HIGH_EFFICIENCY_AAC, recommended)
        assertEquals(64000, recommended.bitRate)
        assertEquals("m4a", recommended.formatExtension)
    }
}
