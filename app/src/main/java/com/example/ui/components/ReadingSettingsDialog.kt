package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutoNightModeOption
import com.example.data.model.QuranReciter
import com.example.data.model.ReadingMode
import com.example.data.model.ReadingSettings
import com.example.data.model.ReadingTheme
import com.example.data.model.RepeatMode
import com.example.data.model.TafsirSource
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingSettingsBottomSheet(
    settings: ReadingSettings,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onSaveSettings: (ReadingSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var quranFontSize by remember(settings) { mutableFloatStateOf(settings.quranFontSize) }
    var tafsirFontSize by remember(settings) { mutableFloatStateOf(settings.tafsirFontSize) }
    var selectedTheme by remember(settings) { mutableStateOf(settings.theme) }
    var previousDayTheme by remember(settings) { mutableStateOf(settings.previousDayTheme) }
    var selectedMode by remember(settings) { mutableStateOf(settings.readingMode) }
    var showTashkeel by remember(settings) { mutableStateOf(settings.showTashkeel) }
    var selectedTafsir by remember(settings) { mutableStateOf(settings.selectedTafsir) }
    var selectedReciter by remember(settings) { mutableStateOf(settings.selectedReciter) }
    var repeatMode by remember(settings) { mutableStateOf(settings.repeatMode) }
    var autoScrollAudio by remember(settings) { mutableStateOf(settings.autoScrollAudio) }
    var autoNightMode by remember(settings) { mutableStateOf(settings.autoNightMode) }
    var eyeComfortWarmFilter by remember(settings) { mutableStateOf(settings.eyeComfortWarmFilter) }

    fun notifySave(
        theme: ReadingTheme = selectedTheme,
        prevTheme: ReadingTheme = previousDayTheme,
        qSize: Float = quranFontSize,
        tSize: Float = tafsirFontSize,
        mode: ReadingMode = selectedMode,
        tashkeel: Boolean = showTashkeel,
        tafsir: TafsirSource = selectedTafsir,
        reciter: QuranReciter = selectedReciter,
        repeat: RepeatMode = repeatMode,
        autoScroll: Boolean = autoScrollAudio,
        nightModeOpt: AutoNightModeOption = autoNightMode,
        eyeComfort: Boolean = eyeComfortWarmFilter
    ) {
        onSaveSettings(
            settings.copy(
                theme = theme,
                previousDayTheme = prevTheme,
                quranFontSize = qSize,
                tafsirFontSize = tSize,
                readingMode = mode,
                showTashkeel = tashkeel,
                selectedTafsir = tafsir,
                selectedReciter = reciter,
                repeatMode = repeat,
                autoScrollAudio = autoScroll,
                autoNightMode = nightModeOpt,
                eyeComfortWarmFilter = eyeComfort
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("reading_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }

                Text(
                    text = "إعدادات القراءة والوضع الليلي",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. LIVE PREVIEW CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(selectedTheme.surfaceColorHex)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (eyeComfortWarmFilter) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color(0xFFE89A3C).copy(alpha = 0.08f))
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "معاينة الخط والمظهر المختار",
                                fontFamily = TajawalFontFamily,
                                fontSize = 12.sp,
                                color = Color(selectedTheme.secondaryTextHex)
                            )
                            if (eyeComfortWarmFilter) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• مرشح راحة العين مفعل 🛡️",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 11.sp,
                                    color = QuranGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showTashkeel)
                                "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿١﴾"
                            else
                                "الحمد لله رب العالمين ﴿١﴾",
                            fontFamily = AmiriFontFamily,
                            fontSize = quranFontSize.sp,
                            lineHeight = (quranFontSize * 1.8f).sp,
                            color = Color(selectedTheme.textColorHex),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "معاينة حجم نص التفسير (${tafsirFontSize.toInt()} نقطة)",
                            fontFamily = AmiriFontFamily,
                            fontSize = tafsirFontSize.sp,
                            color = Color(selectedTheme.secondaryTextHex),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. NIGHT READING & EYE STRAIN REDUCTION SECTION (NEW FEATURE)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("night_reading_comfort_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Quick Night Mode Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightlightRound,
                                contentDescription = null,
                                tint = QuranGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "الوضع الليلي (شاشة داكنة OLED)",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تقليل السطوع والوهج لتلاوة مريحة في الظلام",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = selectedTheme.isDark,
                            onCheckedChange = { isNight ->
                                if (isNight) {
                                    if (!selectedTheme.isDark) previousDayTheme = selectedTheme
                                    selectedTheme = ReadingTheme.NIGHT
                                    notifySave(theme = ReadingTheme.NIGHT, prevTheme = previousDayTheme)
                                } else {
                                    val dayTheme = if (!previousDayTheme.isDark) previousDayTheme else ReadingTheme.PARCHMENT
                                    selectedTheme = dayTheme
                                    notifySave(theme = dayTheme)
                                }
                            },
                            modifier = Modifier.testTag("quick_night_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Eye Comfort Warm Blue-Light Filter Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveRedEye,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "مرشح راحة العين (الضوء الدافئ)",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تقليل انبعاثات الضوء الأزرق وإجهاد القرنية بالليل",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = eyeComfortWarmFilter,
                            onCheckedChange = {
                                eyeComfortWarmFilter = it
                                notifySave(eyeComfort = it)
                            },
                            modifier = Modifier.testTag("eye_comfort_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto Night Mode Trigger Options
                    Text(
                        text = "التشغيل التلقائي للوضع الليلي:",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AutoNightModeOption.values().forEach { option ->
                            val isSelected = autoNightMode == option
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auto_night_option_${option.name}")
                                    .clickable {
                                        autoNightMode = option
                                        notifySave(nightModeOpt = option)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = when (option) {
                                                AutoNightModeOption.AUTO_TIME -> Icons.Default.Schedule
                                                AutoNightModeOption.AUTO_LIGHT_SENSOR -> Icons.Default.Sensors
                                                AutoNightModeOption.FOLLOW_SYSTEM -> Icons.Default.SettingsBrightness
                                                AutoNightModeOption.DISABLED -> Icons.Default.Close
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = option.titleArabic,
                                                fontFamily = TajawalFontFamily,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = option.subtitleArabic,
                                                fontFamily = TajawalFontFamily,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. BACKGROUND COLOR / THEME CHOOSER
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ألوان الخلفية والمظهر",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Theme Swatches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReadingTheme.values().forEach { theme ->
                    val isSelected = selectedTheme == theme
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .testTag("theme_swatch_${theme.name}")
                            .clickable {
                                selectedTheme = theme
                                if (!theme.isDark) {
                                    previousDayTheme = theme
                                }
                                notifySave(theme = theme, prevTheme = previousDayTheme)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(theme.surfaceColorHex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.5.dp,
                                    color = if (isSelected) QuranGold else Color(theme.borderHex),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "محدد",
                                    tint = if (theme.isDark) Color.White else Color(0xFF1E2229),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = theme.titleArabic.take(7),
                            fontFamily = TajawalFontFamily,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. QURAN FONT SIZE SLIDER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "حجم خط الآيات القرآنية",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${quranFontSize.toInt()} pt",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = quranFontSize,
                onValueChange = {
                    quranFontSize = it
                    notifySave(qSize = it)
                },
                valueRange = 18f..40f,
                steps = 10,
                modifier = Modifier.testTag("quran_font_size_slider")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. TAFSIR FONT SIZE SLIDER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "حجم خط التفسير (الجلالين والميسر)",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${tafsirFontSize.toInt()} pt",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = tafsirFontSize,
                onValueChange = {
                    tafsirFontSize = it
                    notifySave(tSize = it)
                },
                valueRange = 13f..24f,
                steps = 10,
                modifier = Modifier.testTag("tafsir_font_size_slider")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. TASHKEEL SWITCH
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "إظهار التشكيل والحركات",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "عرض الحركات التشكيلية على الكلمات القرآنية",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = showTashkeel,
                    onCheckedChange = {
                        showTashkeel = it
                        notifySave(tashkeel = it)
                    },
                    modifier = Modifier.testTag("tashkeel_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7. DEFAULT TAFSIR SOURCE
            Text(
                text = "التفسير الافتراضي المفضل",
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TafsirSource.values().forEach { source ->
                    val isSelected = selectedTafsir == source
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTafsir = source
                                notifySave(tafsir = source)
                            }
                    ) {
                        Text(
                            text = source.title,
                            fontFamily = TajawalFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 8. AUTO SCROLL AUDIO SWITCH
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "التمرير التلقائي أثناء الاستماع",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "متابعة الآية الجارية آليًا عند تشغيل التلاوة",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = autoScrollAudio,
                    onCheckedChange = {
                        autoScrollAudio = it
                        notifySave(autoScroll = it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 9. PREFERRED RECITER SELECTOR
            Text(
                text = "القارئ المفضل لتلاوة الآيات",
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuranReciter.values().forEach { reciter ->
                    val isSelected = selectedReciter == reciter
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedReciter = reciter
                                notifySave(reciter = reciter)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = reciter.nameArabic,
                                fontFamily = TajawalFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 10. VERSE REPETITION (تكرار الآيات للحفظ)
            Text(
                text = "تكرار تلاوة الآيات (للحفظ والمراجعة)",
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RepeatMode.values().forEach { mode ->
                    val isSelected = repeatMode == mode
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                repeatMode = mode
                                notifySave(repeat = mode)
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = mode.badge,
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = mode.labelArabic.take(6),
                                fontFamily = TajawalFontFamily,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save / Done Button
            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_settings_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "تم وحفظ الإعدادات",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
