package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.RevelationType
import com.example.data.model.SurahInfo

/**
 * Room entity representing a Surah of the Holy Quran, storing metadata
 * such as name, revelation type, verse count, juz, and starting page.
 */
@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val englishTranslation: String,
    val revelationType: String, // "MAKKI" or "MADANI"
    val ayahCount: Int,
    val startJuz: Int,
    val startPage: Int
) {
    fun toSurahInfo(): SurahInfo = SurahInfo(
        number = number,
        nameArabic = nameArabic,
        nameEnglish = nameEnglish,
        englishTranslation = englishTranslation,
        revelationType = if (revelationType == "MADANI") RevelationType.MADANI else RevelationType.MAKKI,
        ayahCount = ayahCount,
        startJuz = startJuz,
        startPage = startPage
    )

    companion object {
        fun fromSurahInfo(info: SurahInfo): SurahEntity = SurahEntity(
            number = info.number,
            nameArabic = info.nameArabic,
            nameEnglish = info.nameEnglish,
            englishTranslation = info.englishTranslation,
            revelationType = info.revelationType.name,
            ayahCount = info.ayahCount,
            startJuz = info.startJuz,
            startPage = info.startPage
        )
    }
}
