package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

data class DuaaItem(
    val title: String,
    val arabicTextWithTashkeel: String,
    val sourceOrEvidence: String,
    val meritOrExplanation: String,
    val category: String,
    val badgeIcon: String = "✨"
)

data class ShaarawyGem(
    val situationTitle: String,
    val situationIcon: String,
    val sayingAyah: String,
    val responseAyah: String,
    val surahReference: String,
    val gemReflection: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MustajabDuaaScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("الكنز والـ ١١ كلمة", "روائع الشعراوي", "ماحيات الذنوب", "أوقات الإجابة")

    val infiniteTransition = rememberInfiniteTransition(label = "glow_mustajab")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(QuranGold.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🤲", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "أدعية مستجابة من القرآن والسنة",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "بالأدلة القرآنية والأحاديث النبوية الصحيحة",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = QuranGold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("mustajab_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val fullShare = buildString {
                                appendLine("🤲 أدعية مستجابة من القرآن والسنة النبوية 🤲")
                                appendLine("✨ كنز الـ ١١ كلمة:")
                                appendLine("«سُبْحَانَ اللهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ»")
                                appendLine("\nروائع استجابات الصالحين بالقرآن:")
                                appendLine("١. للضر: ﴿أَنِّي مَسَّنِيَ الضُّرُّ وَأَنْتَ أَرْحَمُ الرَّاحِمِينَ﴾ ➔ ﴿فَاسْتَجَبْنَا لَهُ فَكَشَفْنَا مَا بِهِ مِنْ ضُرٍّ﴾")
                                appendLine("٢. للغم: ﴿لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ﴾ ➔ ﴿فَاسْتَجَبْنَا لَهُ وَنَجَّيْنَاهُ مِنَ الْغَمِّ وَكَذَلِكَ نُنْجِي الْمُؤْمِنِينَ﴾")
                                appendLine("٣. للخوف: ﴿حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ﴾ ➔ ﴿فَانْقَلَبُوا بِنِعْمَةٍ مِنَ اللَّهِ وَفَضْلٍ لَمْ يَمْسَسْهُمْ سُوءٌ﴾")
                                appendLine("\nانشروها، فالدال على الخير كفاعله 🌿")
                            }
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, fullShare)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "نشر الأدعية المستجابة"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة الأدعية",
                            tint = QuranGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = QuranGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = QuranGold,
                        height = 3.dp
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontFamily = TajawalFontFamily,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == index) QuranGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Content per Tab
            when (selectedTab) {
                0 -> GoldenElevenWordsSection(pulseGlow = pulseGlow)
                1 -> ShaarawyGemsSection()
                2 -> ForgivenessAndTasbihSection()
                3 -> TimesOfAcceptanceSection()
            }
        }
    }
}

/**
 * 1. TAB: الـ ١١ كلمة والكنز العظيم
 */
