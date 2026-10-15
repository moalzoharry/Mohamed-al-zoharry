package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.MuslimMunaUpcomingPrayerBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.components.AudioBottomPlayer
import com.example.ui.components.DailyWirdCard
import com.example.ui.components.DailyWirdReminderSheet
import com.example.ui.components.PrayerCountdownCard
import com.example.ui.components.ReciterSelectionBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JuzInfo
import com.example.data.repository.QuranStaticData
import com.example.ui.components.SurahItemCard
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: QuranViewModel,
    onNavigateToReader: (surahNumber: Int, ayahNumber: Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToDuaaKhatm: () -> Unit,
    onNavigateToPrayerTimes: () -> Unit,
    onNavigateToQibla: () -> Unit = {},
    onNavigateToAdhkar: () -> Unit = {},
    onNavigateToTasbih: () -> Unit = {},
    onNavigateToMustajabDuaa: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val surahs = viewModel.allSurahs
    val lastRead by viewModel.lastRead.collectAsState()
    val audioState by viewModel.audioPlayerState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val wirdReminder by viewModel.dailyWirdReminder.collectAsState()
    val prayerSchedule by viewModel.prayerSchedule.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var filterQuery by remember { mutableStateOf("") }
    var showReciterSheet by remember { mutableStateOf(false) }
    val reciterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showWirdSheet by remember { mutableStateOf(false) }
    val wirdSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredSurahs = remember(surahs, filterQuery) {
        if (filterQuery.isBlank()) surahs
        else {
            val normalized = QuranStaticData.normalizeArabic(filterQuery)
            surahs.filter {
                QuranStaticData.normalizeArabic(it.nameArabic).contains(normalized) ||
                        it.nameEnglish.contains(filterQuery, ignoreCase = true) ||
                        it.number.toString() == filterQuery.trim()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(QuranGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "۞",
                                fontSize = 18.sp,
                                color = QuranGold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "المصحف الرقمي",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "تفسير الجلالين • التفسير الميسر",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                },
                actions = {
                    val isNight = settings.theme.isDark

                    IconButton(
                        onClick = { showWirdSheet = true },
                        modifier = Modifier.testTag("home_wird_reminder_button")
                    ) {
                        Icon(
                            imageVector = if (wirdReminder.isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = if (wirdReminder.isEnabled) "منبه الورد القرآني (مفعّل)" else "ضبط منبه الورد اليومي",
                            tint = if (wirdReminder.isEnabled) QuranGold else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleQuickNightMode() },
                        modifier = Modifier.testTag("home_quick_night_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isNight) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isNight) "تبديل إلى الوضع النهاري" else "الوضع الليلي السريع لتقليل إجهاد العين",
                            tint = if (isNight) QuranGold else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("search_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "البحث في القرآن والتفسير",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onNavigateToPrayerTimes,
                        modifier = Modifier.testTag("home_prayer_times_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = "مواقيت الصلاة والأذان",
                            tint = QuranGold
                        )
                    }

                    IconButton(
                        onClick = onOpenAbout,
                        modifier = Modifier.testTag("about_app_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "حول البرنامج والمطور (م. محمد الزهري)",
                            tint = QuranGold
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "إعدادات القراءة",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
        modifier = modifier.testTag("home_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 0. INTRODUCTORY CARD: DEDICATION & PRAYER FOR LIVING AND DECEASED MUSLIMS & RECITING AL-FATIHAH
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("muslims_prayer_intro_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(QuranGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "۞",
                                        fontSize = 16.sp,
                                        color = QuranGold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "إهداء ودعاء لأموات وأحياء المسلمين",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = QuranGold.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "صدقة جارية",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = QuranGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Heartfelt Duaa Text
                        Text(
                            text = "«اللَّهُمَّ اغْفِرْ لِأَحْيَائِنَا وَأَمْوَاتِنَا، وَشَاهِدِنَا وَغَائِبِنَا، وَصَغِيرِنَا وَكَبِيرِنَا، وَذَكَرِنَا وَأُنْثَانَا. اللَّهُمَّ اغْفِرْ لِجَمِيعِ مَوْتَى الْمُسْلِمِينَ الَّذِينَ شَهِدُوا لَكَ بِالْوَحْدَانِيَّةِ وَلِنَبِيِّكَ بِالرِّسَالَةِ وَمَاتُوا عَلَى ذَلِكَ، اللَّهُمَّ ارْحَمْهُمْ وَعَافِهِمْ وَاعْفُ عَنْهُمْ، وَاجْعَلْ قُبُورَهُمْ رَوْضَةً مِنْ رِيَاضِ الْجَنَّةِ، وَارْحَمْنَا إِذَا صِرْنَا إِلَى مَا صَارُوا إِلَيْهِ»",
                            fontFamily = AmiriFontFamily,
                            fontSize = 16.sp,
                            lineHeight = 25.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Read Al-Fatihah & Duaa Khatm Al-Quran
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Recite Al-Fatihah
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToReader(1, 1) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "قراءة الفاتحة لأرواحهم",
                                        fontFamily = TajawalFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            // 2. Duaa Khatm Al-Quran
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = onNavigateToDuaaKhatm)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🤲 دعاء ختم القرآن",
                                        fontFamily = TajawalFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 0.45. MUSLIM MUNA UPCOMING PRAYER TICKER / REMINDER TAB
            item {
                MuslimMunaUpcomingPrayerBar(
                    schedule = prayerSchedule,
                    muezzinName = viewModel.selectedMuezzin.collectAsState().value.nameArabic,
                    onOpenLiveAdhan = { prayerType ->
                        viewModel.openAdhanOverlay(prayerType)
                    },
                    onToggleAlertMode = { prayerType ->
                        viewModel.togglePrayerAlert(prayerType)
                    },
                    onClickBar = onNavigateToPrayerTimes,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // 0.5. LIVE PRAYER COUNTDOWN & ADHAN CARD
            item {
                PrayerCountdownCard(
                    schedule = prayerSchedule,
                    onClick = onNavigateToPrayerTimes
                )
            }

            // 0.6. ISLAMIC SERVICES & WORSHIP QUICK GRID
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("islamic_services_grid_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "بوابة العبادات والأدعية المباركة",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "خدمات إسلامية متكاملة",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IslamicQuickServiceTile(
                                iconText = "🕋",
                                title = "بوصلة القبلة",
                                subtitle = "مستشعرات الهاتف",
                                onClick = onNavigateToQibla,
                                modifier = Modifier.weight(1f),
                                testTag = "home_qibla_tile"
                            )
                            IslamicQuickServiceTile(
                                iconText = "📿",
                                title = "السبحة",
                                subtitle = "عداد مفتوح وحر",
                                onClick = onNavigateToTasbih,
                                modifier = Modifier.weight(1f),
                                testTag = "home_tasbih_tile"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IslamicQuickServiceTile(
                                iconText = "☀️",
                                title = "الأذكار",
                                subtitle = "الصباح والمساء",
                                onClick = onNavigateToAdhkar,
                                modifier = Modifier.weight(1f),
                                testTag = "home_adhkar_tile"
                            )
                            IslamicQuickServiceTile(
                                iconText = "🤲",
                                title = "أدعية مستجابة",
                                subtitle = "الكنز والشعراوي",
                                onClick = onNavigateToMustajabDuaa,
                                modifier = Modifier.weight(1f),
                                testTag = "home_mustajab_duaa_tile"
                            )
                        }
                    }
                }
            }

            // 1. LAST READ BANNER / HERO
            item {
                lastRead?.let { progress ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("continue_reading_card")
                            .clickable {
                                onNavigateToReader(progress.surahNumber, progress.ayahNumber)
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(IslamicGreen, Color(0xFF0A442A))
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoStories,
                                            contentDescription = null,
                                            tint = QuranGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "واصل القراءة",
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = QuranGold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "متابعة الآن ➔",
                                            fontFamily = TajawalFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "سورة ${progress.surahName} : آية ${progress.ayahNumber}",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )

                                if (progress.ayahTextPreview.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "﴿ ${progress.ayahTextPreview.take(65)}... ﴾",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = 15.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1.5. DAILY WIRD REMINDER HERO CARD
            item {
                DailyWirdCard(
                    reminder = wirdReminder,
                    onToggle = { viewModel.toggleDailyWirdReminder() },
                    onConfigureClick = { showWirdSheet = true },
                    onTestNotificationClick = { viewModel.testDailyWirdNotification() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // 2. SEARCH / FILTER SURAHS INPUT
            item {
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = {
                        Text(
                            text = "ابحث عن اسم السورة أو رقمها...",
                            fontFamily = TajawalFontFamily,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("filter_surah_input")
                )
            }

            // 3. TABS: SURAHS VS JUZ
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = "فهرس السور (${filteredSurahs.size})",
                                fontFamily = TajawalFontFamily,
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                text = "فهرس الأجزاء (30 جزء)",
                                fontFamily = TajawalFontFamily,
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            // 4. LIST CONTENT
            if (selectedTabIndex == 0) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    SurahItemCard(
                        surah = surah,
                        onClick = { onNavigateToReader(surah.number, 1) }
                    )
                }
            } else {
                items(QuranStaticData.juzList, key = { it.juzNumber }) { juz ->
                    JuzItemCard(
                        juz = juz,
                        onClick = { onNavigateToReader(juz.startSurahNumber, juz.startAyahNumber) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showReciterSheet) {
            ReciterSelectionBottomSheet(
                selectedReciter = settings.selectedReciter,
                sheetState = reciterSheetState,
                onReciterSelected = { reciter -> viewModel.changeReciter(reciter) },
                onDismissRequest = { showReciterSheet = false }
            )
        }

        if (showWirdSheet) {
            DailyWirdReminderSheet(
                reminder = wirdReminder,
                sheetState = wirdSheetState,
                onDismissRequest = { showWirdSheet = false },
                onSaveReminder = { updatedReminder ->
                    viewModel.updateDailyWirdReminder(updatedReminder)
                    showWirdSheet = false
                },
                onTestNotification = {
                    viewModel.testDailyWirdNotification()
                }
            )
        }
    }
}

@Composable
fun JuzItemCard(
    juz: JuzInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${juz.juzNumber}",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = juz.nameArabic,
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "يبدأ من سورة ${juz.startSurahName} (آية ${juz.startAyahNumber})",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "قراءة ➔",
                fontFamily = TajawalFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun IslamicQuickServiceTile(
    iconText: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = iconText,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontFamily = TajawalFontFamily,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

