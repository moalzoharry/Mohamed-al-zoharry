package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.data.repository.AyahTranslationManager
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ayah
import com.example.data.model.ReadingSettings
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

@Composable
fun AyahItemCard(
    ayah: Ayah,
    surahName: String,
    settings: ReadingSettings,
    isBookmarked: Boolean,
    isPlaying: Boolean,
    isSavedStopPoint: Boolean = false,
    onTafsirClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onSaveStopPointClick: () -> Unit = {},
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = settings.theme
    val surfaceColor = Color(theme.surfaceColorHex)
    val rawTextColor = Color(theme.textColorHex)
    val rawSecondaryColor = Color(theme.secondaryTextHex)
    val accentColor = Color(theme.accentColorHex)
    val borderColor = Color(theme.borderHex)

    // Eye comfort warm coloring to reduce blue light glare in dark rooms
    val textColor = if (settings.eyeComfortWarmFilter) {
        if (theme.isDark) Color(0xFFF3E7D3) else Color(0xFF382B1C)
    } else rawTextColor

    val secondaryColor = if (settings.eyeComfortWarmFilter) {
        if (theme.isDark) Color(0xFFB8AE9C) else Color(0xFF6B5845)
    } else rawSecondaryColor

    val activeBorderColor by animateColorAsState(
        targetValue = if (isPlaying) accentColor else borderColor,
        label = "ayah_border"
    )

    var showTranslation by remember { mutableStateOf(false) }
    var selectedLanguageCode by remember { mutableStateOf("en") }
    var translationText by remember { mutableStateOf<String?>(null) }
    var isTranslating by remember { mutableStateOf(false) }

    LaunchedEffect(showTranslation, selectedLanguageCode) {
        if (showTranslation) {
            isTranslating = true
            translationText = AyahTranslationManager.getTranslation(
                surahNumber = ayah.surahNumber,
                ayahNumber = ayah.ayahNumber,
                languageCode = selectedLanguageCode
            )
            isTranslating = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("ayah_card_${ayah.surahNumber}_${ayah.ayahNumber}")
            .border(
                width = if (isPlaying) 2.dp else 1.dp,
                color = activeBorderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Ayah Number & Surah Ref
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AyahNumberBadge(
                    number = ayah.ayahNumber,
                    theme = theme
                )

                if (isPlaying) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "يتم الاستماع الآن",
                                fontFamily = TajawalFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }

                if (isSavedStopPoint) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = QuranGold.copy(alpha = 0.2f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = "📌", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "موضع وقوفك المحفوظ",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = QuranGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quran Ayah Text
            val displayText = if (settings.showTashkeel) ayah.textWithTashkeel else ayah.textClean
            Text(
                text = "$displayText ﴿${formatArabicNumber(ayah.ayahNumber)}﴾",
                fontFamily = AmiriFontFamily,
                fontSize = settings.quranFontSize.sp,
                lineHeight = (settings.quranFontSize * 1.85f).sp,
                fontWeight = FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(borderColor.copy(alpha = 0.7f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Actions: Tafsir & Translation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Tafsir Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor.copy(alpha = 0.12f),
                        modifier = Modifier
                            .testTag("tafsir_button_${ayah.ayahNumber}")
                            .clickable(onClick = onTafsirClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "التفسير",
                                tint = accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "التفسير والخواطر",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }

                    // Translate Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (showTranslation) QuranGold.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.12f),
                        border = if (showTranslation) androidx.compose.foundation.BorderStroke(1.dp, QuranGold) else null,
                        modifier = Modifier
                            .testTag("translation_button_${ayah.ayahNumber}")
                            .clickable { showTranslation = !showTranslation }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "ترجمة الآية",
                                tint = if (showTranslation) QuranGold else accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ترجمة",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showTranslation) QuranGold else accentColor
                            )
                        }
                    }
                }

                // Right Actions: Play Audio, Bookmark, Copy, Share
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Play / Pause Audio
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("play_audio_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تلاوة الآية",
                            tint = if (isPlaying) accentColor else secondaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Bookmark
                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("bookmark_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "علامة مرجعية",
                            tint = if (isBookmarked) QuranGold else secondaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Save Stop Position in Surah (حفظ موضع الوقوف)
                    IconButton(
                        onClick = onSaveStopPointClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("save_stop_point_${ayah.ayahNumber}")
                    ) {
                        Text(
                            text = if (isSavedStopPoint) "📌" else "📍",
                            fontSize = 16.sp
                        )
                    }

                    // Copy
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = "﴿ ${ayah.textWithTashkeel} ﴾\n[سورة $surahName — الآية ${ayah.ayahNumber}]\n\n📖 من تطبيق «الفرقان بلس» | وقف خالص لوجه الله تعالى"
                            val clip = ClipData.newPlainText("Quran Ayah", clipText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الآية الكريمة بنجاح ✨", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("copy_ayah_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "نسخ الآية الكريمة",
                            tint = secondaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val shareMessage = buildString {
                                appendLine("﷽")
                                appendLine("﴿ ${ayah.textWithTashkeel} ﴾")
                                appendLine("سورة $surahName — الآية ${ayah.ayahNumber}")
                                if (showTranslation && translationText != null) {
                                    appendLine()
                                    appendLine("Meaning ($selectedLanguageCode):")
                                    appendLine(translationText)
                                }
                                appendLine()
                                appendLine("📖 من تطبيق «الفرقان بلس» Al-Furqan Plus")
                                appendLine("وقف خالص لوجه الله تعالى • الدال على الخير كفاعله 💚")
                            }

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة الآية الكريمة (صدقة جارية)"))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_ayah_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة الآية الكريمة",
                            tint = secondaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expandable Translation Panel
            AnimatedVisibility(visible = showTranslation) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, QuranGold.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Language selector row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AyahTranslationManager.supportedLanguages.forEach { lang ->
                                    val isSelected = (selectedLanguageCode == lang.code)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) QuranGold else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.clickable { selectedLanguageCode = lang.code }
                                    ) {
                                        Text(
                                            text = "${lang.flagEmoji} ${lang.code.uppercase()}",
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { showTranslation = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق الترجمة",
                                    tint = secondaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isTranslating) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = QuranGold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "جاري جلب الترجمة المعتمدة...",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = translationText ?: "لا تتوفر ترجمة",
                                fontFamily = TajawalFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Ayah Translation", "${ayah.textClean}\n[${translationText}]")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "تم نسخ الترجمة", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(
                                        text = "📋 نسخ الترجمة",
                                        fontFamily = TajawalFontFamily,
                                        fontSize = 11.sp,
                                        color = QuranGold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
