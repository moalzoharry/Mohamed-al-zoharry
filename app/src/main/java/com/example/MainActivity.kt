package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.adhkar.AdhkarCategory
import com.example.ui.components.AboutAppBottomSheet
import com.example.ui.components.ReadingSettingsBottomSheet
import com.example.ui.screens.AdhanLiveOverlayScreen
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.AdvancedSearchScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DuaaKhatmScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MustajabDuaaScreen
import com.example.ui.screens.NamesOfAllahScreen
import com.example.ui.screens.MorningEveningWirdScreen
import com.example.ui.screens.ReverentRecitationsScreen
import com.example.ui.screens.ShaarawyTafsirScreen
import com.example.ui.screens.CustomAdhansScreen
import com.example.ui.screens.FridaySermonScreen
import android.content.Context
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.QiblaCompassScreen
import com.example.ui.screens.SurahReaderScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.IslamicHeritageScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.HijriCalendarScreen
import com.example.ui.components.DonationSadaqahSheet
import com.example.ui.components.AppUpdateDialog
import com.example.update.AppUpdateManager
import com.example.ui.theme.QuranAppTheme
import com.example.ui.viewmodel.QuranViewModel

sealed interface QuranScreen {
    object Splash : QuranScreen
    object Onboarding : QuranScreen
    object Home : QuranScreen
    data class Reader(val surahNumber: Int, val initialAyah: Int = 1) : QuranScreen
    object Search : QuranScreen
    object Bookmarks : QuranScreen
    object DuaaKhatm : QuranScreen
    object PrayerTimes : QuranScreen
    object Qibla : QuranScreen
    data class Adhkar(val category: AdhkarCategory = AdhkarCategory.MORNING) : QuranScreen
    object Tasbih : QuranScreen
    object MustajabDuaa : QuranScreen
    object NamesOfAllah : QuranScreen
    data class MorningEveningWird(val isEvening: Boolean = false) : QuranScreen
    object ReverentRecitations : QuranScreen
    object ShaarawyTafsir : QuranScreen
    object FridaySermon : QuranScreen
    object CustomAdhans : QuranScreen
    object HijriCalendar : QuranScreen
    object IslamicHeritage : QuranScreen
}

class MainActivity : ComponentActivity() {

    private val viewModel: QuranViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule 10:00 AM and 5:00 PM Wird Alarms
        com.example.reminder.WirdCompanionScheduler.scheduleDailyWirds(this)

        setContent {
            val settings by viewModel.settings.collectAsState()
            val isDarkTheme = settings.theme.isDark
            val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()

            androidx.compose.runtime.LaunchedEffect(systemInDark, settings.autoNightMode) {
                viewModel.checkAutoNightMode(isSystemInDark = systemInDark)
            }

            val initialStack = remember {
                if (intent?.action == com.example.reminder.WirdCompanionScheduler.ACTION_OPEN_WIRD) {
                    val isEve = intent?.getBooleanExtra(com.example.reminder.WirdCompanionScheduler.EXTRA_IS_EVENING, false) ?: false
                    listOf(QuranScreen.Home, QuranScreen.MorningEveningWird(isEve))
                } else {
                    listOf<QuranScreen>(QuranScreen.Splash)
                }
            }
            var screenStack by remember { mutableStateOf(initialStack) }
            val currentScreen = screenStack.lastOrNull() ?: QuranScreen.Home

            var showSettingsSheet by remember { mutableStateOf(false) }
            val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            var showAboutSheet by remember { mutableStateOf(false) }
            val aboutSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            var showDonationSheet by remember { mutableStateOf(false) }
            val updateInfo by AppUpdateManager.updateState.collectAsState()

            androidx.compose.runtime.LaunchedEffect(Unit) {
                AppUpdateManager.checkForUpdates(this@MainActivity, forceShowSimulation = false)
            }

            val activeAdhanOverlayPrayer by viewModel.activeAdhanOverlayPrayer.collectAsState()
            val prayerSchedule by viewModel.prayerSchedule.collectAsState()
            val selectedMuezzin by viewModel.selectedMuezzin.collectAsState()
            val customMuezzin by viewModel.customMuezzin.collectAsState()
            val adhanPlayerState by viewModel.adhanPlayerState.collectAsState()

            // RTL support for authentic Arabic Quranic experience
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                QuranAppTheme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                    ) {
                        BackHandler(enabled = screenStack.size > 1) {
                            screenStack = screenStack.dropLast(1)
                        }

                        val onboardingPrefs = remember { getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE) }

