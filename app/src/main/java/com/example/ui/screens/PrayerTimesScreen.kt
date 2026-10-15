package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import com.example.prayer.CustomMuezzin
import com.example.ui.components.MuslimMunaUpcomingPrayerBar
import com.example.ui.screens.AdhanLiveOverlayScreen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayer.AdhanAlertMode
import com.example.prayer.AdhanMuezzin
import com.example.prayer.CalculationMethod
import com.example.prayer.CityPreset
import com.example.prayer.PrayerSchedule
import com.example.prayer.PrayerTimeItem
import com.example.prayer.PrayerType
import com.example.prayer.PresetCities
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.viewmodel.QuranViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: QuranViewModel,
    onBack: () -> Unit,
    onNavigateToQibla: () -> Unit = {},
    onNavigateToAdhkar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val schedule by viewModel.prayerSchedule.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val selectedMethod by viewModel.selectedCalculationMethod.collectAsState()
    val selectedMuezzin by viewModel.selectedMuezzin.collectAsState()
    val customMuezzin by viewModel.customMuezzin.collectAsState()
    val activeAdhanOverlayPrayer by viewModel.activeAdhanOverlayPrayer.collectAsState()
    val adhanPlayerState by viewModel.adhanPlayerState.collectAsState()

    var showMuezzinSheet by remember { mutableStateOf(false) }
    val muezzinSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showCitySheet by remember { mutableStateOf(false) }
    val citySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showMethodSheet by remember { mutableStateOf(false) }
    val methodSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Permission launcher for GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            detectCurrentLocation(context) { lat, lng, cityName ->
                viewModel.setCustomLocation(cityName, "موقعي الحالي", lat, lng)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "مواقيت الصلاة والأذان",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("prayer_times_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                actions = {
                    // Adhan sound icon
                    IconButton(
                        onClick = { showMuezzinSheet = true },
                        modifier = Modifier.testTag("open_muezzin_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "أصوات المؤذنين",
                            tint = QuranGold
                        )
                    }

                    // Calculation method settings
                    IconButton(
                        onClick = { showMethodSheet = true },
                        modifier = Modifier.testTag("open_method_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "طريقة الحساب",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("prayer_times_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. HERO COUNTDOWN CARD
            item {
                PrayerHeroCard(
                    schedule = schedule,
                    selectedMuezzin = selectedMuezzin,
                    onChangeCityClick = { showCitySheet = true },
                    onChangeMuezzinClick = { showMuezzinSheet = true }
                )
            }

            // 1.5. MUSLIM MUNA UPCOMING PRAYER TICKER / BANNER
            item {
                MuslimMunaUpcomingPrayerBar(
                    schedule = schedule,
                    muezzinName = if (customMuezzin.isEnabled && customMuezzin.filePath.isNotEmpty()) customMuezzin.name else selectedMuezzin.nameArabic,
                    onOpenLiveAdhan = { prayerType ->
                        viewModel.openAdhanOverlay(prayerType)
                    },
                    onToggleAlertMode = { prayerType ->
                        viewModel.togglePrayerAlert(prayerType)
                    },
                    onClickBar = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // 2. ACTIVE MUEZZIN BANNER CARD
            item {
                AdhanMuezzinBannerCard(
                    selectedMuezzin = selectedMuezzin,
                    isPlaying = adhanPlayerState.isPlaying,
                    isBuffering = adhanPlayerState.isBuffering,
                    onPlayPause = {
                        if (adhanPlayerState.isPlaying) viewModel.stopAdhanPreview()
                        else viewModel.playMuezzinPreview(selectedMuezzin)
                    },
                    onChangeMuezzin = { showMuezzinSheet = true }
                )
            }

            // 2.5. QUICK ACTIONS: QIBLA COMPASS & AFTER-PRAYER ADHKAR
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateToQibla)
                            .testTag("prayer_times_to_qibla_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🕋", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "بوصلة القبلة",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateToAdhkar)
                            .testTag("prayer_times_to_adhkar_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🤲", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "أذكار بعد الصلاة",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 3. HEADER: ALL PRAYER TIMES
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "مواقيت اليوم وساعات الأذان:",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "طريقة: ${selectedMethod.nameArabic}",
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { showMethodSheet = true }
                    )
                }
            }

            // 4. LIST OF PRAYERS (Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha)
            items(schedule.items) { item ->
                PrayerItemRowCard(
                    item = item,
                    onToggleAlert = {
                        viewModel.togglePrayerAlert(item.type)
                    }
                )
            }

            // 5. SUNSET & SUNRISE NOTE
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = QuranGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ملاحظات الشروق والغروب:",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• وقت الشروق: ينتهي به وقت صلاة الفجر، ويكره أداء الصلاة عند شروق الشمس مباشرة حتى ترتفع قيد رمح (صلاة الضحى).\n" +
                                    "• وقت الغروب: يتطابق فلكياً مع بدء وقت صلاة المغرب عند مغيب كامل قرص الشمس.\n" +
                                    "• يمكنك الضغط على أيقونة الجرس بجانب أي صلاة للتبديل بين (الأذان الكامل / التكبيرات / إشعار صامت / إيقاف التنبيه).",
                            fontFamily = TajawalFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ================= MODAL BOTTOM SHEETS =================

    // 1. Adhan Muezzin Selection Sheet
    if (showMuezzinSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.stopAdhanPreview()
                showMuezzinSheet = false
            },
            sheetState = muezzinSheetState
        ) {
            MuezzinSelectionSheetContent(
                selectedMuezzin = selectedMuezzin,
                customMuezzin = customMuezzin,
                playerState = adhanPlayerState,
                onSelectMuezzin = { muezzin ->
                    viewModel.selectMuezzin(muezzin)
                },
                onSelectCustomMuezzin = {
                    viewModel.selectCustomMuezzin()
                },
                onUploadCustomAudio = { uri, name ->
                    viewModel.uploadCustomMuezzin(uri, name)
                },
                onTogglePlayPreview = { muezzin ->
                    if (adhanPlayerState.isPlaying && adhanPlayerState.activeMuezzin == muezzin) {
                        viewModel.stopAdhanPreview()
                    } else {
                        viewModel.playMuezzinPreview(muezzin)
                    }
                },
                onTogglePlayCustomPreview = {
                    if (adhanPlayerState.isPlaying) {
                        viewModel.stopAdhanPreview()
                    } else {
                        viewModel.playCustomMuezzinPreview()
                    }
                },
                onClearCustomMuezzin = {
                    viewModel.clearCustomMuezzin()
                },
                onDismiss = {
                    viewModel.stopAdhanPreview()
                    showMuezzinSheet = false
                }
            )
        }
    }

    // 2. City Selection Sheet
    if (showCitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCitySheet = false },
            sheetState = citySheetState
        ) {
            CitySelectionSheetContent(
                currentCity = selectedCity,
                onSelectCity = { city ->
                    viewModel.selectCity(city)
                    showCitySheet = false
                },
                onUseGps = {
                    showCitySheet = false
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onDismiss = { showCitySheet = false }
            )
        }
    }

    // 3. Calculation Method Sheet
    if (showMethodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMethodSheet = false },
            sheetState = methodSheetState
        ) {
            CalculationMethodSheetContent(
                selectedMethod = selectedMethod,
                onSelectMethod = { method ->
                    viewModel.selectCalculationMethod(method)
                    showMethodSheet = false
                },
                onDismiss = { showMethodSheet = false }
            )
        }
    }
}

