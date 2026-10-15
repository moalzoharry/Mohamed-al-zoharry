package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.IslamicGreenDark
import com.example.ui.theme.IslamicGreenLight
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily
import java.text.NumberFormat
import java.util.Locale

val presetTasbihat = listOf(
    "سُبْحَانَ اللَّهِ",
    "الْحَمْدُ لِلَّهِ",
    "لَا إِلَٰهَ إِلَّا اللَّهُ",
    "اللَّهُ أَكْبَرُ",
    "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
    "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
    "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
    "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
    "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
    "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var selectedDhikr by remember { mutableStateOf(presetTasbihat[0]) }
    var customDhikrText by remember { mutableStateOf("") }
    var showCustomDhikrDialog by remember { mutableStateOf(false) }

    // Counter state
    var count by remember { mutableIntStateOf(0) }
    var totalCount by remember { mutableIntStateOf(0) }
    var cyclesCompleted by remember { mutableIntStateOf(0) }

    // Target configuration: 0 means Open/Unlimited (مفتوح بدون حد مهما كان العدد)
    var targetLimit by remember { mutableIntStateOf(0) } // 0 = unlimited, 33, 100, or custom
    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var customTargetInput by remember { mutableStateOf("500") }

    var isVibrationEnabled by remember { mutableStateOf(true) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "tasbihScale"
    )

    fun onIncrement() {
        val next = count + 1
        totalCount += 1

        if (targetLimit > 0) {
            if (next >= targetLimit) {
                count = 0
                cyclesCompleted += 1
                if (isVibrationEnabled) triggerLongVibration(context)
            } else {
                count = next
                if (isVibrationEnabled) triggerShortVibration(context)
            }
        } else {
            // Open unlimited counter mode!
            count = next
            if (isVibrationEnabled) triggerShortVibration(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "السبحة الإلكترونية",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tasbih_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                actions = {
                    // Toggle vibration
                    IconButton(
                        onClick = { isVibrationEnabled = !isVibrationEnabled },
                        modifier = Modifier.testTag("tasbih_vibration_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = if (isVibrationEnabled) "الاهتزاز مفعل" else "الاهتزاز معطل",
                            tint = if (isVibrationEnabled) QuranGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Reset with confirmation
                    IconButton(
                        onClick = {
                            if (count > 0 || totalCount > 0) {
                                showResetConfirmDialog = true
                            }
                        },
                        modifier = Modifier.testTag("tasbih_reset_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تصفير العداد",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("tasbih_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Dhikr selection carousel
            Text(
                text = "اختر صيغة التسبيح أو اكتب ذكراً مخصصاً:",
                fontFamily = TajawalFontFamily,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                textAlign = TextAlign.Right
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(presetTasbihat) { _, dhikr ->
                    val isSelected = selectedDhikr == dhikr
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDhikr = dhikr },
                        label = {
                            Text(
                                text = dhikr,
                                fontFamily = AmiriFontFamily,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicGreen.copy(alpha = 0.2f),
                            selectedLabelColor = IslamicGreen
                        )
                    )
                }

                item {
                    OutlinedButton(
                        onClick = { showCustomDhikrDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("add_custom_dhikr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ذكر مخصص",
                            fontFamily = TajawalFontFamily,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Active Dhikr Card Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("active_dhikr_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedDhikr,
                        fontFamily = AmiriFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Target badge & Mode selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (targetLimit == 0) IslamicGreen.copy(alpha = 0.15f) else QuranGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (targetLimit == 0) "الوضع: عداد مفتوح بدون حد ♾️" else "الهدف: $targetLimit تسبيحة",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (targetLimit == 0) IslamicGreen else QuranGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (targetLimit > 0 && cyclesCompleted > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "الدورات: $cyclesCompleted",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Target Mode Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TargetChip(
                    label = "مفتوح ♾️",
                    isSelected = targetLimit == 0,
                    onClick = { targetLimit = 0 }
                )
                TargetChip(
                    label = "33",
                    isSelected = targetLimit == 33,
                    onClick = { targetLimit = 33 }
                )
                TargetChip(
                    label = "100",
                    isSelected = targetLimit == 100,
                    onClick = { targetLimit = 100 }
                )
                TargetChip(
                    label = "تحديد هدف...",
                    isSelected = targetLimit !in listOf(0, 33, 100),
                    onClick = { showCustomTargetDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. BIG TACTILE BEAD / COUNTER CIRCLE
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                IslamicGreenLight,
                                IslamicGreen,
                                IslamicGreenDark
                            )
                        )
                    )
                    .border(
                        width = 6.dp,
                        brush = Brush.sweepGradient(listOf(QuranGold, IslamicGreenLight, QuranGold)),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onIncrement() }
                    )
                    .testTag("tasbih_main_tap_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "اضغط للتسبيح",
                        fontFamily = TajawalFontFamily,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = NumberFormat.getNumberInstance(Locale.US).format(count),
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (count > 9999) 42.sp else 54.sp,
                        color = Color.White
                    )

                    if (targetLimit > 0) {
                        Text(
                            text = "من $targetLimit",
                            fontFamily = TajawalFontFamily,
                            fontSize = 15.sp,
                            color = QuranGold
                        )
                    } else {
                        Text(
                            text = "سبّح بحمد ربك",
                            fontFamily = AmiriFontFamily,
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Controls: Decrement and Reset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrement (-1)
                IconButton(
                    onClick = {
                        if (count > 0) {
                            count -= 1
                            if (totalCount > 0) totalCount -= 1
                            if (isVibrationEnabled) triggerShortVibration(context)
                        }
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("tasbih_decrement_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "إنقاص واحدة (-1)",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Total statistics
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "إجمالي التسبيح الكلي",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormat.getNumberInstance(Locale.US).format(totalCount),
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = IslamicGreen
                    )
                }

                // Reset button
                IconButton(
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("tasbih_reset_circular_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تصفير",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Information hint
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Text(
                    text = "💡 يمكنك النقر على الدائرة الخضراء الكبيرة للتسبيح بسلاسة، ويعمل العداد المفتوح لأي عدد تريده دون قيود.",
                    fontFamily = TajawalFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Dialog: Custom Dhikr
    if (showCustomDhikrDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDhikrDialog = false },
            title = {
                Text(
                    text = "كتابة ذكر مخصص",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "اكتب الذكر أو الدعاء الذي تود التسبيح به:",
                        fontFamily = TajawalFontFamily,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customDhikrText,
                        onValueChange = { customDhikrText = it },
                        placeholder = { Text("مثال: رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customDhikrText.isNotBlank()) {
                            selectedDhikr = customDhikrText.trim()
                        }
                        showCustomDhikrDialog = false
                    }
                ) {
                    Text("حفظ واختيار")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDhikrDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Dialog: Custom Target
    if (showCustomTargetDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTargetDialog = false },
            title = {
                Text(
                    text = "تحديد هدف التسبيح",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "أدخل العدد المستهدف (مثال: 500 أو 1000 أو أي عدد):",
                        fontFamily = TajawalFontFamily,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customTargetInput,
                        onValueChange = { customTargetInput = it.filter { char -> char.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = customTargetInput.toIntOrNull() ?: 0
                        if (num > 0) {
                            targetLimit = num
                            count = 0
                        }
                        showCustomTargetDialog = false
                    }
                ) {
                    Text("تعيين")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTargetDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Dialog: Reset Confirmation
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = "تأكيد تصفير العداد",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من تصفير العداد الحالي؟\nالعدد الحالي: $count\nالإجمالي الكلي: $totalCount",
                    fontFamily = TajawalFontFamily,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        count = 0
                        totalCount = 0
                        cyclesCompleted = 0
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تصفير العداد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun TargetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) IslamicGreen else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontFamily = TajawalFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun triggerShortVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(35)
            }
        }
    } catch (_: Exception) {}
}

private fun triggerLongVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 150), -1))
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 150), -1))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(200)
            }
        }
    } catch (_: Exception) {}
}
