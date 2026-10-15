package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.repository.QuranEmbeddedTafsirData
import com.example.data.repository.QuranStaticData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber LIMIT 1")
    suspend fun getBookmarkForAyah(surahNumber: Int, ayahNumber: Int): BookmarkEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber)")
    fun isBookmarkedFlow(surahNumber: Int, ayahNumber: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun deleteBySurahAndAyah(surahNumber: Int, ayahNumber: Int)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress ORDER BY timestamp DESC LIMIT 1")
    fun getLastRead(): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE surahNumber = :surahNumber LIMIT 1")
    fun getProgressForSurah(surahNumber: Int): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE surahNumber = :surahNumber LIMIT 1")
    suspend fun getProgressForSurahSync(surahNumber: Int): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress ORDER BY timestamp DESC")
    fun getAllReadingProgress(): Flow<List<ReadingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setLastRead(progress: ReadingProgressEntity)

    @Query("DELETE FROM reading_progress WHERE surahNumber = :surahNumber")
    suspend fun deleteProgressForSurah(surahNumber: Int)
}

@Database(
    entities = [
        BookmarkEntity::class,
        ReadingProgressEntity::class,
        QuranAyahEntity::class,
        SurahEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun quranAyahDao(): QuranAyahDao
    abstract fun surahDao(): SurahDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getInstance(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quran_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate database with Surahs and embedded verses in background
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedDatabase(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance

                // Also check if initial seeding is needed on start
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(instance)
                }

                instance
            }
        }

        /**
         * Seeds initial authentic Quranic verses, Tafsir Al-Jalalayn, and Tafsir Al-Muyassar
         * into Room for offline fast access.
         */
        suspend fun seedDatabase(database: QuranDatabase) {
            try {
                // 1. Seed Surahs if empty
                val surahCount = database.surahDao().getSurahCount()
                if (surahCount == 0) {
                    val surahsToInsert = QuranStaticData.surahs.map { SurahEntity.fromSurahInfo(it) }
                    database.surahDao().insertSurahs(surahsToInsert)
                }

                // 2. Seed embedded Ayahs with Tafsirs if empty or missing
                val ayahCount = database.quranAyahDao().getTotalAyahsCount()
                if (ayahCount < 20) {
                    val entitiesToInsert = mutableListOf<QuranAyahEntity>()
                    QuranEmbeddedTafsirData.embeddedSurahsMap.values.forEach { ayahs ->
                        ayahs.forEach { ayah ->
                            entitiesToInsert.add(QuranAyahEntity.fromAyah(ayah))
                        }
                    }
                    if (entitiesToInsert.isNotEmpty()) {
                        database.quranAyahDao().insertAyahs(entitiesToInsert)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
