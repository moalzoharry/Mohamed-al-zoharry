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
import com.example.ui.components.ReadingSettingsBottomSheet
import com.example.ui.screens.AdvancedSearchScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DuaaKhatmScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SurahReaderScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.theme.QuranAppTheme
import com.example.ui.viewmodel.QuranViewModel

sealed interface QuranScreen {
    object Home : QuranScreen
    data class Reader(val surahNumber: Int, val initialAyah: Int = 1) : QuranScreen
    object Search : QuranScreen
    object Bookmarks : QuranScreen
    object DuaaKhatm : QuranScreen
    object PrayerTimes : QuranScreen
}

class MainActivity : ComponentActivity() {

    private val viewModel: QuranViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()
            val isDarkTheme = settings.theme.isDark
            val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()

            androidx.compose.runtime.LaunchedEffect(systemInDark, settings.autoNightMode) {
                viewModel.checkAutoNightMode(isSystemInDark = systemInDark)
            }

            var screenStack by remember { mutableStateOf(listOf<QuranScreen>(QuranScreen.Home)) }
            val currentScreen = screenStack.lastOrNull() ?: QuranScreen.Home

            var showSettingsSheet by remember { mutableStateOf(false) }
            val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

                        when (val screen = currentScreen) {
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
                                    onOpenSettings = {
                                        showSettingsSheet = true
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
                    }
                }
            }
        }
    }
}