@Composable
private fun GoldenElevenWordsSection(pulseGlow: Float) {
    val context = LocalContext.current
    var dhikrCount by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Optical Illusion / Shimmer Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("eleven_words_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            QuranGold.copy(alpha = pulseGlow),
                            IslamicGreen.copy(alpha = pulseGlow),
                            QuranGold.copy(alpha = pulseGlow)
                        )
                    ),
                    width = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    QuranGold.copy(alpha = 0.12f),
                                    IslamicGreen.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = QuranGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "📿 كنز الحسنات المضاعفة 📿",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = QuranGold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "«عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ»",
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 36.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(QuranGold.copy(alpha = 0.4f))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Explanation Points
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PointRow("✨", "ضِفْ / ضِيفِي الـ ١١ كَلِمَةً دَوْلَ لِأَيِّ ذِكْرٍ تُحِبُّهُ/تُحِبِّينَهُ")
                        PointRow("💫", "وَتَخَيَّلْ / تَخَيَّلِي مِقْدَارَ الثَّوَابِ وَالْحَسَنَاتِ إِنْ شَاءَ اللَّهُ الَّتِي سَيَكْتُبُهَا لَكَ رَبُّنَا")
                        PointRow("➕", "بِالإِضَافَةِ إِلَى ٧٠٠ ضِعْفٍ مِنَ الأَجْرِ الْمُضَاعَفِ")
                        PointRow("🌟", "وَيَا سَلَامَ لَوْ قَالَ رَبُّ الْعِزَّةِ لِلْمَلَائِكَةِ الْكَاتِبِينَ: «اتْرُكُوا ثَوَابَ قَائِلِهَا لِي»")
                        PointRow("😍", "يَا سَعْدَكَ وَيَا هَنَاكَ بِهَذَا الْفَضْلِ الْعَظِيمِ!")
                        PointRow("🤍", "لَا تَنْسَوْنَا مِنْ صَالِحِ دُعَائِكُمْ بِظَهْرِ الْغَيْبِ")
                        PointRow("📢", "انْشُرُوهَا.. فالدَّالُّ عَلَى الْخَيْرِ كَفَاعِلِهِ لَا يَنْقُصُ مِنْ أَجْرِهِ شَيْئًا")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive Counter for the 11 Words
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { dhikrCount++ },
                            colors = ButtonDefaults.buttonColors(containerColor = QuranGold),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text(
                                text = "تسبيح بالـ ١١ كلمة ($dhikrCount)",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val text = "«سُبْحَانَ اللهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ»"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("11 Words", text))
                                Toast.makeText(context, "تم نسخ الكنز العظيم", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = QuranGold)
                        }

                        IconButton(
                            onClick = {
                                val text = "🌟 كنز الـ ١١ كلمة العظيمة 🌟\n«سُبْحَانَ اللهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ»\nضيفها لأي ذكر تحبه وانشرها، فالدال على الخير كفاعله 🌿"
                                val intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(intent, "مشاركة الذكر العظيم"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicGreen)
                        }
                    }
                }
            }
        }

        // Complete Ibrahimiyyah Blessing
        item {
            DuaaCard(
                title = "الصلاة الإبراهيمية العظيمة المباركة",
                arabicText = "«اللَّهُمَّ صَلِّ وَسَلِّمْ وَبَارِكْ عَلَى سَيِّدِنَا وَحَبِيبِنَا وَنَبِيِّنَا مُحَمَّدٍ ﷺ، صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ تَسْلِيمًا كَثِيرًا إِلَى يَوْمِ الدِّينِ، عَدَدَ خَلْقِكَ، وَرِضَا نَفْسِكَ، وَزِنَةَ عَرْشِكَ، وَمِدَادَ كَلِمَاتِكَ»",
                source = "مقرونة بكنز الـ ١١ كلمة",
                merit = "أعظم صيغ الصلاة على النبي المختار، موجبة للشفاعة، ماحية للهموم ومغفرة للذنوب.",
                badgeIcon = "🌺"
            )
        }
    }
}

/**
 * 2. TAB: روائع الصالحين والشيخ الشعراوي بالدليل القرآني
 */
