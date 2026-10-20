package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.prayer.LocationDetectionResult
import com.example.prayer.LocationHelper
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.IslamicGreenLight
import com.example.ui.theme.QuranGold
import com.example.ui.theme.QuranGoldLight
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.viewmodel.QuranViewModel
import kotlinx.coroutines.launch

/**
 * World-Class Visual Masterpiece Onboarding for "الفرقان بلس".
 * 4 Interactive Slides:
 * 1. Royal Emblem & Pure Waqf declaration.
 * 2. Instant GPS prayer times setup.
 * 3. Highlights of exclusive features (Quran, Sermon studio, Widget, Hijri calendar).
 * 4. Sacred Intent (إِنَّمَا نُطْعِمُكُمْ لِوَجْهِ اللَّهِ...) & Enter Journey button.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: QuranViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })

    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_aura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    var isGpsDetecting by remember { mutableStateOf(false) }
    var detectedCityName by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                isGpsDetecting = true
                when (val result = LocationHelper.detectCurrentLocation(context)) {
                    is LocationDetectionResult.Success -> {
                        viewModel.setAutoGpsLocation(
                            lat = result.latitude,
                            lng = result.longitude,
                            cityName = result.cityName,
                            countryName = result.countryName,
                            timeZoneId = result.timeZoneId,
                            method = result.recommendedMethod
                        )
                        detectedCityName = result.cityName
                        Toast.makeText(context, "تم ضبط موقعك بنجاح: ${result.cityName}", Toast.LENGTH_SHORT).show()
                    }
                    is LocationDetectionResult.Error -> {
                        Toast.makeText(context, result.messageArabic, Toast.LENGTH_SHORT).show()
                    }
                }
                isGpsDetecting = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen")
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F442B),
                        IslamicGreen,
                        Color(0xFF072718),
                        Color(0xFF02120B)
                    ),
                    radius = 1100f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Top App Bar with Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_emblem),
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(1.dp, QuranGold, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الْفُرْقَان بَلَس",
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = QuranGoldLight
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, QuranGold.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable(onClick = onFinish)
                ) {
                    Text(
                        text = "تخطي للدخول",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingSlideEmblem(auraScale = auraScale, glowAlpha = glowAlpha)
                    1 -> OnboardingSlideGps(
                        isDetecting = isGpsDetecting,
                        detectedCity = detectedCityName,
                        onDetectClick = {
                            val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (fine || coarse) {
                                coroutineScope.launch {
                                    isGpsDetecting = true
                                    when (val res = LocationHelper.detectCurrentLocation(context)) {
                                        is LocationDetectionResult.Success -> {
                                            viewModel.setAutoGpsLocation(
                                                lat = res.latitude,
                                                lng = res.longitude,
                                                cityName = res.cityName,
                                                countryName = res.countryName,
                                                timeZoneId = res.timeZoneId,
                                                method = res.recommendedMethod
                                            )
                                            detectedCityName = res.cityName
                                            Toast.makeText(context, "تم ضبط موقعك بنجاح: ${res.cityName}", Toast.LENGTH_SHORT).show()
                                        }
                                        is LocationDetectionResult.Error -> {
                                            Toast.makeText(context, res.messageArabic, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    isGpsDetecting = false
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )
                    2 -> OnboardingSlideFeatures()
                    3 -> OnboardingSlideIntent(glowAlpha = glowAlpha, onStart = onFinish)
                }
            }

            // Bottom Navigation & Indicator Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back or empty
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward, // RTL Forward goes to prev in Arabic
                            contentDescription = "السابق",
                            tint = QuranGoldLight
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Page indicator dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 28.dp else 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) QuranGoldLight else Color.White.copy(alpha = 0.3f))
                        )
                    }
                }

                // Next or Finish button
                if (pagerState.currentPage < 3) {
                    Surface(
                        shape = CircleShape,
                        color = QuranGold,
                        modifier = Modifier
                            .size(46.dp)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // RTL Back goes forward
                                contentDescription = "التالي",
                                tint = Color.Black
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

// ================= SLIDE 1: ROYAL EMBLEM & PURE WAQF =================

@Composable
private fun OnboardingSlideEmblem(auraScale: Float, glowAlpha: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .scale(auraScale),
            contentAlignment = Alignment.Center
        ) {
            // Glowing Halo
            Box(
                modifier = Modifier
                    .size(165.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                QuranGoldLight.copy(alpha = 0.5f * glowAlpha),
                                IslamicGreenLight.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Emblem Card
            Surface(
                shape = RoundedCornerShape(36.dp),
                color = Color(0xFF0A3323),
                modifier = Modifier
                    .size(126.dp)
                    .shadow(18.dp, RoundedCornerShape(36.dp), spotColor = QuranGoldLight)
                    .border(
                        2.5.dp,
                        Brush.linearGradient(listOf(QuranGoldLight, QuranGold, Color(0xFFE5C065))),
                        RoundedCornerShape(36.dp)
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "الْفُرْقَان بَلَس",
            fontFamily = AmiriFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = QuranGoldLight,
            style = TextStyle(
                shadow = Shadow(
                    color = QuranGold.copy(alpha = 0.85f * glowAlpha),
                    offset = Offset(0f, 0f),
                    blurRadius = 24f * glowAlpha
                )
            )
        )

        Text(
            text = "رفيقك الإيماني الشامل في رحاب القرآن والعبادات",
            fontFamily = TajawalFontFamily,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.Black.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, QuranGold.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "وَقْفٌ خَالِصٌ لِوَجْهِ اللَّهِ تَعَالَى",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = QuranGoldLight
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "صُمم هذا التطبيق بحب وإتقان ليكون مجانياً 100% مدى الحياة، خالياً تماماً من أي إعلانات تجارية، ولا نبتغي به إلا وجه الله الكريم وخدمة كتاب الله.",
                    fontFamily = TajawalFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ================= SLIDE 2: GPS PRAYER TIMES SETUP =================

@Composable
private fun OnboardingSlideGps(
    isDetecting: Boolean,
    detectedCity: String?,
    onDetectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.35f),
            border = androidx.compose.foundation.BorderStroke(2.dp, QuranGold),
            modifier = Modifier.size(110.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = null,
                    tint = QuranGoldLight,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "مواقيت صلاة وأذان دقيقة 100%",
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = QuranGoldLight,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "حساب فلكي معتمد يحدد مواعيد الأذان بدقة متناهية وفق موقعك الجغرافي، مع عد تنازلي مباشر للصلاة القادمة وتنبيهات أذان منوعة.",
            fontFamily = TajawalFontFamily,
            fontSize = 12.sp,
            lineHeight = 20.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(26.dp))

        // GPS Quick Action Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.Black.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (detectedCity != null) Color(0xFF22C55E) else QuranGold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isDetecting, onClick = onDetectClick)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (detectedCity != null) Color(0xFF22C55E).copy(alpha = 0.2f) else QuranGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (detectedCity != null) Icons.Default.CheckCircle else Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (detectedCity != null) Color(0xFF22C55E) else QuranGoldLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (detectedCity != null) "تم ضبط الموقع: $detectedCity" else "تحديد موقعي الآن عبر GPS",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (detectedCity != null) "المواقيت جاهزة ومضبوطة بنجاح" else "اضغط لضبط المواقيت وطريقة الحساب فوراً",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                if (isDetecting) {
                    CircularProgressIndicator(
                        color = QuranGoldLight,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (detectedCity != null) Color(0xFF22C55E) else QuranGold
                    ) {
                        Text(
                            text = if (detectedCity != null) "مُفعّل ✓" else "ضبط GPS",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (detectedCity != null) Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ================= SLIDE 3: EXCLUSIVE APP HIGHLIGHTS =================

@Composable
private fun OnboardingSlideFeatures() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "منظومة إسلامية متكاملة",
            fontFamily = TajawalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = QuranGoldLight
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "كل ما يحتاجه المسلم في حياته اليومية بين يديك",
            fontFamily = TajawalFontFamily,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4 Feature Tiles Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OnboardingFeatureTile(
                    emoji = "📖",
                    title = "المصحف والتفاسير",
                    subtitle = "رسم عثماني، الجلالين والشعراوي، وتلاوات كبار القراء",
                    modifier = Modifier.weight(1f)
                )
                OnboardingFeatureTile(
                    emoji = "🎙️",
                    title = "تسجيل خطبة الجمعة",
                    subtitle = "جودة استوديو وتوفير مساحة وحفظ بالهاتف أو الميموري",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OnboardingFeatureTile(
                    emoji = "📱",
                    title = "ويدجت الشاشة",
                    subtitle = "متابعة أوقات الصلاة والعد التنازلي من سطح مكتب الموبايل",
                    modifier = Modifier.weight(1f)
                )
                OnboardingFeatureTile(
                    emoji = "📅",
                    title = "التقويم الهجري",
                    subtitle = "تنبيه الأيام البيض وسنن الصيام وضبط رؤية الهلال",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureTile(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, QuranGold.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = QuranGoldLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontFamily = TajawalFontFamily,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// ================= SLIDE 4: SACRED INTENT & ENTER =================

@Composable
private fun OnboardingSlideIntent(
    glowAlpha: Float,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Neon Ayah Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.8.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        QuranGold.copy(alpha = 0.3f),
                        QuranGoldLight.copy(alpha = 0.9f * glowAlpha),
                        QuranGold.copy(alpha = 0.3f)
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = (16 * glowAlpha).dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = QuranGoldLight
                )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = QuranGoldLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مِيثَاقُ الْعَمَلِ وَالْإِخْلَاص",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
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

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "﴿ إِنَّمَا نُطْعِمُكُمْ لِوَجْهِ اللَّهِ لَا نُرِيدُ مِنكُمْ جَزَاءً وَلَا شُكُورًا ﴾",
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFFFF7D6),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFFFFD54F).copy(alpha = 0.95f * glowAlpha),
                            offset = Offset(0f, 0f),
                            blurRadius = 30f * glowAlpha
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

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "نسألكم صالح الدعاء بظهر الغيب لكل من صمم وشارك وساهم في إخراج هذا العمل المبارك للنور.",
                    fontFamily = TajawalFontFamily,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // THE GRAND ENTER BUTTON
        Button(
            onClick = onStart,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QuranGold),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_start_journey_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ابدأ رحلتك الإيمانية المباركة ✨",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1E1E)
                )
            }
        }
    }
}