// ================= HERO CARD =================

@Composable
fun PrayerHeroCard(
    schedule: PrayerSchedule,
    selectedMuezzin: AdhanMuezzin,
    onChangeCityClick: () -> Unit,
    onChangeMuezzinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextPrayer = schedule.nextPrayer

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            IslamicGreen,
                            Color(0xFF0D3D27),
                            Color(0xFF072417)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row: City button & Hijri date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.25f),
                        modifier = Modifier.clickable(onClick = onChangeCityClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = QuranGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = schedule.cityName,
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تغيير",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = QuranGold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = schedule.dateHijriFormatted,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = QuranGold
                        )
                        Text(
                            text = schedule.dateGregorianFormatted,
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Center: Next Prayer Title & Time
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = QuranGold.copy(alpha = 0.2f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = null,
                                tint = QuranGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (nextPrayer != null) "الصلاة القادمة: صلاة ${nextPrayer.type.titleArabic}" else "الصلاة القادمة",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = QuranGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = nextPrayer?.timeFormatted12h ?: "--:--",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // LIVE COUNTDOWN TIMER
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.35f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "متبقي على الأذان:",
                                fontFamily = TajawalFontFamily,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = schedule.countdownFormatted,
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = QuranGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { schedule.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = QuranGold,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }
        }
    }
}

// ================= ADHAN MUEZZIN BANNER CARD =================

