package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qibla.QiblaSensorManager
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.viewmodel.QuranViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaCompassScreen(
    viewModel: QuranViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val selectedCity by viewModel.selectedCity.collectAsState()

    val sensorManager = remember { QiblaSensorManager(context) }
    val qiblaState by sensorManager.state.collectAsState()

    // Update coordinates when selectedCity changes
    LaunchedEffect(selectedCity) {
        sensorManager.updateCoordinates(selectedCity.latitude, selectedCity.longitude)
    }

    // Lifecycle of sensors
    DisposableEffect(Unit) {
        sensorManager.startListening()
        onDispose {
            sensorManager.stopListening()
        }
    }

    // Haptic vibration when user aligns with Qibla
    var lastVibratedState by remember { mutableStateOf(false) }
    LaunchedEffect(qiblaState.isFacingQibla) {
        if (qiblaState.isFacingQibla && !lastVibratedState) {
            triggerVibration(context)
            lastVibratedState = true
        } else if (!qiblaState.isFacingQibla) {
            lastVibratedState = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "بوصلة القبلة الشريفة",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("qibla_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("qibla_compass_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. City and coordinates badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = IslamicGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "المدينة: ${selectedCity.nameArabic}، ${selectedCity.countryArabic}",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "إحداثيات: ${String.format(java.util.Locale.US, "%.2f", selectedCity.latitude)}° N, ${String.format(java.util.Locale.US, "%.2f", selectedCity.longitude)}° E",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "زاوية القبلة",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${qiblaState.qiblaBearing.toInt()}°",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = IslamicGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Qibla Facing Status Alert Banner
            val bannerColor by animateColorAsState(
                targetValue = if (qiblaState.isFacingQibla) IslamicGreen else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(300),
                label = "bannerColor"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = bannerColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (qiblaState.isFacingQibla) Icons.Default.CheckCircle else Icons.Default.Navigation,
                        contentDescription = null,
                        tint = if (qiblaState.isFacingQibla) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (qiblaState.isFacingQibla)
                            "أنت تواجه اتجاه القبلة الشريفة تماماً 🕋"
                        else
                            "وجّه هاتفك حتى تتطابق إبرة القبلة الذهبية مع مؤشر الكعبة",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (qiblaState.isFacingQibla) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. MAIN COMPASS GRAPHICAL DIAL
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .testTag("qibla_compass_dial"),
                contentAlignment = Alignment.Center
            ) {
                // Background compass dial rotating with device heading
                CompassDialCanvas(
                    deviceHeading = qiblaState.deviceHeading,
                    isFacingQibla = qiblaState.isFacingQibla
                )

                // Qibla Pointer Arrow & Kaaba Icon (Points to Kaaba)
                KaabaPointer(
                    relativeAngle = qiblaState.qiblaRelativeAngle,
                    isFacingQibla = qiblaState.isFacingQibla
                )

                // Center Hub with degrees
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(76.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${qiblaState.deviceHeading.toInt()}°",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (qiblaState.isFacingQibla) IslamicGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = getCardinalDirectionArabic(qiblaState.deviceHeading),
                            fontFamily = TajawalFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = QuranGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Instructions and calibration card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = QuranGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إرشادات دقة بوصلة القبلة:",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• ضع الهاتف في وضع أفقي مستوٍ بعيداً عن المعادن والمجالات المغناطيسية وأجهزة الكمبيوتر.\n" +
                                "• في حال تذبذب المؤشر، حرّك الهاتف في الهواء على شكل رقم (8) لمعايرة مستشعر البوصلة المغناطيسي.\n" +
                                "• يهتز الهاتف تلقائياً وتتحول البوصلة إلى اللون الأخضر الزاهي عند مطابقة اتجاه الكعبة المشرفة بدقة.",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun CompassDialCanvas(
    deviceHeading: Float,
    isFacingQibla: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedHeading by animateFloatAsState(
        targetValue = -deviceHeading,
        animationSpec = tween(durationMillis = 150),
        label = "animatedHeading"
    )

    val ringColor = if (isFacingQibla) IslamicGreen else MaterialTheme.colorScheme.outlineVariant

    Canvas(
        modifier = modifier
            .size(310.dp)
            .rotate(animatedHeading)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f - 12.dp.toPx()

        // Outer ring
        drawCircle(
            color = ringColor,
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        // Inner tick marks
        for (i in 0 until 360 step 5) {
            val angleRad = Math.toRadians((i - 90).toDouble())
            val isMajor = i % 30 == 0
            val isCardinal = i % 90 == 0

            val tickLen = when {
                isCardinal -> 14.dp.toPx()
                isMajor -> 10.dp.toPx()
                else -> 5.dp.toPx()
            }

            val startX = center.x + (radius - tickLen) * cos(angleRad).toFloat()
            val startY = center.y + (radius - tickLen) * sin(angleRad).toFloat()
            val endX = center.x + radius * cos(angleRad).toFloat()
            val endY = center.y + radius * sin(angleRad).toFloat()

            val tickColor = when {
                i == 0 -> Color(0xFFDC2626) // North red tick
                isMajor -> QuranGold
                else -> Color.Gray.copy(alpha = 0.4f)
            }

            drawLine(
                color = tickColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun KaabaPointer(
    relativeAngle: Float,
    isFacingQibla: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedAngle by animateFloatAsState(
        targetValue = relativeAngle,
        animationSpec = tween(durationMillis = 180),
        label = "kaabaAngle"
    )

    Box(
        modifier = modifier
            .size(310.dp)
            .rotate(animatedAngle),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 18.dp)
        ) {
            // Kaaba Badge Icon
            Surface(
                shape = CircleShape,
                color = if (isFacingQibla) IslamicGreen else QuranGold,
                shadowElevation = 4.dp,
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🕋",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Triangle Pointer Arrow
            Canvas(modifier = Modifier.size(16.dp, 24.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(
                    path = path,
                    color = if (isFacingQibla) IslamicGreen else QuranGold
                )
            }
        }
    }
}

private fun getCardinalDirectionArabic(degrees: Float): String {
    val deg = (degrees + 360f) % 360f
    return when {
        deg >= 337.5 || deg < 22.5 -> "شمال"
        deg < 67.5 -> "شمال شرق"
        deg < 112.5 -> "شرق"
        deg < 157.5 -> "جنوب شرق"
        deg < 202.5 -> "جنوب"
        deg < 247.5 -> "جنوب غرب"
        deg < 292.5 -> "غرب"
        else -> "شمال غرب"
    }
}

private fun triggerVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(80)
            }
        }
    } catch (_: Exception) {}
}
