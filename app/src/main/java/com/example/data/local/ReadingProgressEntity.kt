package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks the user's stopped reading position per Surah across all 114 Surahs of the Holy Quran,
 * allowing the user to resume reading wherever they stopped in any Surah (e.g. Al-Baqarah, Al-Kahf, etc.).
 */
@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val ayahTextPreview: String,
    val timestamp: Long = System.currentTimeMillis()
)
