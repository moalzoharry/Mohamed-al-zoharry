package com.example.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adhkar.AdhkarCategory
import com.example.adhkar.AdhkarData
import com.example.adhkar.DhikrItem
import com.example.data.repository.QuranStaticData
import com.example.ui.components.ContentProtectionBadge
import com.example.ui.components.ContentProtectionWarningDialog
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorningEveningWirdScreen(
    isEvening: Boolean = false,
    onCloseScreen: () -> Unit,
    onNavigateToReader: (surahNumber: Int, ayahNumber: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onCloseScreen() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showProtectionDialog by remember { mutableStateOf(false) }

    // Audio state
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("مقدمة الذكر والصلاة على النبي ﷺ") } // "الاستفتاح" or "تلاوة آية الكرسي - إسلام صبحي"
    var audioProgress by remember { mutableFloatStateOf(0f) }
    var durationMs by remember { mutableIntStateOf(165000) } // ~2:45 total
    var currentPositionMs by remember { mutableIntStateOf(0) }

    // MediaPlayer reference
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    // Audio URLs
    val ayatAlKursiIslamSobhiUrl = "https://ia801503.us.archive.org/15/items/islam-sobhi-ayat-al-kursi/ayat_al_kursi_islam_sobhi.mp3"
    // Fallback verified CDN
    val backupUrl = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/002255.mp3"

    fun setupMediaPlayer() {
        try {
            mediaPlayer?.release()
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(ayatAlKursiIslamSobhiUrl)
                setOnPreparedListener { preparedMp ->
                    isBuffering = false
                    durationMs = preparedMp.duration.coerceAtLeast(1000)
                    preparedMp.start()
                    isPlaying = true
                    currentPhase = "تلاوة آية الكرسي خاشعة • الشيخ إسلام صبحي"
                }
                setOnCompletionListener {
                    isPlaying = false
                    audioProgress = 1f
                    currentPositionMs = durationMs
                    currentPhase = "اكتملت التلاوة المباركة والحمد لله"
                }
                setOnErrorListener { _, _, _ ->
                    // Fallback to secondary stream
                    try {
                        reset()
                        setDataSource(backupUrl)
                        prepareAsync()
                    } catch (e: Exception) {
                        isBuffering = false
                        isPlaying = false
                    }
                    true
                }
            }
            mediaPlayer = mp
            isBuffering = true
            mp.prepareAsync()
        } catch (e: Exception) {
            isBuffering = false
            isPlaying = false
            Toast.makeText(context, "يمكنك الاستماع للتلاوة مباشرة مع اتصال الإنترنت", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Progress updater loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    val pos = mp.currentPosition
                    val dur = mp.duration.coerceAtLeast(1)
                    currentPositionMs = pos
                    durationMs = dur
                    audioProgress = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                }
            }
            kotlinx.coroutines.delay(500)
        }
    }

    val pageTitle = if (isEvening) "ورد المساء المبارك (٥:٠٠ م)" else "ورد الصباح المبارك (١٠:٠٠ ص)"
    val wirdSubtitle = if (isEvening) "أذكار المساء وآية الكرسي وتلاوة ما تيسر من القرآن" else "أذكار الصباح وآية الكرسي وتلاوة ما تيسر من القرآن"

    val adhkarList = remember(isEvening) {
        if (isEvening) AdhkarData.getAdhkarForCategory(AdhkarCategory.EVENING)
        else AdhkarData.getAdhkarForCategory(AdhkarCategory.MORNING)
    }

    if (showProtectionDialog) {
        ContentProtectionWarningDialog(
            onDismissRequest = { showProtectionDialog = false },
            targetItemName = "أوراد الصباح والمساء وآيات القرآن"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = pageTitle,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = wirdSubtitle,
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = QuranGold
                        )
                    }
                },
                navigationIcon = {
                    // Working Close button as requested: "وزرار الغلق شغال لو حب المستخدم يكنسل"
                    IconButton(
                        onClick = onCloseScreen,
                        modifier = Modifier.testTag("wird_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق التذكير والعودة",
                            tint = Color(0xFFDC2626)
                        )
                    }
                },
                actions = {
                    ContentProtectionBadge(
                        onClick = { showProtectionDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("morning_evening_wird_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. AUDIO PLAYER CARD (سورة الكرسي بصوت إسلام صبحي مع الاستفتاح النبوي)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGreen.copy(alpha = 0.12f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(IslamicGreen.copy(alpha = 0.4f))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Header Invocation requested by user
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(QuranGold.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎙️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الاستفتاح المبارك وسورة الكرسي",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentPhase,
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = IslamicGreen,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text of the opening invocation
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "«بِسْمِ اللَّهِ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ، وَصَلَّى اللَّهُ عَلَى سَيِّدِنَا مُحَمَّدٍ وَعَلَى آلِهِ وَصَحْبِهِ وَسَلَّمَ عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ»",
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = IslamicGreen,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ثم تلاوة آية الكرسي بصوت الشيخ إسلام صبحي حفظه الله",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = QuranGold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar
                    if (isBuffering) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = QuranGold
                        )
                    } else {
                        Slider(
                            value = audioProgress,
                            onValueChange = { newProgress ->
                                audioProgress = newProgress
                                mediaPlayer?.let { mp ->
                                    val targetMs = (newProgress * durationMs).toInt()
                                    mp.seekTo(targetMs)
                                }
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = QuranGold,
                                activeTrackColor = IslamicGreen
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                        )
                    }

                    // Audio controls row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentSec = currentPositionMs / 1000
                        val durSec = durationMs / 1000
                        Text(
                            text = String.format("%02d:%02d / %02d:%02d", currentSec / 60, currentSec % 60, durSec / 60, durSec % 60),
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Replay
                            IconButton(onClick = {
                                mediaPlayer?.seekTo(0)
                                if (isPlaying == false) {
                                    mediaPlayer?.start()
                                    isPlaying = true
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Replay, contentDescription = "إعادة", tint = QuranGold)
                            }

                            // Play / Pause Master
                            Surface(
                                shape = CircleShape,
                                color = IslamicGreen,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clickable {
                                        if (mediaPlayer == null) {
                                            setupMediaPlayer()
                                        } else {
                                            mediaPlayer?.let { mp ->
                                                if (mp.isPlaying) {
                                                    mp.pause()
                                                    isPlaying = false
                                                } else {
                                                    mp.start()
                                                    isPlaying = true
                                                }
                                            }
                                        }
                                    }
                                    .testTag("wird_audio_play_pause")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل الاستماع",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        // Close button on card for quick cancel
                        Button(
                            onClick = onCloseScreen,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "إغلاق التذكير ✕",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. TABS: Tab 1 = قراءة الأذكار | Tab 2 = قراءة ما تيسر من القرآن
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = IslamicGreen,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = if (isEvening) "📿 قراءة أذكار المساء" else "📿 قراءة أذكار الصباح",
                            fontFamily = TajawalFontFamily,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "📖 قراءة ما تيسر من القرآن",
                            fontFamily = TajawalFontFamily,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Adhkar Reading list with interactive counters
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(adhkarList, key = { it.id }) { dhikr ->
                            WirdDhikrCard(dhikr = dhikr)
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onCloseScreen,
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("finish_wird_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Done, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تمّ إتمام الورد والحمد لله ✓",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
                1 -> {
                    // Quran Reading: Recommended morning/evening surahs (Al-Fatihah, Ayat Al-Kursi, Al-Kahf / Yasin / Al-Mulk, Al-Ikhlas, Al-Falaq, An-Nas)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = QuranGold.copy(alpha = 0.12f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "سُور وآيات مستحبة في هذا الورد المبارك (انقر لفتح المصحف مباشرة):",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = QuranGold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        val recommendedSurahs = listOf(
                            Triple(1, "سورة الفاتحة", "أم الكتاب والسبع المثاني والشفاء التام"),
                            Triple(2, "آية الكرسي (سورة البقرة)", "سيدة آي القرآن والحفظ من كل شيطان"),
                            Triple(if (isEvening) 67 else 18, if (isEvening) "سورة الملك" else "سورة الكهف", if (isEvening) "المنجية من عذاب القبر" else "نور ما بين الجمعتين وحصن الفتن"),
                            Triple(112, "سورة الإخلاص", "تعدل ثلث القرآن وبراءة من الشرك"),
                            Triple(113, "سورة الفلق", "الحفظ والوقاية من شر ما خلق والحاسدين"),
                            Triple(114, "سورة الناس", "الاستعاذة برب الناس من الوسواس الخناس")
                        )

                        items(recommendedSurahs) { (num, name, desc) ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToReader(num, 1) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(IslamicGreen.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$num",
                                                fontFamily = TajawalFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = IslamicGreen
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = name,
                                                fontFamily = TajawalFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = desc,
                                                fontFamily = TajawalFontFamily,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = IslamicGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "قراءة الآن 📖",
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = IslamicGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onCloseScreen,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "إغلاق شاشة الورد والعودة للرئيسية ✕",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WirdDhikrCard(
    dhikr: DhikrItem,
    modifier: Modifier = Modifier
) {
    var count by remember { mutableIntStateOf(0) }
    val isCompleted = count >= dhikr.targetCount

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) IslamicGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isCompleted) androidx.compose.ui.graphics.SolidColor(IslamicGreen)
            else androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (count < dhikr.targetCount) count++
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = dhikr.textArabic,
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (dhikr.virtueArabic.isNotBlank()) {
                Text(
                    text = "الفضل: ${dhikr.virtueArabic}",
                    fontFamily = TajawalFontFamily,
                    fontSize = 11.sp,
                    color = QuranGold
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) IslamicGreen else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isCompleted) "اكتمل الورد ✓" else "التكرار: $count / ${dhikr.targetCount}",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isCompleted) IslamicGreen else QuranGold.copy(alpha = 0.2f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "+1",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isCompleted) Color.White else QuranGold
                        )
                    }
                }
            }
        }
    }
}
