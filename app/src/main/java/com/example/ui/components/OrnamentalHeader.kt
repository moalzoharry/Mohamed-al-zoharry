package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReadingTheme
import com.example.data.model.SurahInfo
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

@Composable
fun SurahBanner(
    surahInfo: SurahInfo,
    theme: ReadingTheme,
    modifier: Modifier = Modifier
) {
    val borderColor = Color(theme.borderHex)
    val textColor = Color(theme.textColorHex)
    val secondaryColor = Color(theme.secondaryTextHex)
    val accentColor = Color(theme.accentColorHex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(theme.surfaceColorHex))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ornamental Top Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "۞",
                    fontSize = 18.sp,
                    color = QuranGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "سُورَةُ ${surahInfo.nameArabic}",
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "۞",
                    fontSize = 18.sp,
                    color = QuranGold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = surahInfo.revelationType.titleArabic,
                        fontFamily = TajawalFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "•",
                    color = secondaryColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${surahInfo.ayahCount} آيات",
                    fontFamily = TajawalFontFamily,
                    fontSize = 13.sp,
                    color = secondaryColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "•",
                    color = secondaryColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "الجزء ${surahInfo.startJuz}",
                    fontFamily = TajawalFontFamily,
                    fontSize = 13.sp,
                    color = secondaryColor
                )
            }
        }
    }
}

@Composable
fun BasmalahHeader(
    theme: ReadingTheme,
    modifier: Modifier = Modifier
) {
    val textColor = Color(theme.textColorHex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            fontFamily = AmiriFontFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(QuranGold.copy(alpha = 0.5f))
            )
            Text(
                text = " ✦ ",
                fontSize = 12.sp,
                color = QuranGold
            )
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(QuranGold.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun AyahNumberBadge(
    number: Int,
    theme: ReadingTheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(theme.accentColorHex).copy(alpha = 0.15f))
            .border(1.dp, Color(theme.accentColorHex).copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatArabicNumber(number),
            fontFamily = AmiriFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(theme.accentColorHex)
        )
    }
}

fun formatArabicNumber(num: Int): String {
    val arabicDigits = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
    return num.toString().map { char ->
        if (char in '0'..'9') arabicDigits[char - '0'] else char
    }.joinToString("")
}
