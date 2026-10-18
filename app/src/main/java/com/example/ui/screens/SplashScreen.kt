package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.QuranGoldLight
import com.example.ui.theme.TajawalFontFamily
import kotlinx.coroutines.delay

/**
 * Grand Opening Splash Screen for "الفرقان بلس" (Al-Furqan Plus).
 * Features:
 * - Radiant glowing App Logo Emblem
 * - Neon illuminated Quranic Ayah with full Tashkeel:
 *   ﴿ إِنَّمَا نُطْعِمُكُمْ لِوَجْهِ اللَّهِ لَا نُرِيدُ مِنكُمْ جَزَاءً وَلَا شُكُورًا ﴾
 * - Breathing aura and particle glow
 * - Auto-forward to Home after delay, or instant bypass on click
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.85f) }
    val alphaAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_glow"
    )

    LaunchedEffect(Unit) {
        // Smooth entrance
        alphaAnim.animateTo(1f, animationSpec = tween(900))
        scaleAnim.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
        // Stay for reading and transition
        delay(2600)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("splash_screen")
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F4A2F),
                        IslamicGreen,
                        Color(0xFF082D1C),
                        Color(0xFF03140C)
                    ),
                    radius = 1200f
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Radiant Glowing App Emblem Logo
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer glowing halo
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    QuranGoldLight.copy(alpha = 0.45f * glowIntensity),
                                    IslamicGreen.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Logo container with gold border
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF0A3323),
                    modifier = Modifier
                        .size(108.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = QuranGold,
                            spotColor = QuranGoldLight
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                listOf(QuranGoldLight, QuranGold, Color(0xFFE5C065))
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_emblem),
                        contentDescription = "شعار الفرقان بلس",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. App Name & Spiritual Tagline
            Text(
                text = "الْفُرْقَان بَلَس",
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = QuranGoldLight,
                style = TextStyle(
                    shadow = Shadow(
                        color = QuranGold.copy(alpha = 0.8f * glowIntensity),
                        offset = Offset(0f, 0f),
                        blurRadius = 24f * glowIntensity
                    )
                )
            )

            Text(
                text = "Al-Furqan Plus • القرآن الكريم ومواقيت الصلاة والعبادات",
                fontFamily = TajawalFontFamily,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 3. NEON GLOWING QURANIC AYAH (إِنَّمَا نُطْعِمُكُمْ لِوَجْهِ اللَّهِ...)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Black.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            QuranGold.copy(alpha = 0.2f),
                            QuranGoldLight.copy(alpha = 0.8f * glowIntensity),
                            QuranGold.copy(alpha = 0.2f)
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = (12 * glowIntensity).dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = QuranGoldLight
                    )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = QuranGoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "وَقْفٌ خَالِصٌ لِوَجْهِ اللَّهِ تَعَالَى",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = QuranGoldLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = QuranGoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // THE HOLY AYAH IN NEON GLOWING GOLD WITH FULL TASHKEEL
                    Text(
                        text = "﴿ إِنَّمَا نُطْعِمُكُمْ لِوَجْهِ اللَّهِ لَا نُرِيدُ مِنكُمْ جَزَاءً وَلَا شُكُورًا ﴾",
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFFFF7D6),
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xFFFFD54F).copy(alpha = 0.95f * glowIntensity),
                                offset = Offset(0f, 0f),
                                blurRadius = 28f * glowIntensity
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "سورة الإنسان — الآية ٩",
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        color = QuranGold.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 4. Subtle loading & skip hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = QuranGoldLight,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "جاري فتح التطبيق • اضغط للشاشة للدخول فوراً",
                    fontFamily = TajawalFontFamily,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}
