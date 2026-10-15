package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayer.AdhanAlertMode
import com.example.prayer.PrayerSchedule
import com.example.prayer.PrayerType
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

/**
 * Muslim Muna style Upcoming Prayer Reminder Bar & Ticker.
 * Displays:
 * - Upcoming prayer badge with glowing icon
 * - Dynamic live countdown ticker with progress to next prayer
 * - Quick live adhan wallpaper screen trigger
 * - Muezzin sound alert mode toggle
 */
@Composable
fun MuslimMunaUpcomingPrayerBar(
    schedule: PrayerSchedule,
    muezzinName: String,
    onOpenLiveAdhan: (PrayerType) -> Unit,
    onToggleAlertMode: (PrayerType) -> Unit,
    onClickBar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextPrayer = schedule.nextPrayer ?: return
    val prayerType = nextPrayer.type

    val infiniteTransition = rememberInfiniteTransition(label = "muna_ticker_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("muslim_muna_upcoming_prayer_bar")
            .clickable(onClick = onClickBar),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(IslamicGreen.copy(alpha = glowAlpha), QuranGold.copy(alpha = glowAlpha))
            ),
            width = 1.4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Tag, Next Prayer Name, and Adhan Alert Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(IslamicGreen, Color(0xFF136048)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🕌", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "الصلاة القادمة:",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = prayerType.titleArabic,
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "موعد الأذان: ${nextPrayer.timeFormatted12h}",
                            fontFamily = TajawalFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Countdown badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = QuranGold.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(QuranGold, QuranGold)),
                        width = 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⏳", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = schedule.countdownFormatted,
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress towards next prayer
            LinearProgressIndicator(
                progress = { schedule.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = IslamicGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom action row: Muezzin name + Test Live Adhan Screen button + Alert toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "صوت: $muezzinName",
                    fontFamily = TajawalFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Button to open the live Adhan overlay with Islamic backgrounds
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IslamicGreen.copy(alpha = 0.12f),
                        modifier = Modifier
                            .testTag("open_live_adhan_button")
                            .clickable { onOpenLiveAdhan(prayerType) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "عرض شاشة الأذان",
                                tint = IslamicGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "شاشة الأذان بالخلفية",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = IslamicGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sound alert mode button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { onToggleAlertMode(prayerType) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val alertIcon = when (nextPrayer.alertMode) {
                                AdhanAlertMode.FULL_ADHAN -> Icons.AutoMirrored.Filled.VolumeUp
                                AdhanAlertMode.TAKBEER_ONLY -> Icons.Default.NotificationsActive
                                AdhanAlertMode.NOTIFICATION_ONLY -> Icons.Default.SurroundSound
                                AdhanAlertMode.MUTED -> Icons.AutoMirrored.Filled.VolumeMute
                            }
                            val alertColor = when (nextPrayer.alertMode) {
                                AdhanAlertMode.FULL_ADHAN -> IslamicGreen
                                AdhanAlertMode.TAKBEER_ONLY -> QuranGold
                                AdhanAlertMode.NOTIFICATION_ONLY -> MaterialTheme.colorScheme.primary
                                AdhanAlertMode.MUTED -> Color.Gray
                            }
                            Icon(
                                imageVector = alertIcon,
                                contentDescription = nextPrayer.alertMode.titleArabic,
                                tint = alertColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
