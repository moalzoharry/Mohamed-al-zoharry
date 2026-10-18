package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import com.example.ui.components.PeriodicDhikrReminderSheet
import com.example.ui.viewmodel.QuranViewModel
import com.example.adhkar.AdhkarCategory
import com.example.adhkar.AdhkarData
import com.example.adhkar.DhikrItem
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.IslamicGreenDark
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    onBack: () -> Unit,
    initialCategory: AdhkarCategory = AdhkarCategory.MORNING,
    viewModel: QuranViewModel? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showPeriodicDhikrSheet by remember { mutableStateOf(false) }
    val periodicDhikrSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories = AdhkarCategory.entries.toTypedArray()
    var selectedCategoryIndex by remember {
        mutableIntStateOf(categories.indexOf(initialCategory).coerceAtLeast(0))
    }
    val currentCategory = categories[selectedCategoryIndex]

    // Store per-category list of DhikrItems in memory with counts
    var adhkarMap by remember {
        mutableStateOf(
            categories.associateWith { cat ->
                AdhkarData.getAdhkarForCategory(cat)
            }
        )
    }

    val currentList = adhkarMap[currentCategory] ?: emptyList()
    val completedCount = currentList.count { it.isCompleted }
    val totalCount = currentList.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الأذكار وحصن المسلم",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("adhkar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                actions = {
                    // Reset current category
                    IconButton(
                        onClick = {
                            val reset = AdhkarData.getAdhkarForCategory(currentCategory)
                            adhkarMap = adhkarMap.toMutableMap().apply {
                                put(currentCategory, reset)
                            }
                            Toast.makeText(context, "تمت إعادة ضبط عدادات ${currentCategory.titleArabic}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("adhkar_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "إعادة ضبط العدادات",
                            tint = QuranGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("adhkar_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                edgePadding = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("adhkar_category_tabs")
            ) {
                categories.forEachIndexed { index, cat ->
                    val isSelected = selectedCategoryIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategoryIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cat.emoji,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.titleArabic,
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("adhkar_tab_${cat.id}")
                    )
                }
            }

            // Summary & Progress Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("adhkar_progress_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
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
                        Column {
                            Text(
                                text = currentCategory.titleArabic,
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = currentCategory.timeDescription,
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (completedCount == totalCount && totalCount > 0)
                                IslamicGreen.copy(alpha = 0.15f)
                            else
                                QuranGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$completedCount من $totalCount أذكار",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (completedCount == totalCount && totalCount > 0) IslamicGreen else QuranGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = IslamicGreen,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // List of Dhikr items
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(currentList, key = { _, item -> item.id }) { index, item ->
                    DhikrItemCard(
                        item = item,
                        index = index + 1,
                        onIncrement = {
                            if (item.currentCount < item.targetCount) {
                                triggerHaptic(context)
                                val updatedList = currentList.toMutableList()
                                updatedList[index] = item.copy(currentCount = item.currentCount + 1)
                                adhkarMap = adhkarMap.toMutableMap().apply {
                                    put(currentCategory, updatedList)
                                }
                            }
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Dhikr", item.textArabic)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الذكر الشريف", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun DhikrItemCard(
    item: DhikrItem,
    index: Int,
    onIncrement: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = item.isCompleted
    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) IslamicGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        label = "dhikrCardBorder"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dhikr_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                IslamicGreen.copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Number and Copy action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) IslamicGreen else QuranGold.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "$index",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = QuranGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (item.referenceArabic.isNotBlank()) {
                        Text(
                            text = item.referenceArabic,
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("copy_dhikr_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "نسخ الذكر",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dhikr Arabic Text
            Text(
                text = item.textArabic,
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                lineHeight = 30.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Virtue / Fadl
            if (item.virtueArabic.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.virtueArabic,
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Counter Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isCompleted) IslamicGreen else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isCompleted, onClick = onIncrement)
                    .testTag("dhikr_counter_button_${item.id}")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCompleted) "تم بحمد الله ✓" else "اضغط هنا للعد والتكرار",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "${item.currentCount} / ${item.targetCount}",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun triggerHaptic(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(45)
            }
        }
    } catch (_: Exception) {}
}