@Composable
private fun ShaarawyGemsSection() {
    val gems = remember {
        listOf(
            ShaarawyGem(
                situationTitle = "عَجِبْتُ لِمَنْ بُلِيَ بِالضُّرِّ.. كَيْفَ يَذْهَلُ عَنْ قَوْلِ:",
                situationIcon = "💔",
                sayingAyah = "﴿ أَنِّي مَسَّنِيَ الضُّرُّ وَأَنْتَ أَرْحَمُ الرَّاحِمِينَ ﴾",
                responseAyah = "﴿ فَاسْتَجَبْنَا لَهُ فَكَشَفْنَا مَا بِهِ مِنْ ضُرٍّ ﴾",
                surahReference = "سورة الأنبياء: آية 84",
                gemReflection = "دعاء سيدنا أيوب عليه السلام بعد سنوات البلاء، فكان الجواب الفوري بالفاء «فاستجبنا له»."
            ),
            ShaarawyGem(
                situationTitle = "عَجِبْتُ لِمَنْ بُلِيَ بِالْغَمِّ وَالْكَرْبِ.. كَيْفَ يَذْهَلُ عَنْ قَوْلِ:",
                situationIcon = "💭",
                sayingAyah = "﴿ لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ ﴾",
                responseAyah = "﴿ فَاسْتَجَبْنَا لَهُ وَنَجَّيْنَاهُ مِنَ الْغَمِّ وَكَذَلِكَ نُنْجِي الْمُؤْمِنِينَ ﴾",
                surahReference = "سورة الأنبياء: آية 88",
                gemReflection = "دعوة ذي النون في بطن الحوت، لم يدعُ بها مكروب إلا فرّج الله عنه بنص الوعد الإلهي: «وكذلك ننجي المؤمنين»."
            ),
            ShaarawyGem(
                situationTitle = "عَجِبْتُ لِمَنْ خَافَ شَيْئًا أَوْ عَدُوًّا.. كَيْفَ يَذْهَلُ عَنْ قَوْلِ:",
                situationIcon = "😨",
                sayingAyah = "﴿ حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ ﴾",
                responseAyah = "﴿ فَانْقَلَبُوا بِنِعْمَةٍ مِنَ اللَّهِ وَفَضْلٍ لَمْ يَمْسَسْهُمْ سُوءٌ ﴾",
                surahReference = "سورة آل عمران: آية 174",
                gemReflection = "قالها إبراهيم عليه السلام حين أُلقي في النار، وقالها محمد ﷺ وصحبه حين قال لهم الناس: إن الناس قد جمعوا لكم."
            ),
            ShaarawyGem(
                situationTitle = "عَجِبْتُ لِمَنْ كُوِيدَ فِي أَمْرٍ أَوْ ظُلِمَ.. كَيْفَ يَذْهَلُ عَنْ قَوْلِ:",
                situationIcon = "😔",
                sayingAyah = "﴿ وَأُفَوِّضُ أَمْرِي إِلَى اللَّهِ إِنَّ اللَّهَ بَصِيرٌ بِالْعِبَادِ ﴾",
                responseAyah = "﴿ فَوَقَاهُ اللَّهُ سَيِّئَاتِ مَا مَكَرُوا ﴾",
                surahReference = "سورة غافر: آية 45",
                gemReflection = "تفويض مطلق لله يحطم كل كيد ومكر بشهادة النجاة والحفظ الرباني الفوري."
            ),
            ShaarawyGem(
                situationTitle = "عَجِبْتُ لِمَنْ أَنْعَمَ اللَّهُ عَلَيْهِ بِنِعْمَةٍ خَافَ زَوَالَهَا.. كَيْفَ يَذْهَلُ عَنْ قَوْلِ:",
                situationIcon = "🌸",
                sayingAyah = "﴿ وَلَوْلَا إِذْ دَخَلْتَ جَنَّتَكَ قُلْتَ مَا شَاءَ اللَّهُ لَا قُوَّةَ إِلَّا بِاللَّهِ ﴾",
                responseAyah = "حصن حصين لبقاء النعم والبركة من الحسد والزوال",
                surahReference = "سورة الكهف: آية 39",
                gemReflection = "اعتراف بالمنعم وحفظ للرزق والأهل والأولاد والمال."
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💎", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "قَالَ أَحَدُ الصَّالِحِينَ وَالشَّيْخُ مُحَمَّد مَتْوَلِّي الشَّعْرَاوِيُّ رَحِمَهُ اللَّهُ:",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "خمسة مفاتيح قرآنية عجيبة تفتح كل كرب وضيق بالدليل القطعي من كتاب الله.",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(gems.size) { index ->
            val gem = gems[index]
            ShaarawyGemCard(gem = gem)
        }
    }
}

@Composable
private fun ShaarawyGemCard(gem: ShaarawyGem) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(QuranGold.copy(alpha = 0.6f), IslamicGreen.copy(alpha = 0.4f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = gem.situationIcon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = gem.situationTitle,
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // The Ayah To Say
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = QuranGold.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = gem.sayingAyah,
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Response Guarantee from Allah
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🌿 وَاللَّهُ تَعَالَى يَقُولُ بَعْدَهَا:", fontFamily = TajawalFontFamily, fontSize = 12.sp, color = IslamicGreen, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IslamicGreen.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = gem.responseAyah,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = IslamicGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📗 [${gem.surahReference}]",
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuranGold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = gem.gemReflection,
                fontFamily = TajawalFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        val shareText = "${gem.situationTitle}\n${gem.sayingAyah}\nوالله تعالى يقول بعدها:\n${gem.responseAyah}\n[${gem.surahReference}]\n\nدرر الصالحين والشعراوي - تطبيق الفرقان بلس | Al-Furqan Plus"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Gem", shareText))
                        Toast.makeText(context, "تم نسخ الدرّة القرآنية", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = {
                        val shareText = "${gem.situationTitle}\n${gem.sayingAyah}\nوالله تعالى يقول بعدها:\n${gem.responseAyah}\n[${gem.surahReference}]\n\nدرر الصالحين والشيخ الشعراوي 🌿"
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "مشاركة الدرّة القرآنية"))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = QuranGold, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/**
 * 3. TAB: ماحيات الذنوب وتسبيح الملائكة
 */
@Composable
private fun ForgivenessAndTasbihSection() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            DuaaCard(
                title = "مَاحِيَاتٌ لِلذُّنُوبِ وَثَقِيلَتَانِ فِي الْمِيزَانِ",
                arabicText = "«سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ»",
                source = "صحيح البخاري ومسلم",
                merit = "قال رسول الله ﷺ: «كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي الْمِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ».",
                badgeIcon = "🌟"
            )
        }

        item {
            DuaaCard(
                title = "تَسْبِيحُ الْمَلَائِكَةِ وَحَمَلَةِ الْعَرْشِ",
                arabicText = "«سُبُّوحٌ قُدُّوسٌ رَبُّ الْمَلَائِكَةِ وَالرُّوحِ»",
                source = "صحيح مسلم",
                merit = "كان النبي ﷺ يكثر من قولها في ركوعه وسجوده، وهي تسبيح الملائكة الكرام تنزيهاً لله عز وجل.",
                badgeIcon = "🕌"
            )
        }

        item {
            DuaaCard(
                title = "فَضْلُ سُورَةِ الإِخْلَاصِ ﴿قُلْ هُوَ اللَّهُ أَحَدٌ﴾",
                arabicText = "﴿ قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ ﴾",
                source = "صحيح البخاري ومسلم",
                merit = "أَنَّهَا تَعْدِلُ ثُلُثَ الْقُرْآنِ الْكَرِيمِ، وَحُبُّهَا يُوجِبُ مَحَبَّةَ اللَّهِ وَدُخُولَ الْجَنَّةِ.",
                badgeIcon = "📖"
            )
        }

        item {
            DuaaCard(
                title = "فَضْلُ الآيَتَيْنِ مِنْ آخِرِ سُورَةِ الْبَقَرَةِ",
                arabicText = "﴿ آمَنَ الرَّسُولُ بِمَا أُنْزِلَ إِلَيْهِ مِنْ رَبِّهِ وَالْمُؤْمِنُونَ... إِلَى آخِرِ السُّورَةِ ﴾",
                source = "متفق عليه (البخاري ومسلم)",
                merit = "عَنْ أَبِي مَسْعُودٍ الْبَدْرِيِّ رَضِيَ اللَّهُ عَنْهُ عَنِ النَّبِيِّ ﷺ قَالَ: «مَنْ قَرَأَ بِالْآيَتَيْنِ مِنْ آخِرِ سُورَةِ الْبَقَرَةِ فِي لَيْلَةٍ كَفَتَاهُ» (أَيْ كَفَتَاهُ مِنْ كُلِّ سُوءٍ وَشَرٍّ وَشَيْطَانٍ).",
                badgeIcon = "🌙"
            )
        }
    }
}

