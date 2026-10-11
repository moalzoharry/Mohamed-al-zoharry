package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Quran verses and their dual Tafsirs
 * (Tafsir Al-Jalalayn and Tafsir Al-Muyassar).
 */
@Dao
interface QuranAyahDao {

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun getAyahsForSurah(surahNumber: Int): Flow<List<QuranAyahEntity>>

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    suspend fun getAyahsForSurahSync(surahNumber: Int): List<QuranAyahEntity>

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber LIMIT 1")
    fun getAyah(surahNumber: Int, ayahNumber: Int): Flow<QuranAyahEntity?>

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber LIMIT 1")
    suspend fun getAyahSync(surahNumber: Int, ayahNumber: Int): QuranAyahEntity?

    @Query("SELECT * FROM quran_ayahs WHERE juzNumber = :juzNumber ORDER BY surahNumber ASC, ayahNumber ASC")
    fun getAyahsForJuz(juzNumber: Int): Flow<List<QuranAyahEntity>>

    @Query("SELECT * FROM quran_ayahs WHERE pageNumber = :pageNumber ORDER BY surahNumber ASC, ayahNumber ASC")
    fun getAyahsForPage(pageNumber: Int): Flow<List<QuranAyahEntity>>

    @Query("SELECT COUNT(*) FROM quran_ayahs WHERE surahNumber = :surahNumber")
    suspend fun getAyahCountForSurah(surahNumber: Int): Int

    @Query("SELECT COUNT(*) FROM quran_ayahs")
    fun getTotalAyahsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM quran_ayahs")
    suspend fun getTotalAyahsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<QuranAyahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyah(ayah: QuranAyahEntity)

    // Fast indexed search in Quranic text
    @Query("SELECT * FROM quran_ayahs WHERE textClean LIKE '%' || :query || '%' OR textWithTashkeel LIKE '%' || :query || '%' ORDER BY surahNumber ASC, ayahNumber ASC LIMIT 100")
    suspend fun searchQuran(query: String): List<QuranAyahEntity>

    // Search in Tafsir Al-Jalalayn
    @Query("SELECT * FROM quran_ayahs WHERE tafsirJalalayn LIKE '%' || :query || '%' ORDER BY surahNumber ASC, ayahNumber ASC LIMIT 100")
    suspend fun searchJalalayn(query: String): List<QuranAyahEntity>

    // Search in Tafsir Al-Muyassar
    @Query("SELECT * FROM quran_ayahs WHERE tafsirMuyassar LIKE '%' || :query || '%' ORDER BY surahNumber ASC, ayahNumber ASC LIMIT 100")
    suspend fun searchMuyassar(query: String): List<QuranAyahEntity>

    // Comprehensive search across Quran text and both Tafsirs
    @Query("SELECT * FROM quran_ayahs WHERE textClean LIKE '%' || :query || '%' OR textWithTashkeel LIKE '%' || :query || '%' OR tafsirJalalayn LIKE '%' || :query || '%' OR tafsirMuyassar LIKE '%' || :query || '%' ORDER BY surahNumber ASC, ayahNumber ASC LIMIT 150")
    suspend fun searchAll(query: String): List<QuranAyahEntity>

    @Query("DELETE FROM quran_ayahs WHERE surahNumber = :surahNumber")
    suspend fun deleteAyahsForSurah(surahNumber: Int)

    @Query("DELETE FROM quran_ayahs")
    suspend fun clearAllAyahs()
}
