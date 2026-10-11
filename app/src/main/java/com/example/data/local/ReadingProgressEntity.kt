package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey
    val id: Int = 1,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val ayahTextPreview: String,
    val timestamp: Long = System.currentTimeMillis()
)
