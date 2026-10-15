package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.prayer.AdhanMuezzin
import com.example.prayer.PrayerSchedule
import com.example.prayer.PrayerType
import com.example.ui.components.AdhanIslamicBackground
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily
import kotlinx.coroutines.delay

data class AdhanSentence(
    val arabicWithTashkeel: String,
    val translationArabic: String,
    val isFajrOnly: Boolean = false
)

val FullAdhanSentences = listOf(
    AdhanSentence("اللهُ أَكْبَرُ، اللهُ أَكْبَرُ", "التكبير والتعظيم لله عز وجل"),
    AdhanSentence("اللهُ أَكْبَرُ، اللهُ أَكْبَرُ", "التكبير والتعظيم لله عز وجل"),
    AdhanSentence("أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللهُ", "شهادة التوحيد والإخلاص"),
    AdhanSentence("أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللهُ", "شهادة التوحيد والإخلاص"),
    AdhanSentence("أَشْهَدُ أَنَّ مُحَمَّدًا رَسُولُ اللهِ", "شهادة الرسالة والنبوة لخير البرية"),
    AdhanSentence("أَشْهَدُ أَنَّ مُحَمَّدًا رَسُولُ اللهِ", "شهادة الرسالة والنبوة لخير البرية"),
    AdhanSentence("حَيَّ عَلَى الصَّلَاةِ", "الدعوة إلى عمود الدين والصلة برب العالمين"),
    AdhanSentence("حَيَّ عَلَى الصَّلَاةِ", "الدعوة إلى عمود الدين والصلة برب العالمين"),
    AdhanSentence("حَيَّ عَلَى الْفَلَاحِ", "الدعوة إلى الفوز والنجاح وسعادة الدارين"),
    AdhanSentence("حَيَّ عَلَى الْفَلَاحِ", "الدعوة إلى الفوز والنجاح وسعادة الدارين"),
    AdhanSentence("الصَّلَاةُ خَيْرٌ مِنَ النَّوْمِ", "تثويب الفجر - الصلاة خير من لذة النوم ومتاع الدنيا", isFajrOnly = true),
    AdhanSentence("الصَّلَاةُ خَيْرٌ مِنَ النَّوْمِ", "تثويب الفجر - الصلاة خير من لذة النوم ومتاع الدنيا", isFajrOnly = true),
    AdhanSentence("اللهُ أَكْبَرُ، اللهُ أَكْبَرُ", "خاتمة الأذان بالتعظيم لله سبحانه"),
    AdhanSentence("لَا إِلٰهَ إِلَّا اللهُ", "كلمة الإخلاص والنجاة")
)

/**
 * Full-screen immersive Islamic Adhan Overlay.
 * Includes:
 * 1. Prayer-specific atmospheric Islamic background (Fajr, Dhuhr, Asr, Maghrib, Isha)
 * 2. Top bar with clear close/stop button ("غلق البرنامج من فوق")
 * 3. Arabic Adhan with Tashkeel, rhythmic sentence progression synchronized with reciting
 * 4. Action buttons: "🕋 تحديد القبلة", "🤲 بدء الصلاة", and "✕ إيقاف الأذان"
 */
