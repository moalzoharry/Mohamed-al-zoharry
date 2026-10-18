package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.data.model.ReadingSettings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ayah
import com.example.data.model.ReadingMode
import com.example.ui.components.AudioBottomPlayer
import com.example.ui.components.AyahItemCard
import com.example.ui.components.BasmalahHeader
import com.example.ui.components.ContentProtectionBadge
import com.example.ui.components.ContentProtectionWarningDialog
import com.example.ui.components.ReadingSettingsBottomSheet
import com.example.ui.components.ReciterSelectionBottomSheet
import com.example.ui.components.SurahBanner
import com.example.ui.components.TafsirModalBottomSheet
import com.example.ui.components.formatArabicNumber
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.viewmodel.QuranViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderScreen(
    viewModel: QuranViewModel,
    onBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToDuaaKhatm: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val currentSurah by viewModel.currentSurah.collectAsState()
    val ayahs by viewModel.currentAyahs.collectAsState()
    val isLoading by viewModel.isLoadingAyahs.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val allReadingProgress by viewModel.allReadingProgress.collectAsState()
    val currentSurahProgress = allReadingProgress.firstOrNull { it.surahNumber == currentSurah?.number }
    val savedStopAyah = currentSurahProgress?.ayahNumber
    val audioState by viewModel.audioPlayerState.collectAsState()
    val selectedAyahForTafsir by viewModel.selectedAyahForTafsir.collectAsState()
    val scrollTargetAyah by viewModel.scrollTargetAyah.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showReciterSheet by remember { mutableStateOf(false) }
    var showProtectionDialog by remember { mutableStateOf(false) }
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reciterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tafsirSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val theme = settings.theme
    val bgColor = Color(theme.backgroundColorHex)
    val textColor = Color(theme.textColorHex)
    val surfaceColor = Color(theme.surfaceColorHex)

    // Scroll to target ayah if requested
    LaunchedEffect(scrollTargetAyah, ayahs) {
        scrollTargetAyah?.let { target ->
            val index = ayahs.indexOfFirst { it.ayahNumber == target }
            if (index >= 0) {
                // Offset for banner + basmalah
                val scrollIndex = if (currentSurah?.number != 9 && currentSurah?.number != 1) index + 2 else index + 1
                listState.animateScrollToItem(scrollIndex.coerceAtLeast(0))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currentSurah?.let { surah ->
                            Text(
                                text = "سورة ${surah.nameArabic}",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textColor
                            )
                            Text(
                                text = "الجزء ${surah.startJuz} • ${surah.revelationType.titleArabic} • ${surah.ayahCount} آية",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = Color(theme.secondaryTextHex)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    // Quick Night Mode Toggle Button (زر سريع للوضع الليلي وتقليل إجهاد العين)
                    IconButton(
                        onClick = {
                            val isNowNight = viewModel.toggleQuickNightMode()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (isNowNight) "🌙 تم تفعيل وضع القراءة الليلي لتقليل إجهاد العين"
                                              else "☀️ تم تفعيل وضع القراءة النهاري",
                                    withDismissAction = true
                                )
                            }
                        },
                        modifier = Modifier.testTag("quick_night_mode_button")
                    ) {
                        Icon(
                            imageVector = if (theme.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (theme.isDark) "تبديل إلى الوضع النهاري" else "الوضع الليلي السريع لتقليل إجهاد العين",
                            tint = if (theme.isDark) QuranGold else textColor
                        )
                    }

                    IconButton(
                        onClick = { showReciterSheet = true },
                        modifier = Modifier.testTag("reader_reciter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "اختيار القارئ",
                            tint = textColor
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("reader_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "البحث في الآيات",
                            tint = textColor
                        )
                    }

                    ContentProtectionBadge(
                        onClick = { showProtectionDialog = true },
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("reader_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "تخصيص القراءة والخط والخلفية",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor
                )
            )
        },
        bottomBar = {
            AudioBottomPlayer(
                state = audioState,
                onTogglePlayPause = { viewModel.audioPlayer.togglePlayPause() },
                onNext = { viewModel.audioPlayer.nextAyah() },
                onPrevious = { viewModel.audioPlayer.previousAyah() },
                onRepeatClick = { viewModel.cycleRepeatMode() },
                onSelectReciterClick = { showReciterSheet = true },
                onStop = { viewModel.audioPlayer.stop() }
            )
        },
        modifier = modifier.testTag("surah_reader_screen")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(paddingValues)
        ) {
            // Eye Comfort Warm Blue-Light Reduction Overlay
            if (settings.eyeComfortWarmFilter) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE89A3C).copy(alpha = 0.06f))
                )
            }
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(theme.accentColorHex)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "جاري تحميل الآيات والتفاسير...",
                            fontFamily = TajawalFontFamily,
                            color = textColor
                        )
                    }
                }
            } else {
                currentSurah?.let { surah ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        // 1. SURAH BANNER
                        item {
                            SurahBanner(
                                surahInfo = surah,
                                theme = theme
                            )
                        }

                        // 2. BASMALAH (Except Surah At-Tawbah 9 and already included in Al-Fatihah 1)
                        if (surah.number != 9 && surah.number != 1) {
                            item {
                                BasmalahHeader(theme = theme)
                            }
                        }

                        // 2.5. SAVED STOPPING POINT BANNER IN THIS SURAH
                        if (savedStopAyah != null && savedStopAyah > 1) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = QuranGold.copy(alpha = 0.12f),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.horizontalGradient(listOf(QuranGold, Color.White.copy(alpha = 0.6f))),
                                        width = 1.2.dp
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                        .clickable {
                                            val index = ayahs.indexOfFirst { it.ayahNumber == savedStopAyah }
                                            if (index >= 0) {
                                                val offset = if (surah.number != 9 && surah.number != 1) 3 else 2
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem((index + offset).coerceAtLeast(0))
                                                }
                                            }
                                        }
                                        .testTag("saved_stop_ayah_banner")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(text = "📌", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "موضع وقوفك في سورة ${surah.nameArabic}: الآية $savedStopAyah",
                                                    fontFamily = TajawalFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = textColor
                                                )
                                                Text(
                                                    text = "اضغط للقفز المباشر لموضع وقوفك ومتابعة التلاوة",
                                                    fontFamily = TajawalFontFamily,
                                                    fontSize = 11.sp,
                                                    color = Color(theme.secondaryTextHex)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = QuranGold
                                        ) {
                                            Text(
                                                text = "انتقال ➔",
                                                fontFamily = TajawalFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. READING CONTENT: AYAH BY AYAH OR CONTINUOUS
                        if (settings.readingMode == ReadingMode.AYAH_BY_AYAH) {
                            itemsIndexed(ayahs, key = { _, ayah -> "${ayah.surahNumber}_${ayah.ayahNumber}" }) { _, ayah ->
                                val isBookmarked = bookmarks.any {
                                    it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.ayahNumber
                                }
                                val isPlaying = audioState.isPlaying &&
                                        audioState.currentSurah == ayah.surahNumber &&
                                        audioState.currentAyah == ayah.ayahNumber
                                val isStopPoint = (savedStopAyah == ayah.ayahNumber)

                                AyahItemCard(
                                    ayah = ayah,
                                    surahName = surah.nameArabic,
                                    settings = settings,
                                    isBookmarked = isBookmarked,
                                    isPlaying = isPlaying,
                                    isSavedStopPoint = isStopPoint,
                                    onTafsirClick = { viewModel.openTafsir(ayah) },
                                    onBookmarkClick = { viewModel.toggleBookmark(ayah, surah.nameArabic) },
                                    onSaveStopPointClick = {
                                        viewModel.saveSurahStopAyah(
                                            surahNumber = ayah.surahNumber,
                                            ayahNumber = ayah.ayahNumber,
                                            surahName = surah.nameArabic,
                                            ayahText = ayah.textWithTashkeel
                                        )
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "📌 تم حفظ الآية ${ayah.ayahNumber} كموضع وقوفك في سورة ${surah.nameArabic}",
                                                withDismissAction = true
                                            )
                                        }
                                    },
                                    onPlayClick = {
                                        if (isPlaying) {
                                            viewModel.audioPlayer.togglePlayPause()
                                        } else {
                                            viewModel.playAyah(ayah, surah)
                                        }
                                    }
                                )
                            }
                        } else {
                            // CONTINUOUS MUSHAF PAGE READING MODE
                            item {
                                ContinuousMushafCard(
                                    ayahs = ayahs,
                                    surahName = surah.nameArabic,
                                    settings = settings,
                                    onAyahClick = { ayah -> viewModel.openTafsir(ayah) }
                                )
                            }
                        }

                        // If Surah An-Nas (114), show Khatm Al-Quran prompt
                        if (surah.number == 114 && onNavigateToDuaaKhatm != null) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                        .clickable { onNavigateToDuaaKhatm() },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(theme.accentColorHex)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "۞ قراءة دعاء ختم القرآن الكريم ۞",
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // 4. SURAH NAVIGATION BAR (PREVIOUS / NEXT SURAH)
                        item {
                            SurahNavBottomBar(
                                currentSurahNumber = surah.number,
                                onSelectSurah = { newSurah -> viewModel.loadSurah(newSurah, 1) },
                                theme = theme
                            )
                        }
                    }
                }
            }
        }
    }

    // TAFSIR MODAL BOTTOM SHEET
    selectedAyahForTafsir?.let { ayah ->
        currentSurah?.let { surah ->
            TafsirModalBottomSheet(
                ayah = ayah,
                surahName = surah.nameArabic,
                settings = settings,
                sheetState = tafsirSheetState,
                onDismissRequest = { viewModel.closeTafsir() },
                onFontSizeChange = { newSize ->
                    viewModel.updateSettings(settings.copy(tafsirFontSize = newSize))
                }
            )
        }
    }

    // CUSTOMIZABLE READING SETTINGS SHEET
    if (showSettingsSheet) {
        ReadingSettingsBottomSheet(
            settings = settings,
            sheetState = settingsSheetState,
            onDismissRequest = { showSettingsSheet = false },
            onSaveSettings = { newSettings -> viewModel.updateSettings(newSettings) }
        )
    }

    // RECITER SELECTION SHEET
    if (showReciterSheet) {
        ReciterSelectionBottomSheet(
            selectedReciter = settings.selectedReciter,
            sheetState = reciterSheetState,
            onReciterSelected = { reciter -> viewModel.changeReciter(reciter) },
            onDismissRequest = { showReciterSheet = false }
        )
    }

    if (showProtectionDialog) {
        ContentProtectionWarningDialog(
            onDismissRequest = { showProtectionDialog = false },
            targetItemName = "المصحف الشريف والآيات الكريمة"
        )
    }
}

