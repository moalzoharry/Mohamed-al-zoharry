package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Ayah
import com.example.data.model.ReadingSettings
import com.example.data.model.TafsirSource
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirModalBottomSheet(
    ayah: Ayah,
    surahName: String,
    settings: ReadingSettings,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember {
        mutableIntStateOf(
            when (settings.selectedTafsir) {
                TafsirSource.JALALAYN -> 0
                TafsirSource.MUYASSAR -> 1
                TafsirSource.SHAARAWI -> 2
            }
        )
    }

    val tabs = listOf("تفسير الجلالين", "التفسير الميسر", "خواطر الشعراوي", "مقارنة شاملة")

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("tafsir_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sheet Header with Close Button and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "تفسير سورة $surahName : آية ${ayah.ayahNumber}",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Quick Font Size Adjuster Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                if (settings.tafsirFontSize > 12f) onFontSizeChange(settings.tafsirFontSize - 1f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "A-", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                if (settings.tafsirFontSize < 24f) onFontSizeChange(settings.tafsirFontSize + 1f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "A+", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ayah Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "﴿ ${ayah.textWithTashkeel} ﴾",
                        fontFamily = AmiriFontFamily,
                        fontSize = (settings.quranFontSize * 0.85f).sp,
                        lineHeight = (settings.quranFontSize * 1.5f).sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs for Jalalayn / Muyassar / Compare
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontFamily = TajawalFontFamily,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            val shaarawiContent = ayah.tafsirShaarawi 
                ?: com.example.data.model.ShaarawyTafsirData.getGeneralReflectionForAyah(ayah.surahNumber, ayah.ayahNumber)

            when (selectedTabIndex) {
                0 -> {
                    // Tafsir Al-Jalalayn
                    TafsirCardItem(
                        source = TafsirSource.JALALAYN,
                        content = ayah.tafsirJalalayn,
                        fontSize = settings.tafsirFontSize,
                        surahName = surahName,
                        ayahNumber = ayah.ayahNumber
                    )
                }
                1 -> {
                    // Tafsir Al-Muyassar
                    TafsirCardItem(
                        source = TafsirSource.MUYASSAR,
                        content = ayah.tafsirMuyassar,
                        fontSize = settings.tafsirFontSize,
                        surahName = surahName,
                        ayahNumber = ayah.ayahNumber
                    )
                }
                2 -> {
                    // Sheikh Al-Sha'rawi
                    TafsirCardItem(
                        source = TafsirSource.SHAARAWI,
                        content = shaarawiContent,
                        fontSize = settings.tafsirFontSize,
                        surahName = surahName,
                        ayahNumber = ayah.ayahNumber
                    )
                }
                3 -> {
                    // All three stacked for full comparison
                    Column {
                        TafsirCardItem(
                            source = TafsirSource.JALALAYN,
                            content = ayah.tafsirJalalayn,
                            fontSize = settings.tafsirFontSize,
                            surahName = surahName,
                            ayahNumber = ayah.ayahNumber
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TafsirCardItem(
                            source = TafsirSource.MUYASSAR,
                            content = ayah.tafsirMuyassar,
                            fontSize = settings.tafsirFontSize,
                            surahName = surahName,
                            ayahNumber = ayah.ayahNumber
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TafsirCardItem(
                            source = TafsirSource.SHAARAWI,
                            content = shaarawiContent,
                            fontSize = settings.tafsirFontSize,
                            surahName = surahName,
                            ayahNumber = ayah.ayahNumber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TafsirCardItem(
    source: TafsirSource,
    content: String,
    fontSize: Float,
    surahName: String,
    ayahNumber: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = when (source) {
        TafsirSource.JALALAYN -> QuranGold
        TafsirSource.MUYASSAR -> IslamicGreen
        TafsirSource.SHAARAWI -> androidx.compose.ui.graphics.Color(0xFF0284C7) // Celestial Sky Blue for Sha'rawi
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Source Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = source.title,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Tafsir", "${source.title}:\n$content\n[$surahName : $ayahNumber]")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ التفسير", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "نسخ التفسير",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${source.title} [سورة $surahName : $ayahNumber]:\n$content\n\nتطبيق المصحف الرقمي"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة التفسير"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Author metadata
            Text(
                text = "المؤلف: ${source.author}",
                fontFamily = TajawalFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.6.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tafsir text
            Text(
                text = content,
                fontFamily = AmiriFontFamily,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.75f).sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
