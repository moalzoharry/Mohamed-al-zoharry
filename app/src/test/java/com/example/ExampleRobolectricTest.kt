package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AutoNightModeOption
import com.example.data.model.ReadingSettings
import com.example.data.model.ReadingTheme
import com.example.data.repository.QuranStaticData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("الفرقان بلس", appName)
    }

    @Test
    fun `surahs static list contains 114 surahs`() {
        assertEquals(114, QuranStaticData.surahs.size)
        val fatihah = QuranStaticData.getSurah(1)
        assertNotNull(fatihah)
        assertEquals("الفاتحه", QuranStaticData.normalizeArabic(fatihah!!.nameArabic))
        assertEquals(7, fatihah.ayahCount)

        val baqarah = QuranStaticData.getSurah(2)
        assertNotNull(baqarah)
        assertEquals("البقره", QuranStaticData.normalizeArabic(baqarah!!.nameArabic))
        assertEquals(286, baqarah.ayahCount)

        val nas = QuranStaticData.getSurah(114)
        assertNotNull(nas)
        assertEquals("الناس", QuranStaticData.normalizeArabic(nas!!.nameArabic))
        assertEquals(6, nas.ayahCount)
    }

    @Test
    fun `search surah by name and number with arabic normalization`() {
        val surahs = QuranStaticData.surahs

        // Search by number "1"
        val byNum = surahs.filter { it.number.toString() == "1" }
        assertEquals(1, byNum.size)
        assertEquals("الفاتحه", QuranStaticData.normalizeArabic(byNum.first().nameArabic))

        // Search by number "114"
        val byNum114 = surahs.filter { it.number.toString() == "114" }
        assertEquals(1, byNum114.size)
        assertEquals("الناس", QuranStaticData.normalizeArabic(byNum114.first().nameArabic))

        // Search by name with normalization (e.g. searching "كهف" matches "الكهف")
        val query = "كهف"
        val normalized = QuranStaticData.normalizeArabic(query)
        val results = surahs.filter {
            QuranStaticData.normalizeArabic(it.nameArabic).contains(normalized)
        }
        assertTrue(results.any { it.number == 18 })

        // Search "يس"
        val yaseenResults = surahs.filter {
            QuranStaticData.normalizeArabic(it.nameArabic).contains("يس")
        }
        assertTrue(yaseenResults.any { it.number == 36 })
    }

    @Test
    fun `night mode and eye comfort settings default verification`() {
        val defaultSettings = ReadingSettings()
        assertEquals(ReadingTheme.PARCHMENT, defaultSettings.theme)
        assertEquals(AutoNightModeOption.AUTO_TIME, defaultSettings.autoNightMode)
        assertEquals(false, defaultSettings.eyeComfortWarmFilter)
    }
}