/**
 * 4. TAB: أوقات وأحوال إجابة الدعاء
 */
@Composable
private fun TimesOfAcceptanceSection() {
    val times = remember {
        listOf(
            DuaaItem(
                title = "١. ثلث الليل الأخير (وقت النزول الإلهي)",
                arabicTextWithTashkeel = "«يَنْزِلُ رَبُّنَا تَبَارَكَ وَتَعَالَى كُلَّ لَيْلَةٍ إِلَى السَّمَاءِ الدُّنْيَا حِينَ يَبْقَى ثُلُثُ اللَّيْلِ الآخِرُ يَقُولُ: مَنْ يَدْعُونِي فَأَسْتَجِيبَ لَهُ، مَنْ يَسْأَلُنِي فَأُعْطِيَهُ، مَنْ يَسْتَغْفِرُنِي فَأَغْفِرَ لَهُ»",
                sourceOrEvidence = "متفق عليه",
                meritOrExplanation = "أعظم أوقات القرب والفتوحات، حين يخلو الحبيب بحبيبه في جوف الليل.",
                category = "أوقات"
            ),
            DuaaItem(
                title = "٢. بين الأذان والإقامة",
                arabicTextWithTashkeel = "«الدُّعَاءُ لَا يُرَدُّ بَيْنَ الأَذَانِ وَالإِقَامَةِ»",
                sourceOrEvidence = "رواه أبو داود والترمذي وصححه الألباني",
                meritOrExplanation = "وقت مبارك بعد نداء الحق، فاغتنم تلك الدقائق الذهبية بالدعاء وسؤال الحاجات.",
                category = "أوقات"
            ),
            DuaaItem(
                title = "٣. حال السجود في الصلاة",
                arabicTextWithTashkeel = "«أَقْرَبُ مَا يَكُونُ الْعَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ، فَأَكْثِرُوا الدُّعَاءَ»",
                sourceOrEvidence = "صحيح مسلم",
                meritOrExplanation = "قمة التذلل والخضوع بين يدي الخالق، فيكون الدعاء أرجى للقبول.",
                category = "أحوال"
            ),
            DuaaItem(
                title = "٤. آخر ساعة بعد العصر يوم الجمعة",
                arabicTextWithTashkeel = "«يَوْمُ الْجُمُعَةِ اثْنَتَا عَشْرَةَ سَاعَةً، لَا يُوجَدُ فِيهَا عَبْدٌ مُسْلِمٌ يَسْأَلُ اللَّهَ شَيْئًا إِلَّا آتَاهُ إِيَّاهُ، فَالْتَمِسُوهَا آخِرَ سَاعَةٍ بَعْدَ الْعَصْرِ»",
                sourceOrEvidence = "رواه أبو داود والنسائي",
                meritOrExplanation = "ساعة الاستجابة في خير يوم طلعت عليه الشمس.",
                category = "أوقات"
            ),
            DuaaItem(
                title = "٥. عند نزول الغيث (المطر)",
                arabicTextWithTashkeel = "«ثِنْتَانِ مَا تُرَدَّانِ: الدُّعَاءُ عِنْدَ النِّدَاءِ، وَتَحْتَ الْمَطَرِ»",
                sourceOrEvidence = "رواه أبو داود والحاكم وصححه الألباني",
                meritOrExplanation = "وقت تنزل الرحمات والبركات من السماء.",
                category = "أحوال"
            ),
            DuaaItem(
                title = "٦. عند إفطار الصائم",
                arabicTextWithTashkeel = "«ثَلَاثَةٌ لَا تُرَدُّ دَعْوَتُهُمْ: الصَّائِمُ حَتَّى يُفْطِرَ، وَالإِمَامُ الْعَادِلُ، وَدَعْوَةُ الْمَظْلُومِ»",
                sourceOrEvidence = "رواه الترمذي وابن ماجه",
                meritOrExplanation = "فرحة الصوم وكسار النفس لله عز وجل.",
                category = "أحوال"
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = QuranGold.copy(alpha = 0.12f),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⏳", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "مَوَاطِنُ وَأَوْقَاتُ إِجَابَةِ الدُّعَاءِ الْمُسْتَحَبَّةُ:",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "أوقات نبوية شريفة ثبت بالحديث الصحيح فتح أبواب السماء فيها.",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(times.size) { index ->
            val item = times[index]
            DuaaCard(
                title = item.title,
                arabicText = item.arabicTextWithTashkeel,
                source = item.sourceOrEvidence,
                merit = item.meritOrExplanation,
                badgeIcon = "⏰"
            )
        }
    }
}

@Composable
private fun DuaaCard(
    title: String,
    arabicText: String,
    source: String,
    merit: String,
    badgeIcon: String
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = badgeIcon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = QuranGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = source,
                        fontFamily = TajawalFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuranGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = arabicText,
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 30.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "✨ الفضل والبيان: $merit",
                fontFamily = TajawalFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        val text = "$title\n$arabicText\n[$source]\n$merit\n\nتطبيق الفرقان بلس | Al-Furqan Plus"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Duaa", text))
                        Toast.makeText(context, "تم نسخ الذكر والدعاء", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = {
                        val text = "$title\n$arabicText\n[$source]\n$merit\n\nانشرها، فالدال على الخير كفاعله 🌿"
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "مشاركة الدعاء المبارك"))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = QuranGold, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun PointRow(icon: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontFamily = TajawalFontFamily,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
