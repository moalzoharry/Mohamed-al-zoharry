package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.BookmarkDao
import com.example.data.local.BookmarkEntity
import com.example.data.local.QuranAyahDao
import com.example.data.local.QuranAyahEntity
import com.example.data.local.QuranDatabase
import com.example.data.local.ReadingProgressDao
import com.example.data.local.ReadingProgressEntity
import com.example.data.local.SurahDao
import com.example.data.model.Ayah
import com.example.data.model.DailyWirdReminder
import com.example.data.model.MatchType
import com.example.data.model.ReadingMode
import com.example.data.model.ReadingSettings
import com.example.data.model.ReadingTheme
import com.example.data.model.SearchResult
import com.example.data.model.SurahInfo
import com.example.data.model.TafsirSource
import com.example.data.model.WirdGoalType
import com.example.data.remote.QuranApiClient
import com.example.reminder.DailyWirdScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SearchFilter(val titleArabic: String) {
    ALL("الكل (القرآن والتفاسير)"),
    QURAN_ONLY("نص القرآن فقط"),
    JALALAYN_ONLY("تفسير الجلالين فقط"),
    MUYASSAR_ONLY("التفسير الميسر فقط")
}

class QuranRepository(private val context: Context) {

    private val db = QuranDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()
    private val readingProgressDao: ReadingProgressDao = db.readingProgressDao()
    private val quranAyahDao: QuranAyahDao = db.quranAyahDao()
    private val surahDao: SurahDao = db.surahDao()