@Composable
fun AdhanLiveOverlayScreen(
    prayerType: PrayerType,
    schedule: PrayerSchedule,
    muezzinName: String,
    isPlaying: Boolean,
    onStopAdhan: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onStartPrayer: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onStopAdhan()
        onDismiss()
    }

    val sentences = remember(prayerType) {
        if (prayerType == PrayerType.FAJR) {
            FullAdhanSentences
        } else {
            FullAdhanSentences.filter { !it.isFajrOnly }
        }
    }

    // Progression of adhan phrases during playback
    var activeSentenceIndex by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    LaunchedEffect(isPlaying, sentences.size) {
        if (isPlaying) {
            // Cycle through sentences smoothly
            while (true) {
                delay(7000L) // Average phrase length in adhan
                activeSentenceIndex = (activeSentenceIndex + 1) % sentences.size
                listState.animateScrollToItem(activeSentenceIndex)
            }
        }
    }

    var showPrayerCompanionDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            onStopAdhan()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        AdhanIslamicBackground(
            prayerType = prayerType,
            modifier = modifier
                .fillMaxSize()
                .testTag("adhan_live_overlay_screen")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TOP HEADER WITH CLOSE BUTTON (غلق من فوق)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button with high-contrast pill
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp),
                        modifier = Modifier
                            .testTag("adhan_top_close_button")
                            .clickable {
                                onStopAdhan()
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق الأذان",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إغلاق",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }

                    // City and Time
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = schedule.cityName,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "أذان ${prayerType.titleArabic}",
                            fontFamily = TajawalFontFamily,
                            fontSize = 12.sp,
                            color = QuranGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. HERO BADGE: PRAYER NAME & MUEZZIN
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(QuranGold, IslamicGreen)),
                        width = 1.5.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🕌", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حان الآن موعد أذان ${prayerType.titleArabic}",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "بصوت: $muezzinName",
                            fontFamily = TajawalFontFamily,
                            fontSize = 13.sp,
                            color = QuranGold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "«أقم صلاتك تنعم بحياتك • وتطمئن بها القلوب»",
                            fontFamily = TajawalFontFamily,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. TASHKEEL ADHAN CHANT PHRASES LIST
                Text(
                    text = "كلمات الأذان المباركة بالتشكيل",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = QuranGold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    itemsIndexed(sentences) { index, sentence ->
                        val isActive = index == activeSentenceIndex && isPlaying

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isActive) QuranGold.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.35f),
                            border = if (isActive) CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(QuranGold, Color.White)),
                                width = 1.8.dp
                            ) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { activeSentenceIndex = index }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sentence.arabicWithTashkeel,
                                        fontFamily = AmiriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isActive) 22.sp else 18.sp,
                                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Right
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = sentence.translationArabic,
                                        fontFamily = TajawalFontFamily,
                                        fontSize = 11.sp,
                                        color = if (isActive) QuranGold else Color.White.copy(alpha = 0.6f)
                                    )
                                }

                                if (isActive) {
                                    Surface(
                                        shape = CircleShape,
                                        color = QuranGold
                                    ) {
                                        Text(
                                            text = "يردد الآن",
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. ACTION BUTTONS: QIBLA, START PRAYER, STOP ADHAN
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Qibla Button (تحديد القبلة)
                        Button(
                            onClick = {
                                onStopAdhan()
                                onDismiss()
                                onNavigateToQibla()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IslamicGreen
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("adhan_overlay_qibla_button")
                        ) {
                            Text(text = "🕋", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تحديد القبلة",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        // 2. Start Prayer Button (بدء الصلاة)
                        Button(
                            onClick = {
                                showPrayerCompanionDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = QuranGold
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("adhan_overlay_start_prayer_button")
                        ) {
                            Text(text = "🤲", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "بدء الصلاة",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }

                    // 3. Stop Adhan / Cancel playback
                    OutlinedButton(
                        onClick = {
                            onStopAdhan()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("adhan_overlay_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "إيقاف الأذان",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إيقاف صوت الأذان وإغلاق الشاشة",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Dialog for "بدء الصلاة"
    if (showPrayerCompanionDialog) {
        PrayerCompanionDialog(
            prayerType = prayerType,
            onDismiss = { showPrayerCompanionDialog = false },
            onConfirmCompleted = {
                showPrayerCompanionDialog = false
                onStartPrayer(prayerType)
                onStopAdhan()
                onDismiss()
            }
        )
    }
}

/**
 * Prayer guidance modal shown when user presses "بدء الصلاة"
 */
@Composable
fun PrayerCompanionDialog(
    prayerType: PrayerType,
    onDismiss: () -> Unit,
    onConfirmCompleted: () -> Unit
) {
    val rakahsInfo = when (prayerType) {
        PrayerType.FAJR -> "ركعتان سنة قبلية مؤكدة + ركعتان فرض جهريتان"
        PrayerType.DHUHR -> "٤ ركعات سنة قبلية + ٤ ركعات فرض سرية + ركعتان سنة بعدية"
        PrayerType.ASR -> "٤ ركعات فرض سرية (ويستحب ٤ ركعات قبلها سنة غير مؤكدة)"
        PrayerType.MAGHRIB -> "٣ ركعات فرض (الأولى والثانية جهراً والثالثة سراً) + ركعتان سنة بعدية"
        PrayerType.ISHA -> "٤ ركعات فرض (الأولى والثانية جهراً) + ركعتان سنة بعدية + الشفع والوتر"
        else -> "صلاة نافلة وطاعة لله سبحانه وتعالى"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                    Text(
                        text = "دليل أداء صلاة ${prayerType.titleArabic}",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🕌 عدد الركعات والسنة:",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rakahsInfo,
                            fontFamily = TajawalFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🤲 دعاء الاستفتاح المستحب:",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = QuranGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "«سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَى جَدُّكَ، وَلَا إِلَهَ غَيْرُكَ»",
                            fontFamily = AmiriFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onConfirmCompleted,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تقبل الله • التوجه لأذكار ما بعد الصلاة والسبحة",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