@Composable
fun ContinuousMushafCard(
    ayahs: List<Ayah>,
    surahName: String,
    settings: ReadingSettings,
    onAyahClick: (Ayah) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = settings.theme
    val textColor = Color(theme.textColorHex)
    val accentColor = Color(theme.accentColorHex)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(theme.surfaceColorHex),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "عرض القراءة المتصلة (انقر على أي آية لعرض التفسير)",
                fontFamily = TajawalFontFamily,
                fontSize = 11.sp,
                color = Color(theme.secondaryTextHex),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Concatenated verses with ornamental number glyphs
            val textBuilder = StringBuilder()
            ayahs.forEach { ayah ->
                val txt = if (settings.showTashkeel) ayah.textWithTashkeel else ayah.textClean
                textBuilder.append("$txt ﴿${formatArabicNumber(ayah.ayahNumber)}﴾ ")
            }

            Text(
                text = textBuilder.toString(),
                fontFamily = AmiriFontFamily,
                fontSize = settings.quranFontSize.sp,
                lineHeight = (settings.quranFontSize * 1.9f).sp,
                color = textColor,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SurahNavBottomBar(
    currentSurahNumber: Int,
    onSelectSurah: (Int) -> Unit,
    theme: com.example.data.model.ReadingTheme,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentSurahNumber > 1) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(theme.surfaceColorHex),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.clickable { onSelectSurah(currentSurahNumber - 1) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "السورة السابقة",
                        tint = Color(theme.accentColorHex)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "السورة السابقة",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(theme.textColorHex)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (currentSurahNumber < 114) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(theme.surfaceColorHex),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.clickable { onSelectSurah(currentSurahNumber + 1) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "السورة التالية",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(theme.textColorHex)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "السورة التالية",
                        tint = Color(theme.accentColorHex)
                    )
                }
            }
        }
    }
}
