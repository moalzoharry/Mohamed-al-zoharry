package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import com.example.data.model.ShaarawyReflection
import com.example.data.model.ShaarawyTafsirData
import com.example.ui.components.ContentProtectionBadge
import com.example.ui.components.ContentProtectionWarningDialog
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShaarawyTafsirScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReader: (surahNumber: Int, ayahNumber: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var searchQuery by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<Int?>(1) }
    var showProtectionDialog by remember { mutableStateOf(false) }

    val filteredReflections = remember(searchQuery) {
        if (searchQuery.isBlank()) ShaarawyTafsirData.reflections
        else {
            val q = searchQuery.trim()
            ShaarawyTafsirData.reflections.filter {
                it.surahName.contains(q) ||
                it.title.contains(q) ||
                it.reflectionText.contains(q) ||
                it.goldenQuote.contains(q)
            }
        }
    }

    if (showProtectionDialog) {
        ContentProtectionWarningDialog(
            onDismissRequest = { showProtectionDialog = false },
            targetItemName = "خواطر وتفسير الشيخ الشعراوي"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "خواطر وتفسير الشيخ الشعراوي",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "إمام الدعاة فضيلة الشيخ محمد متولي الشعراوي",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = QuranGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("shaarawy_back_btn")) {
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
        modifier = modifier.testTag("shaarawy_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Intro Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0284C7).copy(alpha = 0.12f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0284C7).copy(alpha = 0.4f))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎙️", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "خواطر إيمانية وتدبرات بيانية",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0284C7)
                                )
                                Text(
                                    text = "«خواطري حول القرآن الكريم لا تعني تفسيراً للقرآن، وإنما هي هبات صفائية تخطر على قلب مؤمن في آية من كتاب الله»",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Search input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "ابحث في سور وخواطر الشيخ الشعراوي...",
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
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shaarawy_search_input")
                )
            }

            items(filteredReflections, key = { it.id }) { item ->
                ShaarawyItemCard(
                    reflection = item,
                    isExpanded = expandedId == item.id,
                    onToggleExpand = {
                        expandedId = if (expandedId == item.id) null else item.id
                    },
                    onOpenSurah = {
                        onNavigateToReader(item.surahNumber, 1)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun ShaarawyItemCard(
    reflection: ShaarawyReflection,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenSurah: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isExpanded)
                androidx.compose.ui.graphics.SolidColor(Color(0xFF0284C7))
            else
                androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand)
            .testTag("shaarawy_card_${reflection.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = reflection.surahName,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reflection.ayahRange,
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isExpanded) Color(0xFF0284C7) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isExpanded) "إغلاق ▲" else "قراءة الخواطر ▼",
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpanded) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = reflection.title,
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Golden Quote pill
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = QuranGold.copy(alpha = 0.12f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = QuranGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = reflection.goldenQuote,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = QuranGold
                    )
                }
            }

            // Expanded body
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reflection.reflectionText,
                            fontFamily = TajawalFontFamily,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Open in Quran Reader
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = IslamicGreen.copy(alpha = 0.15f),
                            modifier = Modifier.clickable(onClick = onOpenSurah)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = IslamicGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "فتح سورة ${reflection.surahName} بالمصحف",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = IslamicGreen
                                )
                            }
                        }

                        // Share & Copy
                        Row {
                            IconButton(onClick = {
                                val shareMsg = """
                                    ✦ خواطر الشيخ الشعراوي رحمه الله ✦
                                    ${reflection.title} - ${reflection.surahName} (${reflection.ayahRange})
                                    
                                    ${reflection.goldenQuote}
                                    
                                    ${reflection.reflectionText}
                                    
                                    تطبيق الفرقان بلس | Al-Furqan Plus
                                """.trimIndent()
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, shareMsg)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة خواطر الشعراوي"))
                            }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "مشاركة", tint = QuranGold)
                            }

                            IconButton(onClick = {
                                val clip = ClipData.newPlainText("خواطر الشعراوي", "${reflection.title}\n${reflection.reflectionText}")
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ الخواطر للمحفظة", Toast.LENGTH_SHORT).show()
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
