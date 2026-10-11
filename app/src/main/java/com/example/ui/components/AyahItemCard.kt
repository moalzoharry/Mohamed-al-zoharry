package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    onTafsirClick: () -> Unit,
    onBookmarkClick: () -> Unit,
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
                // Left Actions: Tafsir Button (Highlighted Primary)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier
                        .testTag("tafsir_button_${ayah.ayahNumber}")
                        .clickable(onClick = onTafsirClick)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "التفسير",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "التفسير (الجلالين / الميسر)",
                            fontFamily = TajawalFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
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

                    // Copy
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Quran Ayah", "${ayah.textWithTashkeel} [$surahName : ${ayah.ayahNumber}]")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الآية الكريمة", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "نسخ الآية",
                            tint = secondaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "﴿${ayah.textWithTashkeel}﴾\n[سورة $surahName : آية ${ayah.ayahNumber}]\n\nتمت المشاركة من تطبيق المصحف الرقمي"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة الآية الكريمة"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة الآية",
                            tint = secondaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
