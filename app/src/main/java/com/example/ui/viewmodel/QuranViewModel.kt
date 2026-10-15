package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerState
import com.example.audio.QuranAudioPlayer
import com.example.data.local.BookmarkEntity
import com.example.data.local.ReadingProgressEntity
import com.example.data.model.AutoNightModeOption
import com.example.data.model.Ayah
import com.example.data.model.DailyWirdReminder
import com.example.data.model.ReadingSettings
import com.example.data.model.ReadingTheme
import com.example.data.model.SearchResult
import com.example.data.model.SurahInfo
import com.example.data.model.TafsirSource
import com.example.data.repository.QuranRepository
import com.example.data.repository.QuranStaticData
import com.example.data.repository.SearchFilter
import com.example.reminder.DailyWirdScheduler
import com.example.util.AutoNightModeSensorManager
import com.example.prayer.AdhanAlertMode
import com.example.prayer.AdhanMuezzin
import com.example.prayer.AdhanPlayerState
import com.example.prayer.CalculationMethod
import com.example.prayer.CityPreset
import com.example.prayer.PrayerSchedule
import com.example.prayer.PrayerTimesRepository
import com.example.prayer.PrayerType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuranRepository(application)
    val audioPlayer = QuranAudioPlayer(application)
    val autoNightSensorManager = AutoNightModeSensorManager(application)
    val prayerRepository = PrayerTimesRepository(application)

    val settings: StateFlow<ReadingSettings> = repository.settingsFlow
    val dailyWirdReminder: StateFlow<DailyWirdReminder> = repository.dailyWirdFlow
    val ambientLux: StateFlow<Float?> = autoNightSensorManager.ambientLux

    val prayerSchedule: StateFlow<PrayerSchedule> = prayerRepository.scheduleFlow
    val selectedCity: StateFlow<CityPreset> = prayerRepository.selectedCityFlow
    val selectedCalculationMethod: StateFlow<CalculationMethod> = prayerRepository.selectedMethodFlow
    val selectedMuezzin: StateFlow<AdhanMuezzin> = prayerRepository.selectedMuezzinFlow
    val adhanPlayerState: StateFlow<AdhanPlayerState> = prayerRepository.adhanPlayer.state

    val allSurahs: List<SurahInfo> = QuranStaticData.surahs

    private val _currentSurah = MutableStateFlow<SurahInfo?>(QuranStaticData.getSurah(1))
    val currentSurah: StateFlow<SurahInfo?> = _currentSurah.asStateFlow()

    private val _currentAyahs = MutableStateFlow<List<Ayah>>(emptyList())
    val currentAyahs: StateFlow<List<Ayah>> = _currentAyahs.asStateFlow()

    private val _isLoadingAyahs = MutableStateFlow(false)
    val isLoadingAyahs: StateFlow<Boolean> = _isLoadingAyahs.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastRead: StateFlow<ReadingProgressEntity?> = repository.getLastRead()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allReadingProgress: StateFlow<List<ReadingProgressEntity>> = repository.getAllReadingProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customMuezzin: StateFlow<com.example.prayer.CustomMuezzin> = prayerRepository.customMuezzinFlow

    val activeAdhanOverlayPrayer = MutableStateFlow<com.example.prayer.PrayerType?>(null)

    // Search state
    val searchQuery = MutableStateFlow("")
    val searchFilter = MutableStateFlow(SearchFilter.ALL)
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    private var searchJob: Job? = null

    // Tafsir dialog/bottomsheet state
    private val _selectedAyahForTafsir = MutableStateFlow<Ayah?>(null)
    val selectedAyahForTafsir: StateFlow<Ayah?> = _selectedAyahForTafsir.asStateFlow()

    val selectedTafsirTab = MutableStateFlow(TafsirSource.JALALAYN)

    // Target ayah to scroll to
    val scrollTargetAyah = MutableStateFlow<Int?>(null)

    val audioPlayerState: StateFlow<AudioPlayerState> = audioPlayer.playerState

    init {
        loadSurah(1)

        audioPlayer.setReciter(settings.value.selectedReciter)
        audioPlayer.setRepeatMode(settings.value.repeatMode)

        audioPlayer.onAyahChanged = { surahNum, ayahNum ->
            if (settings.value.autoScrollAudio) {
                scrollTargetAyah.value = ayahNum
            }
        }

        // Start sensor listening if light sensor automation is requested
        if (settings.value.autoNightMode == AutoNightModeOption.AUTO_LIGHT_SENSOR) {
            autoNightSensorManager.startListening()
        }

        // Observe low light changes from sensor
        viewModelScope.launch {
            autoNightSensorManager.isLowLightDetected.collect { isLowLight ->
                if (settings.value.autoNightMode == AutoNightModeOption.AUTO_LIGHT_SENSOR) {
                    val current = settings.value
                    if (isLowLight && !current.theme.isDark) {
                        updateSettings(current.copy(theme = ReadingTheme.NIGHT, previousDayTheme = current.theme))
                    } else if (!isLowLight && current.theme.isDark) {
                        val dayTheme = if (!current.previousDayTheme.isDark) current.previousDayTheme else ReadingTheme.PARCHMENT
                        updateSettings(current.copy(theme = dayTheme))
                    }
                }
            }
        }
    }

    /**
     * Checks auto night mode conditions (clock time or system dark theme).
     */
    fun checkAutoNightMode(isSystemInDark: Boolean) {
        val current = settings.value
        val shouldBeNight = when (current.autoNightMode) {
            AutoNightModeOption.DISABLED -> return
            AutoNightModeOption.AUTO_TIME -> AutoNightModeSensorManager.isNightTimeByClock()
            AutoNightModeOption.AUTO_LIGHT_SENSOR -> {
                autoNightSensorManager.isLowLightDetected.value || AutoNightModeSensorManager.isNightTimeByClock()
            }
            AutoNightModeOption.FOLLOW_SYSTEM -> isSystemInDark
        }

        if (shouldBeNight && !current.theme.isDark) {
            updateSettings(current.copy(theme = ReadingTheme.NIGHT, previousDayTheme = current.theme))
        } else if (!shouldBeNight && current.theme.isDark && current.autoNightMode == AutoNightModeOption.FOLLOW_SYSTEM) {
            val dayTheme = if (!current.previousDayTheme.isDark) current.previousDayTheme else ReadingTheme.PARCHMENT
            updateSettings(current.copy(theme = dayTheme))
        }
    }

    /**
     * Quick toggle button action for night reading mode.
     * Instantly flips between night OLED/dark mode and the daytime/cream theme.
     * Returns true if night mode is now active, false if day mode is active.
     */
    fun toggleQuickNightMode(): Boolean {
        val current = settings.value
        return if (current.theme.isDark) {
            // Restore daytime theme
            val dayTheme = if (!current.previousDayTheme.isDark) current.previousDayTheme else ReadingTheme.PARCHMENT
            updateSettings(current.copy(theme = dayTheme))
            false
        } else {
            // Activate night mode, remembering day theme
            updateSettings(current.copy(theme = ReadingTheme.NIGHT, previousDayTheme = current.theme))
            true
        }
    }

    /**
     * Toggles warm eye comfort filter to reduce blue light glare in dim settings.
     */
    fun toggleEyeComfortWarmFilter() {
        val current = settings.value
        updateSettings(current.copy(eyeComfortWarmFilter = !current.eyeComfortWarmFilter))
    }

    fun setAutoNightModeOption(option: AutoNightModeOption) {
        val current = settings.value
        if (option == AutoNightModeOption.AUTO_LIGHT_SENSOR) {
            autoNightSensorManager.startListening()
        } else {
            autoNightSensorManager.stopListening()
        }
        updateSettings(current.copy(autoNightMode = option))
    }

    /**
     * Updates the daily wird reminder schedule and persists to repository.
     */
    fun updateDailyWirdReminder(newReminder: DailyWirdReminder) {
        repository.updateDailyWirdReminder(newReminder)
    }

    /**
     * Toggles the daily wird reminder on or off. Returns the new state.
     */
    fun toggleDailyWirdReminder(): Boolean {
        val current = dailyWirdReminder.value
        val updated = current.copy(isEnabled = !current.isEnabled)
        repository.updateDailyWirdReminder(updated)
        return updated.isEnabled
    }

    /**
     * Dispatches an immediate test notification so the user can verify the alarm sound and look.
     */
    fun testDailyWirdNotification() {
        DailyWirdScheduler.showNotification(getApplication(), dailyWirdReminder.value, isTest = true)
    }

    fun loadSurah(surahNumber: Int, targetAyahNumber: Int? = null) {
        val surahInfo = QuranStaticData.getSurah(surahNumber) ?: return
        _currentSurah.value = surahInfo

        viewModelScope.launch {
            val resolvedAyah = targetAyahNumber
                ?: repository.getProgressForSurahSync(surahNumber)?.ayahNumber
                ?: 1

            scrollTargetAyah.value = resolvedAyah
            _isLoadingAyahs.value = true
            val ayahs = repository.getSurahAyahs(surahNumber)
            _currentAyahs.value = ayahs
            _isLoadingAyahs.value = false

            // Save reading progress for this specific Surah
            if (ayahs.isNotEmpty()) {
                val preview = ayahs.firstOrNull { it.ayahNumber == resolvedAyah }?.textWithTashkeel
                    ?: ayahs.first().textWithTashkeel
                repository.saveLastRead(
                    surahNumber = surahNumber,
                    ayahNumber = resolvedAyah,
                    surahName = surahInfo.nameArabic,
                    ayahTextPreview = preview
                )
            }
        }
    }

    fun saveSurahStopAyah(surahNumber: Int, ayahNumber: Int, surahName: String, ayahText: String) {
        viewModelScope.launch {
            repository.saveLastRead(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName,
                ayahTextPreview = ayahText
            )
        }
    }

    fun updateSettings(newSettings: ReadingSettings) {
        if (newSettings.selectedReciter != settings.value.selectedReciter) {
            audioPlayer.setReciter(newSettings.selectedReciter)
        }
        if (newSettings.repeatMode != settings.value.repeatMode) {
            audioPlayer.setRepeatMode(newSettings.repeatMode)
        }
        repository.updateSettings(newSettings)
    }

    fun changeReciter(reciter: com.example.data.model.QuranReciter) {
        audioPlayer.setReciter(reciter)
        updateSettings(settings.value.copy(selectedReciter = reciter))
    }

    fun cycleRepeatMode() {
        val nextMode = audioPlayer.cycleRepeatMode()
        updateSettings(settings.value.copy(repeatMode = nextMode))
    }

    fun setRepeatMode(repeatMode: com.example.data.model.RepeatMode) {
        audioPlayer.setRepeatMode(repeatMode)
        updateSettings(settings.value.copy(repeatMode = repeatMode))
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
        triggerSearch(newQuery, searchFilter.value)
    }

    fun onSearchFilterChanged(newFilter: SearchFilter) {
        searchFilter.value = newFilter
        triggerSearch(searchQuery.value, newFilter)
    }

    private fun triggerSearch(query: String, filter: SearchFilter) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(250) // debounce
            _isSearching.value = true
            val results = repository.search(query, filter)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun toggleBookmark(ayah: Ayah, surahName: String) {
        viewModelScope.launch {
            repository.toggleBookmark(
                surahNumber = ayah.surahNumber,
                ayahNumber = ayah.ayahNumber,
                surahName = surahName,
                ayahText = ayah.textWithTashkeel
            )
        }
    }

    fun saveBookmarkWithDetails(ayah: Ayah, surahName: String, note: String, tag: String) {
        viewModelScope.launch {
            repository.saveBookmarkWithDetails(
                surahNumber = ayah.surahNumber,
                ayahNumber = ayah.ayahNumber,
                surahName = surahName,
                ayahText = ayah.textWithTashkeel,
                note = note,
                tag = tag
            )
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmarkById(id)
        }
    }

    fun openTafsir(ayah: Ayah) {
        _selectedAyahForTafsir.value = ayah
        selectedTafsirTab.value = settings.value.selectedTafsir
    }

    fun closeTafsir() {
        _selectedAyahForTafsir.value = null
    }

    fun playAyah(ayah: Ayah, surahInfo: SurahInfo) {
        audioPlayer.playAyah(
            surahNumber = ayah.surahNumber,
            ayahNumber = ayah.ayahNumber,
            surahName = surahInfo.nameArabic,
            totalAyahs = surahInfo.ayahCount,
            reciter = settings.value.selectedReciter,
            repeatMode = settings.value.repeatMode
        )
    }

    // ================= PRAYER TIMES & ADHAN METHODS =================
    fun selectCity(city: CityPreset) {
        prayerRepository.setCity(city)
    }

    fun setCustomLocation(cityName: String, countryName: String, lat: Double, lng: Double) {
        prayerRepository.setCoordinates(cityName, countryName, lat, lng)
    }

    fun selectCalculationMethod(method: CalculationMethod) {
        prayerRepository.setCalculationMethod(method)
    }

    fun selectMuezzin(muezzin: AdhanMuezzin) {
        prayerRepository.setSelectedMuezzin(muezzin)
    }

    fun togglePrayerAlert(prayer: PrayerType): AdhanAlertMode {
        return prayerRepository.cycleAlertMode(prayer)
    }

    fun playMuezzinPreview(muezzin: AdhanMuezzin) {
        prayerRepository.adhanPlayer.playMuezzin(muezzin)
    }

    fun stopAdhanPreview() {
        prayerRepository.adhanPlayer.stop()
    }

    fun uploadCustomMuezzin(uri: android.net.Uri, displayName: String): Boolean {
        return prayerRepository.setCustomMuezzinFromUri(uri, displayName)
    }

    fun selectCustomMuezzin() {
        prayerRepository.useCustomMuezzin(true)
    }

    fun clearCustomMuezzin() {
        prayerRepository.clearCustomMuezzin()
    }

    fun playCustomMuezzinPreview() {
        val custom = customMuezzin.value
        if (custom.filePath.isNotEmpty()) {
            prayerRepository.adhanPlayer.playCustomAudio(custom.filePath, custom.name)
        }
    }

    fun openAdhanOverlay(prayerType: PrayerType) {
        activeAdhanOverlayPrayer.value = prayerType
        val custom = customMuezzin.value
        if (custom.isEnabled && custom.filePath.isNotEmpty()) {
            prayerRepository.adhanPlayer.playCustomAudio(custom.filePath, custom.name)
        } else {
            prayerRepository.adhanPlayer.playMuezzin(selectedMuezzin.value)
        }
    }

    fun closeAdhanOverlay() {
        activeAdhanOverlayPrayer.value = null
        prayerRepository.adhanPlayer.stop()
    }

    override fun onCleared() {
        super.onCleared()
        autoNightSensorManager.stopListening()
        audioPlayer.release()
        prayerRepository.adhanPlayer.release()
    }
}
