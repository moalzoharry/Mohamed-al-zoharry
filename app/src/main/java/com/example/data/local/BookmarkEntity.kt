package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val ayahText: String,
    val note: String = "",
    val category: String = "عام", // e.g. "حفظ", "تدبر", "دعاء", "مراجعة"
    val timestamp: Long = System.currentTimeMillis()
)
