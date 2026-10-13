package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Ayah

/**
 * Room entity representing an Ayah (verse) of the Holy Quran,
 * storing authentic Quranic text along with Tafsir Al-Jalalayn
 * and Tafsir Al-Muyassar for fast offline reading and instantaneous queries.
 */
@Entity(
    tableName = "quran_ayahs",
    indices = [
        Index(value = ["surahNumber", "ayahNumber"], unique = true),
        Index(value = ["surahNumber"]),
        Index(value = ["juzNumber"]),
        Index(value = ["pageNumber"])
    ]
)
data class QuranAyahEntity(
    @PrimaryKey
    val id: Long = 0, // Deterministic: surahNumber * 10000 + ayahNumber
    val surahNumber: Int,
    val ayahNumber: Int,
    val textWithTashkeel: String,
    val textClean: String,
    val tafsirJalalayn: String,
    val tafsirMuyassar: String,
    val tafsirJalalaynClean: String = "",
    val tafsirMuyassarClean: String = "",
    val juzNumber: Int = 1,
    val pageNumber: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toAyah(): Ayah = Ayah(
        surahNumber = surahNumber,
        ayahNumber = ayahNumber,
        textWithTashkeel = textWithTashkeel,
        textClean = textClean,
        tafsirJalalayn = tafsirJalalayn,
        tafsirMuyassar = tafsirMuyassar,
        juzNumber = juzNumber,
        pageNumber = pageNumber
    )

    companion object {
        fun makeId(surahNumber: Int, ayahNumber: Int): Long {
            return surahNumber.toLong() * 10000L + ayahNumber.toLong()
        }

        fun fromAyah(ayah: Ayah): QuranAyahEntity = QuranAyahEntity(
            id = makeId(ayah.surahNumber, ayah.ayahNumber),
            surahNumber = ayah.surahNumber,
            ayahNumber = ayah.ayahNumber,
            textWithTashkeel = ayah.textWithTashkeel,
            textClean = ayah.textClean,
            tafsirJalalayn = ayah.tafsirJalalayn,
            tafsirMuyassar = ayah.tafsirMuyassar,
            tafsirJalalaynClean = com.example.data.repository.QuranStaticData.normalizeArabic(ayah.tafsirJalalayn),
            tafsirMuyassarClean = com.example.data.repository.QuranStaticData.normalizeArabic(ayah.tafsirMuyassar),
            juzNumber = ayah.juzNumber,
            pageNumber = ayah.pageNumber
        )
    }
}
