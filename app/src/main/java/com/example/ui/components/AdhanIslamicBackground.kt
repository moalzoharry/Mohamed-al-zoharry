package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.prayer.PrayerType

/**
 * Rich Islamic artistic backgrounds tailored specifically for each of the 5 daily prayers:
 * - Fajr: Celestial dawn indigo & morning horizon gold with crescent moon
 * - Dhuhr: Sunlit marble arches, radiant midday emerald & golden arabesque rosette
 * - Asr: Warm golden hour amber & dusk mosque dome silhouettes
 * - Maghrib: Twilight crimson, purple horizon & tranquil sunset mosque silhouette
 * - Isha: Deep velvety starry midnight sapphire & illuminated Kaaba/mosque minarets
 */
@Composable
fun AdhanIslamicBackground(
    prayerType: PrayerType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "islamic_bg_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val gradientColors = when (prayerType) {
        PrayerType.FAJR, PrayerType.SUNRISE -> listOf(
            Color(0xFF091322),
            Color(0xFF132A3E),
            Color(0xFF1C424D),
            Color(0xFF5E4935),
            Color(0xFF9E6D38)
        )
        PrayerType.DHUHR -> listOf(
            Color(0xFF06231A),
            Color(0xFF0B3A2C),
            Color(0xFF13533F),
            Color(0xFF1E6F56),
            Color(0xFFB58D3D)
        )
        PrayerType.ASR -> listOf(
            Color(0xFF261209),
            Color(0xFF452210),
            Color(0xFF6B3718),
            Color(0xFF915124),
            Color(0xFFD99B3B)
        )
        PrayerType.SUNSET, PrayerType.MAGHRIB -> listOf(
            Color(0xFF190A22),
            Color(0xFF2D1036),
            Color(0xFF4C153B),
            Color(0xFF70203B),
            Color(0xFFB54C35)
        )
        PrayerType.ISHA -> listOf(
            Color(0xFF050B14),
            Color(0xFF0A1526),
            Color(0xFF102340),
            Color(0xFF173059),
            Color(0xFFC4A252)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // Decorative canvas with Islamic arches, stars, and minaret silhouettes
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIslamicScenery(prayerType, pulse)
        }

        content()
    }
}

private fun DrawScope.drawIslamicScenery(prayerType: PrayerType, pulse: Float) {
    val w = size.width
    val h = size.height

    // 1. Radiant glow in the upper center / horizon
    val glowCenter = Offset(w * 0.5f, h * 0.22f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x35E0B94D), Color(0x00000000)),
            center = glowCenter,
            radius = w * 0.65f * pulse
        ),
        radius = w * 0.65f * pulse,
        center = glowCenter
    )

    // 2. Stars for night / dawn / dusk prayers
    if (prayerType == PrayerType.ISHA || prayerType == PrayerType.FAJR || prayerType == PrayerType.MAGHRIB) {
        val starCoords = listOf(
            Offset(w * 0.15f, h * 0.08f),
            Offset(w * 0.28f, h * 0.12f),
            Offset(w * 0.78f, h * 0.09f),
            Offset(w * 0.88f, h * 0.16f),
            Offset(w * 0.45f, h * 0.05f),
            Offset(w * 0.65f, h * 0.14f),
            Offset(w * 0.12f, h * 0.20f),
            Offset(w * 0.84f, h * 0.25f)
        )
        for (st in starCoords) {
            drawCircle(
                color = Color.White.copy(alpha = 0.7f * pulse),
                radius = 2.5f,
                center = st
            )
        }
    }

    // 3. Subtle Crescent Moon in top left or right
    val moonCenter = Offset(w * 0.82f, h * 0.10f)
    drawCircle(
        color = Color(0xFFF7D98B).copy(alpha = 0.85f),
        radius = 22f,
        center = moonCenter
    )
    drawCircle(
        color = Color(0xFF0D1726).copy(alpha = 0.95f),
        radius = 18f,
        center = Offset(moonCenter.x - 7f, moonCenter.y - 4f)
    )

    // 4. Islamic Arches & Minarets Silhouette along bottom
    val mosqueColor = Color(0x66000000)
    val silhouettePath = Path().apply {
        moveTo(0f, h)
        lineTo(0f, h * 0.88f)

        // Left Minaret
        lineTo(w * 0.12f, h * 0.88f)
        lineTo(w * 0.12f, h * 0.76f)
        lineTo(w * 0.14f, h * 0.72f) // spire
        lineTo(w * 0.16f, h * 0.76f)
        lineTo(w * 0.16f, h * 0.88f)

        // Small Dome
        lineTo(w * 0.26f, h * 0.88f)
        cubicTo(
            w * 0.28f, h * 0.82f,
            w * 0.36f, h * 0.82f,
            w * 0.38f, h * 0.88f
        )

        // Central Grand Dome
        lineTo(w * 0.42f, h * 0.88f)
        cubicTo(
            w * 0.44f, h * 0.74f,
            w * 0.56f, h * 0.74f,
            w * 0.58f, h * 0.88f
        )

        // Small Dome Right
        lineTo(w * 0.62f, h * 0.88f)
        cubicTo(
            w * 0.64f, h * 0.82f,
            w * 0.72f, h * 0.82f,
            w * 0.74f, h * 0.88f
        )

        // Right Minaret
        lineTo(w * 0.84f, h * 0.88f)
        lineTo(w * 0.84f, h * 0.75f)
        lineTo(w * 0.86f, h * 0.71f) // spire
        lineTo(w * 0.88f, h * 0.75f)
        lineTo(w * 0.88f, h * 0.88f)

        lineTo(w, h * 0.88f)
        lineTo(w, h)
        close()
    }

    drawPath(path = silhouettePath, color = mosqueColor)
}