    private val apiClient = QuranApiClient()
    private val prefs: SharedPreferences = context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)

    // Memory cache of loaded surahs
    private val surahCache = mutableMapOf<Int, List<Ayah>>()

    // In-memory cache for full Quran and Tafsirs loaded from assets on demand
    private var cachedQuranJson: org.json.JSONObject? = null
    private var cachedJalalaynJson: org.json.JSONObject? = null
    private var cachedMuyassarJson: org.json.JSONObject? = null

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow = _settingsFlow.asStateFlow()

    private val _dailyWirdFlow = MutableStateFlow(loadDailyWirdReminder())
    val dailyWirdFlow = _dailyWirdFlow.asStateFlow()

    init {
        // Pre-populate Surah 1 (Al-Fatihah)
        loadSurahFromAssets(1)?.let {
            surahCache[1] = it
        }

        // Asynchronously ensure full Quran (6,236 verses) is indexed in Room database for instant search
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            ensureDatabasePopulated()
        }
    }

    /**
     * Checks if the local database has all 6,236 authentic ayahs.
     * If incomplete or if placeholder records from an earlier build exist,
     * populates/updates Room from the bundled assets.
     */
    suspend fun ensureDatabasePopulated() = withContext(Dispatchers.IO) {
        try {
            val totalCount = quranAyahDao.getTotalAyahsCount()
            val sampleAyah = quranAyahDao.getAyahSync(2, 3)
            val needsPopulation = totalCount < 6236 || sampleAyah == null || sampleAyah.textWithTashkeel.startsWith("﴿سورة")

            if (needsPopulation) {
                val allEntities = mutableListOf<QuranAyahEntity>()
                for (sNum in 1..114) {
                    val ayahs = loadSurahFromAssets(sNum) ?: continue
                    allEntities.addAll(ayahs.map { QuranAyahEntity.fromAyah(it) })
                }
                if (allEntities.size >= 6236) {
                    allEntities.chunked(300).forEach { chunk ->
                        quranAyahDao.insertAyahs(chunk)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSettings(): ReadingSettings {
        val qSize = prefs.getFloat("quran_font_size", 27f)
        val tSize = prefs.getFloat("tafsir_font_size", 16f)
        val themeName = prefs.getString("reading_theme", ReadingTheme.PARCHMENT.name) ?: ReadingTheme.PARCHMENT.name
        val theme = try { ReadingTheme.valueOf(themeName) } catch (_: Exception) { ReadingTheme.PARCHMENT }
        val modeName = prefs.getString("reading_mode", ReadingMode.AYAH_BY_AYAH.name) ?: ReadingMode.AYAH_BY_AYAH.name
        val mode = try { ReadingMode.valueOf(modeName) } catch (_: Exception) { ReadingMode.AYAH_BY_AYAH }
        val showTashkeel = prefs.getBoolean("show_tashkeel", true)
        val tafsirName = prefs.getString("selected_tafsir", TafsirSource.JALALAYN.name) ?: TafsirSource.JALALAYN.name
        val tafsir = try { TafsirSource.valueOf(tafsirName) } catch (_: Exception) { TafsirSource.JALALAYN }
        val autoScroll = prefs.getBoolean("auto_scroll_audio", true)

        val reciterName = prefs.getString("selected_reciter", com.example.data.model.QuranReciter.ALAFASY.name) ?: com.example.data.model.QuranReciter.ALAFASY.name
        val reciter = try { com.example.data.model.QuranReciter.valueOf(reciterName) } catch (_: Exception) { com.example.data.model.QuranReciter.ALAFASY }
        val repeatName = prefs.getString("repeat_mode", com.example.data.model.RepeatMode.ONCE.name) ?: com.example.data.model.RepeatMode.ONCE.name
        val repeat = try { com.example.data.model.RepeatMode.valueOf(repeatName) } catch (_: Exception) { com.example.data.model.RepeatMode.ONCE }

        val prevThemeName = prefs.getString("previous_day_theme", ReadingTheme.PARCHMENT.name) ?: ReadingTheme.PARCHMENT.name
        val prevTheme = try { ReadingTheme.valueOf(prevThemeName) } catch (_: Exception) { ReadingTheme.PARCHMENT }
        val autoNightName = prefs.getString("auto_night_mode", com.example.data.model.AutoNightModeOption.AUTO_TIME.name) ?: com.example.data.model.AutoNightModeOption.AUTO_TIME.name
        val autoNight = try { com.example.data.model.AutoNightModeOption.valueOf(autoNightName) } catch (_: Exception) { com.example.data.model.AutoNightModeOption.AUTO_TIME }
        val eyeComfort = prefs.getBoolean("eye_comfort_filter", false)

        return ReadingSettings(
            quranFontSize = qSize,
            tafsirFontSize = tSize,
            theme = theme,
            previousDayTheme = prevTheme,
            readingMode = mode,
            showTashkeel = showTashkeel,
            selectedTafsir = tafsir,
            selectedReciter = reciter,
            repeatMode = repeat,
            autoScrollAudio = autoScroll,
            autoNightMode = autoNight,
            eyeComfortWarmFilter = eyeComfort
        )
    }

    fun updateSettings(newSettings: ReadingSettings) {
        _settingsFlow.value = newSettings
        prefs.edit()
            .putFloat("quran_font_size", newSettings.quranFontSize)
            .putFloat("tafsir_font_size", newSettings.tafsirFontSize)
            .putString("reading_theme", newSettings.theme.name)
            .putString("previous_day_theme", newSettings.previousDayTheme.name)
            .putString("reading_mode", newSettings.readingMode.name)
            .putBoolean("show_tashkeel", newSettings.showTashkeel)
            .putString("selected_tafsir", newSettings.selectedTafsir.name)
            .putString("selected_reciter", newSettings.selectedReciter.name)
            .putString("repeat_mode", newSettings.repeatMode.name)
            .putBoolean("auto_scroll_audio", newSettings.autoScrollAudio)
            .putString("auto_night_mode", newSettings.autoNightMode.name)
            .putBoolean("eye_comfort_filter", newSettings.eyeComfortWarmFilter)
            .apply()
    }

    fun loadDailyWirdReminder(): DailyWirdReminder {
        val enabled = prefs.getBoolean("wird_reminder_enabled", false)
        val hour = prefs.getInt("wird_reminder_hour", 20)
        val minute = prefs.getInt("wird_reminder_minute", 30)
        val goalName = prefs.getString("wird_reminder_goal", WirdGoalType.ONE_PAGE.name) ?: WirdGoalType.ONE_PAGE.name
        val goal = try { WirdGoalType.valueOf(goalName) } catch (_: Exception) { WirdGoalType.ONE_PAGE }
        val message = prefs.getString("wird_reminder_message", "حان موعد وردك القرآني اليومي.. نوّر قلبك ويومك بآيات من الذكر الحكيم 📖")
            ?: "حان موعد وردك القرآني اليومي.. نوّر قلبك ويومك بآيات من الذكر الحكيم 📖"
        val fridayKahf = prefs.getBoolean("wird_reminder_friday_kahf", true)
        val sound = prefs.getBoolean("wird_reminder_sound", true)
        val vibrate = prefs.getBoolean("wird_reminder_vibrate", true)

        return DailyWirdReminder(
            isEnabled = enabled,
            hour = hour,
            minute = minute,
            wirdGoalType = goal,
            customMessage = message,
            remindOnFridaySurahKahf = fridayKahf,
            soundEnabled = sound,
            vibrateEnabled = vibrate
        )
    }

    fun updateDailyWirdReminder(newReminder: DailyWirdReminder) {
        _dailyWirdFlow.value = newReminder
        prefs.edit()
            .putBoolean("wird_reminder_enabled", newReminder.isEnabled)
            .putInt("wird_reminder_hour", newReminder.hour)
            .putInt("wird_reminder_minute", newReminder.minute)
            .putString("wird_reminder_goal", newReminder.wirdGoalType.name)
            .putString("wird_reminder_message", newReminder.customMessage)
            .putBoolean("wird_reminder_friday_kahf", newReminder.remindOnFridaySurahKahf)
            .putBoolean("wird_reminder_sound", newReminder.soundEnabled)
            .putBoolean("wird_reminder_vibrate", newReminder.vibrateEnabled)
            .apply()

        if (newReminder.isEnabled) {
            DailyWirdScheduler.schedule(context, newReminder)
        } else {
            DailyWirdScheduler.cancel(context)
        }
    }

    /**
     * Loads the complete authentic Quranic text and authentic Tafsirs (Jalalayn & Muyassar)
     * from the locally bundled assets (offline-ready for all 114 surahs and 6236 ayahs).
     */
    private fun loadSurahFromAssets(surahNumber: Int): List<Ayah>? {
        return try {
            if (cachedQuranJson == null) {
                context.assets.open("quran_uthmani.json").bufferedReader().use {
                    cachedQuranJson = org.json.JSONObject(it.readText())
                }
            }
            val surahAyahsArray = cachedQuranJson?.optJSONArray(surahNumber.toString()) ?: return null

            if (cachedJalalaynJson == null) {
                try {
                    context.assets.open("tafsir_jalalayn.json").bufferedReader().use {
                        cachedJalalaynJson = org.json.JSONObject(it.readText())
                    }
                } catch (_: Exception) {}
            }

            if (cachedMuyassarJson == null) {
                try {
                    context.assets.open("tafsir_muyassar.json").bufferedReader().use {
                        cachedMuyassarJson = org.json.JSONObject(it.readText())
                    }
                } catch (_: Exception) {}
            }

            val surahJalalaynObj = cachedJalalaynJson?.optJSONObject(surahNumber.toString())
            val surahMuyassarObj = cachedMuyassarJson?.optJSONObject(surahNumber.toString())

            val ayahs = mutableListOf<Ayah>()
            for (i in 0 until surahAyahsArray.length()) {
                val aObj = surahAyahsArray.getJSONObject(i)
                val ayahNum = aObj.getInt("n")
                val textWithTashkeel = aObj.getString("t")
                val juz = aObj.optInt("j", 1)
                val page = aObj.optInt("p", 1)

                val jText = surahJalalaynObj?.optString(ayahNum.toString()) ?: ""
                val mText = surahMuyassarObj?.optString(ayahNum.toString()) ?: ""

                ayahs.add(
                    Ayah(
                        surahNumber = surahNumber,
                        ayahNumber = ayahNum,
                        textWithTashkeel = textWithTashkeel,
                        textClean = QuranStaticData.normalizeArabic(textWithTashkeel),
                        tafsirJalalayn = jText.ifEmpty { "تفسير الجلالين للآية الكريمة." },
                        tafsirMuyassar = mText.ifEmpty { "التفسير الميسر للآية الكريمة." },
                        juzNumber = juz,
                        pageNumber = page
                    )
                )
            }
            ayahs
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Retrieves verses with Tafsir Al-Jalalayn & Tafsir Al-Muyassar for a given surah.
     * 1. Checks in-memory cache
     * 2. Checks Room SQLite database (if fully cached with expected count)
     * 3. Loads from pre-bundled authentic offline assets (instantaneous, offline for all 114 surahs)
     * 4. If asset fails, attempts network fetch
     * 5. Fallback if completely unavailable
     */
    suspend fun getSurahAyahs(surahNumber: Int): List<Ayah> = withContext(Dispatchers.IO) {
        val surahInfo = QuranStaticData.getSurah(surahNumber)
        val expectedCount = surahInfo?.ayahCount ?: 7

        // 1. Return if in memory cache and complete
        val cached = surahCache[surahNumber]
        if (cached != null && cached.size >= expectedCount) {
            return@withContext cached
        }

        // 2. Load from pre-bundled authentic assets (100% complete offline Quran & Tafsirs)
        val assetAyahs = loadSurahFromAssets(surahNumber)
        if (!assetAyahs.isNullOrEmpty() && assetAyahs.size >= expectedCount) {
            val entities = assetAyahs.map { QuranAyahEntity.fromAyah(it) }
            quranAyahDao.insertAyahs(entities)
            surahCache[surahNumber] = assetAyahs
            return@withContext assetAyahs
        }

        // 3. Check Room Database (only if verified authentic and complete)
        val roomEntities = quranAyahDao.getAyahsForSurahSync(surahNumber)
        if (roomEntities.isNotEmpty() && roomEntities.size >= expectedCount && !roomEntities.first().textWithTashkeel.startsWith("﴿سورة")) {
            val ayahs = roomEntities.map { it.toAyah() }
            surahCache[surahNumber] = ayahs
            return@withContext ayahs
        }

        // 4. Attempt remote load and persist to Room
        val remoteAyahs = apiClient.fetchSurahAyahs(surahNumber)
        if (!remoteAyahs.isNullOrEmpty()) {
            val entities = remoteAyahs.map { QuranAyahEntity.fromAyah(it) }
            quranAyahDao.insertAyahs(entities)
            surahCache[surahNumber] = remoteAyahs
            return@withContext remoteAyahs
        }

        // 5. If Room has authentic verses
        if (roomEntities.isNotEmpty() && !roomEntities.first().textWithTashkeel.startsWith("﴿سورة")) {
            val ayahs = roomEntities.map { it.toAyah() }
            surahCache[surahNumber] = ayahs
            return@withContext ayahs
        }

        return@withContext emptyList()
    }

    /**
     * Performs indexed fast search across Quran text and both Tafsirs using Room SQL queries.
     */
    suspend fun search(query: String, filter: SearchFilter): List<SearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        // Ensure database has all 6,236 verses indexed for comprehensive search across all 114 surahs
        if (quranAyahDao.getTotalAyahsCount() < 6236) {
            ensureDatabasePopulated()
        }

        val normalizedQuery = QuranStaticData.normalizeArabic(trimmed)

        // 1. Query Room Database directly using diacritic-free normalized text
        val roomResults = when (filter) {
            SearchFilter.QURAN_ONLY -> quranAyahDao.searchQuran(normalizedQuery)
            SearchFilter.JALALAYN_ONLY -> quranAyahDao.searchJalalayn(normalizedQuery)
            SearchFilter.MUYASSAR_ONLY -> quranAyahDao.searchMuyassar(normalizedQuery)
            SearchFilter.ALL -> quranAyahDao.searchAll(normalizedQuery)
        }

        val resultsMap = mutableMapOf<Pair<Int, Int>, SearchResult>()

        for (entity in roomResults) {
            val surahInfo = QuranStaticData.getSurah(entity.surahNumber) ?: continue
            val key = Pair(entity.surahNumber, entity.ayahNumber)

            val matchType = when {
                entity.textClean.contains(normalizedQuery, ignoreCase = true) ||
                QuranStaticData.normalizeArabic(entity.textWithTashkeel).contains(normalizedQuery, ignoreCase = true) -> MatchType.QURAN
                entity.tafsirJalalaynClean.contains(normalizedQuery, ignoreCase = true) ||
                QuranStaticData.normalizeArabic(entity.tafsirJalalayn).contains(normalizedQuery, ignoreCase = true) -> MatchType.JALALAYN
                else -> MatchType.MUYASSAR
            }

            val snippet = when (matchType) {
                MatchType.QURAN -> entity.textWithTashkeel
                MatchType.JALALAYN -> entity.tafsirJalalayn
                MatchType.MUYASSAR -> entity.tafsirMuyassar
            }

            resultsMap[key] = SearchResult(
                surahNumber = entity.surahNumber,
                surahName = surahInfo.nameArabic,
                ayahNumber = entity.ayahNumber,
                ayahText = entity.textWithTashkeel,
                snippet = snippet,
                matchType = matchType,
                highlightQuery = trimmed
            )
        }

        // 2. Supplement with in-memory cache if any missing
        for ((surahNum, ayahs) in surahCache) {
            val surahInfo = QuranStaticData.getSurah(surahNum) ?: continue
            for (ayah in ayahs) {
                val key = Pair(surahNum, ayah.ayahNumber)
                if (resultsMap.containsKey(key)) continue

                if (filter == SearchFilter.ALL || filter == SearchFilter.QURAN_ONLY) {
                    val norm = QuranStaticData.normalizeArabic(ayah.textWithTashkeel)
                    if (norm.contains(normalizedQuery, ignoreCase = true)) {
                        resultsMap[key] = SearchResult(
                            surahNumber = surahNum,
                            surahName = surahInfo.nameArabic,
                            ayahNumber = ayah.ayahNumber,
                            ayahText = ayah.textWithTashkeel,
                            snippet = ayah.textWithTashkeel,
                            matchType = MatchType.QURAN,
                            highlightQuery = trimmed
                        )
                        continue
                    }
                }
                if (filter == SearchFilter.ALL || filter == SearchFilter.JALALAYN_ONLY) {
                    if (QuranStaticData.normalizeArabic(ayah.tafsirJalalayn).contains(normalizedQuery, ignoreCase = true)) {
                        resultsMap[key] = SearchResult(
                            surahNumber = surahNum,
                            surahName = surahInfo.nameArabic,
                            ayahNumber = ayah.ayahNumber,
                            ayahText = ayah.textWithTashkeel,
                            snippet = ayah.tafsirJalalayn,
                            matchType = MatchType.JALALAYN,
                            highlightQuery = trimmed
                        )
                        continue
                    }
                }
                if (filter == SearchFilter.ALL || filter == SearchFilter.MUYASSAR_ONLY) {
                    if (QuranStaticData.normalizeArabic(ayah.tafsirMuyassar).contains(normalizedQuery, ignoreCase = true)) {
                        resultsMap[key] = SearchResult(
                            surahNumber = surahNum,
                            surahName = surahInfo.nameArabic,
                            ayahNumber = ayah.ayahNumber,
                            ayahText = ayah.textWithTashkeel,
                            snippet = ayah.tafsirMuyassar,
                            matchType = MatchType.MUYASSAR,
                            highlightQuery = trimmed
                        )
                    }
                }
            }
        }

        return@withContext resultsMap.values.toList()
    }

    // Room Database Stats & Caching Helpers
    fun getStoredAyahsCountFlow(): Flow<Int> = quranAyahDao.getTotalAyahsCountFlow()

    suspend fun getStoredAyahsCount(): Int = withContext(Dispatchers.IO) {
        quranAyahDao.getTotalAyahsCount()
    }

    suspend fun isSurahCachedInRoom(surahNumber: Int): Boolean = withContext(Dispatchers.IO) {
        val count = quranAyahDao.getAyahCountForSurah(surahNumber)
        val surah = QuranStaticData.getSurah(surahNumber)
        count >= (surah?.ayahCount ?: 1)
    }

    suspend fun cacheSurahInRoom(surahNumber: Int): Boolean = withContext(Dispatchers.IO) {
        val ayahs = apiClient.fetchSurahAyahs(surahNumber)
        if (!ayahs.isNullOrEmpty()) {
            quranAyahDao.insertAyahs(ayahs.map { QuranAyahEntity.fromAyah(it) })
            surahCache[surahNumber] = ayahs
            true
        } else {
            false
        }
    }

    // Bookmarks operations
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    fun isBookmarked(surahNumber: Int, ayahNumber: Int): Flow<Boolean> =
        bookmarkDao.isBookmarkedFlow(surahNumber, ayahNumber)

    suspend fun toggleBookmark(surahNumber: Int, ayahNumber: Int, surahName: String, ayahText: String, note: String = "", tag: String = "عام") {
        val existing = bookmarkDao.getBookmarkForAyah(surahNumber, ayahNumber)
        if (existing != null) {
            bookmarkDao.deleteBookmark(existing)
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    surahName = surahName,
                    ayahText = ayahText,
                    note = note,
                    category = tag
                )
            )
        }
    }

    suspend fun saveBookmarkWithDetails(surahNumber: Int, ayahNumber: Int, surahName: String, ayahText: String, note: String, tag: String) {
        val existing = bookmarkDao.getBookmarkForAyah(surahNumber, ayahNumber)
        if (existing != null) {
            bookmarkDao.updateBookmark(
                existing.copy(note = note, category = tag, timestamp = System.currentTimeMillis())
            )
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    surahName = surahName,
                    ayahText = ayahText,
                    note = note,
                    category = tag
                )
            )
        }
    }

    suspend fun deleteBookmarkById(id: Long) = bookmarkDao.deleteById(id)

    // Reading progress (Per-Surah bookmarks & Global last read)
    fun getLastRead(): Flow<ReadingProgressEntity?> = readingProgressDao.getLastRead()

    fun getProgressForSurah(surahNumber: Int): Flow<ReadingProgressEntity?> =
        readingProgressDao.getProgressForSurah(surahNumber)

    suspend fun getProgressForSurahSync(surahNumber: Int): ReadingProgressEntity? =
        readingProgressDao.getProgressForSurahSync(surahNumber)

    fun getAllReadingProgress(): Flow<List<ReadingProgressEntity>> =
        readingProgressDao.getAllReadingProgress()

    suspend fun saveLastRead(surahNumber: Int, ayahNumber: Int, surahName: String, ayahTextPreview: String) {
        readingProgressDao.setLastRead(
            ReadingProgressEntity(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName,
                ayahTextPreview = ayahTextPreview,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearProgressForSurah(surahNumber: Int) {
        readingProgressDao.deleteProgressForSurah(surahNumber)
    }
}
