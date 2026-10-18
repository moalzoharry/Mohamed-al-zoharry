package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DhikrPresets
import com.example.data.model.PeriodicDhikrReminder
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PeriodicDhikrReminderSheet(
    reminder: PeriodicDhikrReminder,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onSaveReminder: (PeriodicDhikrReminder) -> Unit,
    onTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isEnabled by remember(reminder) { mutableStateOf(reminder.isEnabled) }
    var intervalMinutes by remember(reminder) { mutableStateOf(reminder.intervalMinutes) }
    var sliderValue by remember(reminder) { mutableFloatStateOf(reminder.intervalMinutes.toFloat()) }
    var selectedDhikrText by remember(reminder) { mutableStateOf(reminder.selectedDhikrText) }
    var isRandomRotation by remember(reminder) { mutableStateOf(reminder.isRandomPresetRotation) }
    var soundEnabled by remember(reminder) { mutableStateOf(reminder.soundEnabled) }
    var vibrateEnabled by remember(reminder) { mutableStateOf(reminder.vibrateEnabled) }

    val presetIntervals = listOf(1, 2, 3, 5, 10, 15, 30, 60, 120)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("periodic_dhikr_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(QuranGold.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📿", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "منبه الأذكار والصلاة على النبي ﷺ",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تذكير تلقائي دوري بالذكر الذي تختاره",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = QuranGold
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. MASTER TOGGLE SWITCH CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEnabled) IslamicGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = if (isEnabled) androidx.compose.ui.graphics.SolidColor(IslamicGreen) else androidx.compose.ui.graphics.SolidColor(Color.Transparent)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (isEnabled) IslamicGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "تفعيل التنبيه الدوري التلقائي",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEnabled) "يعمل الآن: ${PeriodicDhikrReminder(intervalMinutes = intervalMinutes).formatIntervalArabic()}" else "التنبيه معطل حالياً",
                                fontFamily = TajawalFontFamily,
                                fontSize = 12.sp,
                                color = if (isEnabled) IslamicGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        modifier = Modifier.testTag("periodic_dhikr_master_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. INTERVAL SELECTION CARD (تقليل وتطويل المدة)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = QuranGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تكرار التنبيه كل:",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = QuranGold.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = PeriodicDhikrReminder(intervalMinutes = intervalMinutes).formatIntervalArabic(),
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = QuranGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset interval quick chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetIntervals.forEach { mins ->
                            val isSelected = intervalMinutes == mins
                            val chipLabel = when (mins) {
                                1 -> "دقيقة ⚡"
                                2 -> "دقيقتان"
                                3 -> "3 دقائق"
                                5 -> "5 دقائق ⭐"
                                10 -> "10 دقائق"
                                15 -> "15 دقيقة"
                                30 -> "نصف ساعة"
                                60 -> "ساعة"
                                120 -> "ساعتان"
                                else -> "$mins دقيقة"
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) QuranGold else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.clickable {
                                    intervalMinutes = mins
                                    sliderValue = mins.toFloat()
                                }
                            ) {
                                Text(
                                    text = chipLabel,
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Slider for granular fine-tuning (1 to 180 mins)
                    Text(
                        text = "ضبط دقيق للمدة (من دقيقة إلى 3 ساعات):",
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = it
                            intervalMinutes = it.toInt().coerceAtLeast(1)
                        },
                        valueRange = 1f..180f,
                        steps = 179,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. DHIKR TEXT SELECTION & CUSTOM INPUT
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Random rotation switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                tint = IslamicGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "تبديل الذكر تلقائياً في كل مرة",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تغيير الذكر عشوائياً بين روائع الأذكار النبوية في كل إشعار",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isRandomRotation,
                            onCheckedChange = { isRandomRotation = it }
                        )
                    }

                    AnimatedVisibility(visible = !isRandomRotation) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            Text(
                                text = "اختر من الأذكار الجاهزة:",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DhikrPresets.defaultList.forEach { preset ->
                                    val isChosen = selectedDhikrText == preset
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isChosen) QuranGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        border = if (isChosen) CardDefaults.outlinedCardBorder().copy(width = 1.dp) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedDhikrText = preset }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = preset,
                                                fontFamily = AmiriFontFamily,
                                                fontSize = 14.sp,
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isChosen) QuranGold else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isChosen) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = QuranGold,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Custom Text Input field
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = IslamicGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "أو اكتب ذكرك أو دعاءك المخصص:",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = selectedDhikrText,
                                onValueChange = { selectedDhikrText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_dhikr_input"),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                ),
                                placeholder = {
                                    Text(
                                        text = "مثال: صلّ على الحبيب ﷺ، سبحان الله، استغفر الله العظيم...",
                                        fontFamily = TajawalFontFamily,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = QuranGold,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. SOUND & VIBRATION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = QuranGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "صوت التنبيه", fontFamily = TajawalFontFamily, fontSize = 13.sp)
                        }
                        Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = QuranGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "الاهتزاز", fontFamily = TajawalFontFamily, fontSize = 13.sp)
                        }
                        Switch(checked = vibrateEnabled, onCheckedChange = { vibrateEnabled = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Test & Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val testReminder = PeriodicDhikrReminder(
                            isEnabled = true,
                            intervalMinutes = intervalMinutes,
                            selectedDhikrText = selectedDhikrText.ifBlank { "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى سَيِّدِنَا مُحَمَّدٍ ﷺ" },
                            isRandomPresetRotation = isRandomRotation,
                            soundEnabled = soundEnabled,
                            vibrateEnabled = vibrateEnabled
                        )
                        onTestNotification()
                        Toast.makeText(context, "تم إرسال إشعار تجريبي فوري 🔔", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "تجربة التنبيه 🔔", fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val updated = PeriodicDhikrReminder(
                            isEnabled = isEnabled,
                            intervalMinutes = intervalMinutes,
                            selectedDhikrText = selectedDhikrText.ifBlank { "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى سَيِّدِنَا مُحَمَّدٍ ﷺ" },
                            isRandomPresetRotation = isRandomRotation,
                            soundEnabled = soundEnabled,
                            vibrateEnabled = vibrateEnabled
                        )
                        onSaveReminder(updated)
                        val statusMsg = if (updated.isEnabled) {
                            "تم تفعيل المنبه الدوري (${updated.formatIntervalArabic()}) بنجاح ✨"
                        } else {
                            "تم حفظ الإعدادات وإيقاف التنبيه"
                        }
                        Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QuranGold),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "حفظ وتفعيل ✓",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
