package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayer.HijriDateConverter
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.QuranGoldLight
import com.example.ui.theme.TajawalFontFamily
import java.util.Calendar

data class IslamicEvent(
    val monthIndex: Int, // 1 to 12
    val day: Int,
    val titleArabic: String,
    val descriptionArabic: String,
    val isFastingRecommended: Boolean = false
)

object IslamicEventsList {
    val events = listOf(
        IslamicEvent(1, 1, "رأس السنة الهجرية", "بداية العام الهجري الجديد واستحضار هجرة النبي ﷺ"),
        IslamicEvent(1, 9, "تاسوعاء", "صيام تاسوعاء مستحب مع عاشوراء مخافة مشابهة أهل الكتاب", isFastingRecommended = true),
        IslamicEvent(1, 10, "يوم عاشوراء", "صيامه يكفّر ذنوب السنة الماضية كما ورد في صحيح مسلم", isFastingRecommended = true),
        IslamicEvent(3, 12, "المولد النبوي الشريف", "ذكرى مولد سيد الخلق وخاتم الأنبياء محمد ﷺ"),
        IslamicEvent(7, 27, "ذكرى الإسراء والمعراج", "معجزة الإسراء من المسجد الحرام للأقصى والمعراج للسماوات العلى"),
        IslamicEvent(8, 15, "ليلة النصف من شعبان", "ليلة مباركة يُستحب فيها قيام الليل والدعاء"),
        IslamicEvent(9, 1, "غرة شهر رمضان المبارك", "بداية شهر الصيام والقرآن والرحمة والمغفرة", isFastingRecommended = true),
        IslamicEvent(9, 21, "بداية العشر الأواخر من رمضان", "الاجتهاد في العبادة والاعتكاف وطلب ليلة القدر", isFastingRecommended = true),
        IslamicEvent(9, 27, "أرجى ليالي القدر", "ليلة القدر خير من ألف شهر", isFastingRecommended = true),
        IslamicEvent(10, 1, "عيد الفطر المبارك", "يوم الجائزة والفرح بإتمام صيام شهر رمضان (يحرم صيامه)"),
        IslamicEvent(10, 2, "صيام الست من شوال", "من صام رمضان ثم أتبعه بست من شوال كان كصيام الدهر", isFastingRecommended = true),
        IslamicEvent(12, 1, "أول ذي الحجة (العشر الأوائل)", "أفضل أيام الدنيا، العمل الصالح فيها أحب إلى الله", isFastingRecommended = true),
        IslamicEvent(12, 9, "يوم عرفة", "أعظم أيام العام، صيامه يكفر سنة ماضية وسنة باقية لغير الحاج", isFastingRecommended = true),
        IslamicEvent(12, 10, "عيد الأضحى المبارك", "يوم النحر الأكبر وفرحة الحجاج والمسلمين بالأضاحي (يحرم صيامه)"),
        IslamicEvent(12, 11, "أيام التشريق (11-13 ذي الحجة)", "أيام أكل وشرب وذكر لله تعالى (يحرم صيامها)")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriCalendarScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("hijri_calendar_prefs", Context.MODE_PRIVATE) }
    var moonAdjustment by remember { mutableIntStateOf(prefs.getInt("moon_adj", 0)) }

    val cal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, moonAdjustment)
    }
    val hijriDate = HijriDateConverter.gregorianToHijri(cal)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "التقويم الهجري والمناسبات",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("hijri_calendar_back_button")
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
        modifier = modifier.testTag("hijri_calendar_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. HERO TODAY HIJRI CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(IslamicGreen, Color(0xFF093923), Color(0xFF051C11))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = QuranGold.copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = QuranGoldLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "تاريخ اليوم الهجري",
                                fontFamily = TajawalFontFamily,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${hijriDate.day} ${hijriDate.monthArabic} ${hijriDate.year} هـ",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = QuranGoldLight
                            )

                            // Sunnah fasting badge if today is White day or Mon/Thu
                            val isWhiteDay = hijriDate.day in 13..15
                            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                            val isMonOrThu = dayOfWeek == Calendar.MONDAY || dayOfWeek == Calendar.THURSDAY

                            if (isWhiteDay || isMonOrThu) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = QuranGold.copy(alpha = 0.25f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, QuranGold)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "✨", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isWhiteDay) "اليوم من الأيام البيض المستحب صيامها" else "اليوم سُنة صيام (الإثنين/الخميس)",
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. MOON SIGHTING ADJUSTMENT (+1 / -1 DAY)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تعديل رؤية الهلال:",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "لمطابقة الرؤية الشرعية الرسمية لبلدك (+1 أو -1 يوم)",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    moonAdjustment -= 1
                                    prefs.edit().putInt("moon_adj", moonAdjustment).apply()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "إنقاص يوم", tint = MaterialTheme.colorScheme.onSurface)
                            }

                            Text(
                                text = if (moonAdjustment > 0) "+$moonAdjustment" else "$moonAdjustment",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = IslamicGreen,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )

                            IconButton(
                                onClick = {
                                    moonAdjustment += 1
                                    prefs.edit().putInt("moon_adj", moonAdjustment).apply()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة يوم", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // 3. SUNNAH FASTING EXPLAINER (الأيام البيض وصيام الإثنين والخميس)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🌙", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سُنن الصيام الدورية:",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = IslamicGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• صيام الأيام البيض: 13 و 14 و 15 من كل شهر هجري، كان النبي ﷺ لا يدع صيامها.\n• صيام الإثنين والخميس: تُعرض فيهما الأعمال على الله تعالى ويُحب النبي ﷺ أن يُعرض عمله وهو صائم.",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. MAJOR ISLAMIC EVENTS SECTION
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = QuranGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "المناسبات والشهور الإسلامية الكبرى:",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            items(IslamicEventsList.events) { event ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (event.isFastingRecommended) IslamicGreen.copy(alpha = 0.12f) else QuranGold.copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${event.day}",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (event.isFastingRecommended) IslamicGreen else QuranGold
                                )
                                Text(
                                    text = "شهر ${event.monthIndex}",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = event.titleArabic,
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (event.isFastingRecommended) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = IslamicGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "سُنة صيام",
                                            fontFamily = TajawalFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IslamicGreen,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = event.descriptionArabic,
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