@Composable
fun AdhanMuezzinBannerCard(
    selectedMuezzin: AdhanMuezzin,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onPlayPause: () -> Unit,
    onChangeMuezzin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("adhan_muezzin_banner_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Play / Pause preview button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IslamicGreen)
                        .clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف" else "تشغيل الأذان",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "صوت الأذان المعتمد:",
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedMuezzin.nameArabic,
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = selectedMuezzin.locationOrSheikh,
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        color = QuranGold
                    )
                }
            }

            OutlinedButton(
                onClick = onChangeMuezzin,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "تغيير المؤذن",
                    fontFamily = TajawalFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ================= PRAYER ITEM ROW CARD =================

@Composable
fun PrayerItemRowCard(
    item: PrayerTimeItem,
    onToggleAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNext = item.isNext
    val isCurrent = item.isCurrent

    val cardBorder = if (isNext) CardDefaults.outlinedCardBorder().copy(
        brush = Brush.horizontalGradient(listOf(IslamicGreen, QuranGold)),
        width = 2.dp
    ) else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("prayer_row_${item.type.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isNext -> IslamicGreen.copy(alpha = 0.08f)
                isCurrent -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon & Name & Next Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNext) IslamicGreen.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPrayerIcon(item.type),
                        contentDescription = null,
                        tint = if (isNext) IslamicGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.type.titleArabic,
                            fontFamily = TajawalFontFamily,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (isNext) IslamicGreen else MaterialTheme.colorScheme.onSurface
                        )

                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = QuranGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "الصلاة القادمة",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (!item.type.hasAdhan) {
                        Text(
                            text = if (item.type == PrayerType.SUNRISE) "وقت انتهاء الفجر" else "وقت مغيب الشمس",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Time & Alert toggle button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.timeFormatted12h,
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (isNext) IslamicGreen else MaterialTheme.colorScheme.onSurface
                )

                if (item.type.hasAdhan) {
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = onToggleAlert,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("toggle_alert_${item.type.id}")
                    ) {
                        val (icon, tint) = when (item.alertMode) {
                            AdhanAlertMode.FULL_ADHAN -> Pair(Icons.Default.NotificationsActive, IslamicGreen)
                            AdhanAlertMode.TAKBEER_ONLY -> Pair(Icons.Default.VolumeUp, QuranGold)
                            AdhanAlertMode.NOTIFICATION_ONLY -> Pair(Icons.Default.Notifications, Color(0xFF2563EB))
                            AdhanAlertMode.MUTED -> Pair(Icons.Default.NotificationsOff, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = item.alertMode.titleArabic,
                            tint = tint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getPrayerIcon(type: PrayerType): ImageVector {
    return when (type) {
        PrayerType.FAJR -> Icons.Default.DarkMode
        PrayerType.SUNRISE -> Icons.Default.WbSunny
        PrayerType.DHUHR -> Icons.Default.WbSunny
        PrayerType.ASR -> Icons.Default.WbSunny
        PrayerType.SUNSET -> Icons.Default.WbTwilight
        PrayerType.MAGHRIB -> Icons.Default.WbTwilight
        PrayerType.ISHA -> Icons.Default.DarkMode
    }
}

// ================= ADHAN MUEZZIN SELECTION SHEET =================

@Composable
fun MuezzinSelectionSheetContent(
    selectedMuezzin: AdhanMuezzin,
    customMuezzin: CustomMuezzin,
    playerState: com.example.prayer.AdhanPlayerState,
    onSelectMuezzin: (AdhanMuezzin) -> Unit,
    onSelectCustomMuezzin: () -> Unit,
    onUploadCustomAudio: (android.net.Uri, String) -> Unit,
    onTogglePlayPreview: (AdhanMuezzin) -> Unit,
    onTogglePlayCustomPreview: () -> Unit,
    onClearCustomMuezzin: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val fileName = getFileNameFromUri(context, uri) ?: "أذان مخصص من هاتفي"
            onUploadCustomAudio(uri, fileName)
            onSelectCustomMuezzin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "اختيار صوت المؤذن والأذان",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "اختر من أصوات كبار المؤذنين أو حمّل أذانك المفضل من جهازك",
                    fontFamily = TajawalFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(430.dp)
        ) {
            // 0. CUSTOM AUDIO FROM PHONE
            item {
                Text(
                    text = "مؤذن مخصص من هاتفك:",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (customMuezzin.filePath.isNotEmpty()) {
                    val isSelected = customMuezzin.isEnabled
                    val isPlayingCustom = playerState.isPlaying && playerState.isCustomActive

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectCustomMuezzin() }
                            .testTag("custom_muezzin_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) QuranGold.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(QuranGold, QuranGold)),
                            width = 1.5.dp
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = onSelectCustomMuezzin
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "📁", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = customMuezzin.name,
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "ملف صوتي مخزن محلياً على الهاتف",
                                        fontFamily = TajawalFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Play preview
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlayingCustom) QuranGold else MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { onTogglePlayCustomPreview() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingCustom) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "استماع",
                                        tint = if (isPlayingCustom) Color.Black else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Re-upload button
                                IconButton(
                                    onClick = { audioPicker.launch("audio/*") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileUpload,
                                        contentDescription = "تغيير الملف",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Delete custom
                                IconButton(
                                    onClick = onClearCustomMuezzin,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف",
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Upload button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { audioPicker.launch("audio/*") }
                            .testTag("upload_custom_muezzin_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(IslamicGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileUpload,
                                        contentDescription = null,
                                        tint = IslamicGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "تحميل مؤذن أو أذان من هاتفك",
                                        fontFamily = TajawalFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "اختر أي ملف صوتي MP3 من جهازك",
                                        fontFamily = TajawalFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = IslamicGreen
                            ) {
                                Text(
                                    text = "اختيار ملف ➔",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "أو اختر من أصوات كبار المؤذنين:",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(AdhanMuezzin.values()) { muezzin ->
                val isSelected = muezzin == selectedMuezzin
                val isCurrentlyPlaying = playerState.isPlaying && playerState.activeMuezzin == muezzin
                val isBufferingThis = playerState.isBuffering && playerState.activeMuezzin == muezzin

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectMuezzin(muezzin) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) IslamicGreen.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(IslamicGreen, IslamicGreen)),
                        width = 1.5.dp
                    ) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectMuezzin(muezzin) }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = muezzin.nameArabic,
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = muezzin.locationOrSheikh,
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Play preview button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrentlyPlaying) QuranGold
                                    else MaterialTheme.colorScheme.primaryContainer
                                )
                                .clickable { onTogglePlayPreview(muezzin) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBufferingThis) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isCurrentlyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "استماع",
                                    tint = if (isCurrentlyPlaying) Color.Black else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
        ) {
            Text(
                text = "حفظ وتأكيد الاختيار",
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

// ================= CITY SELECTION SHEET =================

@Composable
fun CitySelectionSheetContent(
    currentCity: CityPreset,
    onSelectCity: (CityPreset) -> Unit,
    onUseGps: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allCities = PresetCities.list

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) allCities
        else allCities.filter {
            it.nameArabic.contains(searchQuery.trim()) || it.countryArabic.contains(searchQuery.trim())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "اختيار المدينة والدولة",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "المدينة الحالية: ${currentCity.nameArabic} (${currentCity.countryArabic})",
                    fontFamily = TajawalFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // GPS Auto-detect button
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = IslamicGreen.copy(alpha = 0.12f),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onUseGps)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = IslamicGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "تحديد موقعي الحالي تلقائياً (GPS)",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = IslamicGreen
                    )
                    Text(
                        text = "حساب دقيق لمواقيت الصلاة حسب إحداثيات موقعك الجغرافي",
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "ابحث عن المدينة (القاهرة، الإسكندرية، مكة، الرياض...)",
                    fontFamily = TajawalFontFamily,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            items(filtered) { city ->
                val isSelected = city.nameArabic == currentCity.nameArabic

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectCity(city) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) IslamicGreen.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) IslamicGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = city.nameArabic,
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = city.countryArabic,
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = IslamicGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= CALCULATION METHOD SHEET =================

@Composable
fun CalculationMethodSheetContent(
    selectedMethod: CalculationMethod,
    onSelectMethod: (CalculationMethod) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "طريقة حساب مواقيت الصلاة",
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "اختر الهيئة أو المرجع الفلكي المعتمد في بلدك",
            fontFamily = TajawalFontFamily,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        CalculationMethod.values().forEach { method ->
            val isSelected = method == selectedMethod

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectMethod(method) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) IslamicGreen.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectMethod(method) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = method.nameArabic,
                            fontFamily = TajawalFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "زاوية الفجر: ${method.fajrAngle}° • زاوية العشاء: ${if (method.ishaFixedMinutes != null) "${method.ishaFixedMinutes} دقيقة" else "${method.ishaAngle}°"}",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@SuppressLint("MissingPermission")
private fun detectCurrentLocation(context: Context, onLocationFound: (Double, Double, String) -> Unit) {
    try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            var cityName = "موقعي الحالي"
            try {
                val geocoder = Geocoder(context, Locale("ar"))
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    cityName = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea ?: "موقعي الحالي"
                }
            } catch (_: Exception) {}

            onLocationFound(location.latitude, location.longitude, cityName)
        }
    } catch (_: Exception) {}
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = try {
            context.contentResolver.query(uri, null, null, null, null)
        } catch (_: Exception) {
            null
        }
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}