                        when (val screen = currentScreen) {
                            is QuranScreen.Splash -> {
                                SplashScreen(
                                    onSplashFinished = {
                                        val hasSeen = onboardingPrefs.getBoolean("has_seen_onboarding", false)
                                        if (!hasSeen) {
                                            screenStack = listOf(QuranScreen.Onboarding)
                                        } else {
                                            screenStack = listOf(QuranScreen.Home)
                                        }
                                    }
                                )
                            }
                            is QuranScreen.Onboarding -> {
                                OnboardingScreen(
                                    viewModel = viewModel,
                                    onFinish = {
                                        onboardingPrefs.edit().putBoolean("has_seen_onboarding", true).apply()
                                        screenStack = listOf(QuranScreen.Home)
                                    }
                                )
                            }
                            is QuranScreen.Home -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToReader = { surahNum, ayahNum ->
                                        viewModel.loadSurah(surahNum, ayahNum)
                                        screenStack = screenStack + QuranScreen.Reader(surahNum, ayahNum)
                                    },
                                    onNavigateToSearch = {
                                        screenStack = screenStack + QuranScreen.Search
                                    },
                                    onNavigateToBookmarks = {
                                        screenStack = screenStack + QuranScreen.Bookmarks
                                    },
                                    onNavigateToDuaaKhatm = {
                                        screenStack = screenStack + QuranScreen.DuaaKhatm
                                    },
                                    onNavigateToPrayerTimes = {
                                        screenStack = screenStack + QuranScreen.PrayerTimes
                                    },
                                    onNavigateToQibla = {
                                        screenStack = screenStack + QuranScreen.Qibla
                                    },
                                    onNavigateToAdhkar = {
                                        screenStack = screenStack + QuranScreen.Adhkar()
                                    },
                                    onNavigateToTasbih = {
                                        screenStack = screenStack + QuranScreen.Tasbih
                                    },
                                    onNavigateToMustajabDuaa = {
                                        screenStack = screenStack + QuranScreen.MustajabDuaa
                                    },
                                    onNavigateToNamesOfAllah = {
                                        screenStack = screenStack + QuranScreen.NamesOfAllah
                                    },
                                    onNavigateToShaarawyTafsir = {
                                        screenStack = screenStack + QuranScreen.ShaarawyTafsir
                                    },
                                    onNavigateToMorningEveningWird = { isEvening ->
                                        screenStack = screenStack + QuranScreen.MorningEveningWird(isEvening)
                                    },
                                    onNavigateToReverentRecitations = {
                                        screenStack = screenStack + QuranScreen.ReverentRecitations
                                    },
                                    onNavigateToFridaySermon = {
                                        screenStack = screenStack + QuranScreen.FridaySermon
                                    },
                                    onNavigateToCustomAdhans = {
                                        screenStack = screenStack + QuranScreen.CustomAdhans
                                    },
                                    onNavigateToHijriCalendar = {
                                        screenStack = screenStack + QuranScreen.HijriCalendar
                                    },
                                    onNavigateToIslamicHeritage = {
                                        screenStack = screenStack + QuranScreen.IslamicHeritage
                                    },
                                    onOpenDonationSheet = {
                                        showDonationSheet = true
                                    },
                                    onCheckUpdates = {
                                        AppUpdateManager.checkForUpdates(this@MainActivity, forceShowSimulation = true)
                                    },
                                    onOpenSettings = {
                                        showSettingsSheet = true
                                    },
                                    onOpenAbout = {
                                        showAboutSheet = true
                                    }
                                )
                            }
                            is QuranScreen.Reader -> {
                                SurahReaderScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    },
                                    onNavigateToSearch = {
                                        screenStack = screenStack + QuranScreen.Search
                                    },
                                    onNavigateToDuaaKhatm = {
                                        screenStack = screenStack + QuranScreen.DuaaKhatm
                                    }
                                )
                            }
                            is QuranScreen.Search -> {
                                AdvancedSearchScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    },
                                    onNavigateToAyah = { surahNum, ayahNum ->
                                        viewModel.loadSurah(surahNum, ayahNum)
                                        screenStack = screenStack + QuranScreen.Reader(surahNum, ayahNum)
                                    }
                                )
                            }
                            is QuranScreen.Bookmarks -> {
                                BookmarksScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    },
                                    onNavigateToAyah = { surahNum, ayahNum ->
                                        viewModel.loadSurah(surahNum, ayahNum)
                                        screenStack = screenStack + QuranScreen.Reader(surahNum, ayahNum)
                                    }
                                )
                            }
                            is QuranScreen.DuaaKhatm -> {
                                DuaaKhatmScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                            is QuranScreen.PrayerTimes -> {
                                PrayerTimesScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    },
                                    onNavigateToQibla = {
                                        screenStack = screenStack + QuranScreen.Qibla
                                    },
                                    onNavigateToAdhkar = {
                                        screenStack = screenStack + QuranScreen.Adhkar(AdhkarCategory.AFTER_PRAYER)
                                    },
                                    onNavigateToCustomAdhans = {
                                        screenStack = screenStack + QuranScreen.CustomAdhans
                                    }
                                )
                            }
                            is QuranScreen.Qibla -> {
                                QiblaCompassScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                            is QuranScreen.Adhkar -> {
                                AdhkarScreen(
                                    initialCategory = screen.category,
                                    onBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                            is QuranScreen.Tasbih -> {
                                TasbihScreen(
                                    onBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                            is QuranScreen.MustajabDuaa -> {
                                MustajabDuaaScreen(
                                    onBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                            is QuranScreen.NamesOfAllah -> {
                                NamesOfAllahScreen(
                                    onNavigateBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                            is QuranScreen.ShaarawyTafsir -> {
                                ShaarawyTafsirScreen(
                                    onNavigateBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    },
                                    onNavigateToReader = { surahNum, ayahNum ->
                                        viewModel.loadSurah(surahNum, ayahNum)
                                        screenStack = screenStack + QuranScreen.Reader(surahNum, ayahNum)
                                    }
                                )
                            }
                            is QuranScreen.MorningEveningWird -> {
                                MorningEveningWirdScreen(
                                    isEvening = screen.isEvening,
                                    onCloseScreen = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    },
                                    onNavigateToReader = { surahNum, ayahNum ->
                                        viewModel.loadSurah(surahNum, ayahNum)
                                        screenStack = screenStack + QuranScreen.Reader(surahNum, ayahNum)
                                    }
                                )
                            }
                            is QuranScreen.ReverentRecitations -> {
                                ReverentRecitationsScreen(
                                    onNavigateBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                            is QuranScreen.FridaySermon -> {
                                FridaySermonScreen(
                                    onNavigateBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                            is QuranScreen.CustomAdhans -> {
                                CustomAdhansScreen(
                                    onNavigateBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                            is QuranScreen.HijriCalendar -> {
                                HijriCalendarScreen(
                                    onBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                            is QuranScreen.IslamicHeritage -> {
                                IslamicHeritageScreen(
                                    onNavigateBack = {
                                        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
                                    }
                                )
                            }
                        }

                        if (showSettingsSheet) {
                            ReadingSettingsBottomSheet(
                                settings = settings,
                                sheetState = settingsSheetState,
                                onDismissRequest = { showSettingsSheet = false },
                                onSaveSettings = { newSettings -> viewModel.updateSettings(newSettings) }
                            )
                        }

                        if (showAboutSheet) {
                            AboutAppBottomSheet(
                                sheetState = aboutSheetState,
                                onDismissRequest = { showAboutSheet = false },
                                onOpenOnboarding = {
                                    showAboutSheet = false
                                    screenStack = listOf(QuranScreen.Onboarding)
                                }
                            )
                        }

                        if (showDonationSheet) {
                            DonationSadaqahSheet(
                                onDismiss = { showDonationSheet = false }
                            )
                        }

                        updateInfo?.let { info ->
                            AppUpdateDialog(
                                updateInfo = info,
                                onDismiss = { AppUpdateManager.dismissUpdate() }
                            )
                        }

                        // Full-screen Islamic Adhan Overlay with Prayer Wallpaper & Actions
                        activeAdhanOverlayPrayer?.let { prayerType ->
                            AdhanLiveOverlayScreen(
                                prayerType = prayerType,
                                schedule = prayerSchedule,
                                muezzinName = if (customMuezzin.isEnabled && customMuezzin.filePath.isNotEmpty()) customMuezzin.name else selectedMuezzin.nameArabic,
                                isPlaying = adhanPlayerState.isPlaying,
                                onStopAdhan = { viewModel.stopAdhanPreview() },
                                onDismiss = { viewModel.closeAdhanOverlay() },
                                onNavigateToQibla = {
                                    screenStack = screenStack + QuranScreen.Qibla
                                },
                                onStartPrayer = {
                                    screenStack = screenStack + QuranScreen.Adhkar(AdhkarCategory.AFTER_PRAYER)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
