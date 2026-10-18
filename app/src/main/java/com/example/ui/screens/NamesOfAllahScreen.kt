package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NameOfAllah
import com.example.data.model.NamesOfAllahData
import com.example.ui.components.ContentProtectionBadge
import com.example.ui.components.ContentProtectionWarningDialog
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NamesOfAllahScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var expandedNameId by remember { mutableStateOf<Int?>(null) }
    var showProtectionDialog by remember { mutableStateOf(false) }

    val categories = listOf("الكل", "الرحمة والمغفرة", "الجلال والكمال", "الرزق والعطاء", "القدرة والخلق", "العلم والحكمة", "الأمن والرعاية", "الهداية والنور")

    val filteredNames = remember(searchQuery, selectedCategory) {
        NamesOfAllahData.names.filter { item ->
            val matchesCategory = selectedCategory == "الكل" || item.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                item.nameArabic.contains(searchQuery.trim()) ||
                item.meaning.contains(searchQuery.trim()) ||
                item.spiritualBenefit.contains(searchQuery.trim())
            matchesCategory && matchesSearch
        }
    }

    if (showProtectionDialog) {
        ContentProtectionWarningDialog(
            onDismissRequest = { showProtectionDialog = false },
            targetItemName = "أسماء الله الحسنى وشروحاتها"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "أسماء الله الحسنى ومعانيها",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "٩٩ اسماً مع الشرح والفوائد وثمرات الدعاء",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = QuranGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("names_of_allah_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    ContentProtectionBadge(
                        onClick = { showProtectionDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("names_of_allah_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Intro Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = IslamicGreen.copy(alpha = 0.10f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(IslamicGreen.copy(alpha = 0.35f))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "﴿وَلِلَّهِ الْأَسْمَاءُ الْحُسْنَىٰ فَادْعُوهُ بِهَا﴾",
                            fontFamily = AmiriFontFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreen,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "قال رسول الله ﷺ: «إِنَّ لِلَّهِ تِسْعَةً وَتِسْعِينَ اسْمًا، مِائَةً إِلَّا وَاحِدًا، مَنْ أَحْصَاهَا دَخَلَ الْجَنَّةَ» [متفق عليه]",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "ابحث عن اسم، معنى، أو فائدة روحية...",
                            fontFamily = TajawalFontFamily,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = QuranGold)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("names_search_input")
                )
            }

            // Categories Filter Chips
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) IslamicGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontFamily = TajawalFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Count summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عدد الأسماء المعروضة: ${filteredNames.size} من ٩٩",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "انقر على الاسم للتفاصيل والتسبيح 📿",
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        color = QuranGold
                    )
                }
            }

            // Names List
            items(filteredNames, key = { it.id }) { item ->
                NameOfAllahCard(
                    name = item,
                    isExpanded = expandedNameId == item.id,
                    onToggleExpand = {
                        expandedNameId = if (expandedNameId == item.id) null else item.id
                    },
                    onAttemptEdit = { showProtectionDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun NameOfAllahCard(
    name: NameOfAllah,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAttemptEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dhikrCount by remember { mutableIntStateOf(0) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isExpanded)
                Brush.horizontalGradient(listOf(IslamicGreen, QuranGold))
            else
                androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand)
            .testTag("name_card_${name.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Number circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(QuranGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${name.id}",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = QuranGold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = name.nameArabic,
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = IslamicGreen
                        )
                        Text(
                            text = "${name.transliteration} • ${name.category}",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isExpanded) IslamicGreen else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isExpanded) "إغلاق ▲" else "شرح وفائدة ▼",
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpanded) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Expanded Details
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    // 1. Meaning
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📖 الشرح والمعنى الإيماني:",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name.meaning,
                                fontFamily = TajawalFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Spiritual Benefit
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = QuranGold.copy(alpha = 0.10f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✨ الفائدة الروحية وثمرة الدعاء به:",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = QuranGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name.spiritualBenefit,
                                fontFamily = TajawalFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Evidence
                    Text(
                        text = "الشاهد: ${name.quranEvidence}",
                        fontFamily = AmiriFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive Dhikr Counter & Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Tasbih Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IslamicGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGreen),
                            modifier = Modifier.clickable { dhikrCount++ }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = IslamicGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ذكر وتسبيح ($dhikrCount)",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = IslamicGreen
                                )
                            }
                        }

                        // Copy & Share & Edit warning
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val shareText = """
                                    ✦ ${name.nameArabic} (${name.transliteration}) ✦
                                    المعنى: ${name.meaning}
                                    الفائدة وثمرة الدعاء: ${name.spiritualBenefit}
                                    الشاهد: ${name.quranEvidence}
                                    
                                    تطبيق الفرقان بلس | Al-Furqan Plus
                                """.trimIndent()
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة اسم الله الحسنى"))
                            }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "مشاركة", tint = QuranGold)
                            }

                            IconButton(onClick = {
                                val clip = ClipData.newPlainText("اسم الله الحسنى", "${name.nameArabic}\n${name.meaning}\n${name.spiritualBenefit}")
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ الاسم والشرح للمحفظة", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
