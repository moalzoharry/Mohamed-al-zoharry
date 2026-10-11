package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
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
import androidx.compose.runtime.collectAsState
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
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaaKhatmScreen(
    viewModel: QuranViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val theme = settings.theme
    val textColor = Color(theme.textColorHex)
    val bgColor = Color(theme.backgroundColorHex)
    val surfaceColor = Color(theme.surfaceColorHex)

    val duaaParagraphs = listOf(
        "صَدَقَ اللَّهُ الْعَظِيمُ الَّذِي لَا إِلَهَ إِلَّا هُوَ، الْمُتَوَحِّدُ فِي الْجَلَالِ بِكَمَالِ الْجَمَالِ، تَعْظِيمًا وَتَكْبِيرًا، صَدَقَ اللَّهُ الْعَلِيُّ الْعَظِيمُ وَبَلَّغَ رَسُولُهُ الْكَرِيمُ، وَنَحْنُ عَلَى ذَلِكَ مِنَ الشَّاهِدِينَ الشَّاكِرِينَ.",
        "اللَّهُمَّ ارْحَمْنَا بِالْقُرْآنِ، وَاجْعَلْهُ لَنَا إِمَامًا وَنُورًا وَهُدًى وَرَحْمَةً.",
        "اللَّهُمَّ ذَكِّرْنَا مِنْهُ مَا نَسِينَا، وَعَلِّمْنَا مِنْهُ مَا جَهِلْنَا، وَارْزُقْنَا تِلَاوَتَهُ آنَاءَ اللَّيْلِ وَأَطْرَافَ النَّهَارِ، وَاجْعَلْهُ لَنَا حُجَّةً يَا رَبَّ الْعَالَمِينَ.",
        "اللَّهُمَّ أَصْلِحْ لَنَا دِينَنَا الَّذِي هُوَ عِصْمَةُ أَمْرِنَا، وَأَصْلِحْ لَنَا دُنْيَانَا الَّتِي فِيهَا مَعَاشُنَا، وَأَصْلِحْ لَنَا آخِرَتَنَا الَّتِي إِلَيْهَا مَعَادُنَا، وَاجْعَلِ الْحَيَاةَ زِيَادَةً لَنَا فِي كُلِّ خَيْرٍ، وَاجْعَلِ الْمَوْتَ رَاحَةً لَنَا مِنْ كُلِّ شَرٍّ.",
        "اللَّهُمَّ اجْعَلْ خَيْرَ أَعْمَارِنَا أَوَاخِرَهَا، وَخَيْرَ أَعْمَالِنَا خَوَاتِيمَهَا، وَخَيْرَ أَيَّامِنَا يَوْمَ نَلْقَاكَ فِيهِ.",
        "اللَّهُمَّ إِنَّا نَسْأَلُكَ عِيشَةً هَنِيَّةً، وَمِيتَةً سَوِيَّةً، وَمَرَدًّا غَيْرَ مُخْزٍ وَلَا فَاضِحٍ.",
        "اللَّهُمَّ إِنَّا نَسْأَلُكَ مُوجِبَاتِ رَحْمَتِكَ، وَعَزَائِمَ مَغْفِرَتِكَ، وَالسَّلَامَةَ مِنْ كُلِّ إِثْمٍ، وَالْغَنِيمَةَ مِنْ كُلِّ بِرٍّ، وَالْفَوْزَ بِالْجَنَّةِ، وَالنَّجَاةَ مِنَ النَّارِ.",
        "اللَّهُمَّ أَحْسِنْ عَاقِبَتَنَا فِي الْأُمُورِ كُلِّهَا، وَأَجِرْنَا مِنْ خِزْيِ الدُّنْيَا وَعَذَابِ الْآخِرَةِ.",
        "اللَّهُمَّ لَا تَدَعْ لَنَا ذَنْبًا إِلَّا غَفَرْتَهُ، وَلَا هَمًّا إِلَّا فَرَّجْتَهُ، وَلَا دَيْنًا إِلَّا قَضَيْتَهُ، وَلَا مَرِيضًا إِلَّا شَفَيْتَهُ، وَلَا مُبْتَلًى إِلَّا عَافَيْتَهُ، وَلَا ضَالًّا إِلَّا هَدَيْتَهُ، وَلَا مَيِّتًا إِلَّا رَحِمْتَهُ، وَلَا حَاجَةً مِنْ حَوَائِجِ الدُّنْيَا وَالْآخِرَةِ هِيَ لَكَ رِضًا وَلَنَا فِيهَا صَلَاحٌ إِلَّا قَضَيْتَهَا وَيَسَّرْتَهَا يَا أَرْحَمَ الرَّاحِمِينَ.",
        "اللَّهُمَّ اغْفِرْ لِجَمِيعِ مَوْتَى الْمُسْلِمِينَ الَّذِينَ شَهِدُوا لَكَ بِالْوَحْدَانِيَّةِ وَلِنَبِيِّكَ بِالرِّسَالَةِ وَمَاتُوا عَلَى ذَلِكَ، اللَّهُمَّ اغْفِرْ لَهُمْ وَارْحَمْهُمْ، وَعَافِهِمْ وَاعْفُ عَنْهُمْ، وَأَكْرِمْ نُزُلَهُمْ، وَوَسِّعْ مُدْخَلَهُمْ، وَاغْسِلْهُمْ بِالْمَاءِ وَالثَّلْجِ وَالْبَرَدِ، وَنَقِّهِمْ مِنَ الذُّنُوبِ وَالْخَطَايَا كَمَا يُنَقَّى الثَّوْبُ الْأَبْيَضُ مِنَ الدَّنَسِ، وَارْحَمْنَا اللَّهُمَّ إِذَا صِرْنَا إِلَى مَا صَارُوا إِلَيْهِ تَحْتَ الْجَنَادِلِ وَالتُّرَابِ وَحْدَنَا.",
        "اللَّهُمَّ ارْحَمْ آبَاءَنَا وَأُمَّهَاتِنَا وَمَشَايِخَنَا وَمَنْ عَلَّمَنَا وَمَنْ أَحْسَنَ إِلَيْنَا وَمَنْ أَوْصَانَا بِالدُّعَاءِ، وَارْحَمْ جَمِيعَ أَمْوَاتِ الْمُسْلِمِينَ وَاحْفَظْ وَوَفِّقْ أَحْيَاءَهُمْ لِمَا تُحِبُّ وَتَرْضَى.",
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ، وَصَلَّى اللَّهُ عَلَى سَيِّدِنَا مُحَمَّدٍ وَعَلَى آلِهِ وَصَحْبِهِ وَسَلَّمَ تَسْلِيمًا كَثِيرًا، وَالْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ."
    )

    val fullDuaaText = duaaParagraphs.joinToString("\n\n")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "دعاء ختم القرآن الكريم",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("دعاء ختم القرآن", fullDuaaText))
                            Toast.makeText(context, "تم نسخ دعاء ختم القرآن الكريم", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "نسخ الدعاء",
                            tint = textColor
                        )
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "﴿ دعاء ختم القرآن الكريم ﴾\n\n$fullDuaaText\n\nتطبيق المصحف الرقمي")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة دعاء ختم القرآن"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor
                )
            )
        },
        modifier = modifier.testTag("duaa_khatm_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Ornament
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(QuranGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "۞",
                            fontSize = 24.sp,
                            color = QuranGold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "دُعَاءُ خَتْمِ القُرْآنِ الكَرِيمِ",
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(theme.accentColorHex)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مأثور ومستحب عند ختم كتاب الله عز وجل",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        color = Color(theme.secondaryTextHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Supplications paragraphs
            duaaParagraphs.forEachIndexed { index, paragraph ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = surfaceColor
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(theme.accentColorHex).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontFamily = TajawalFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(theme.accentColorHex)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = paragraph,
                            fontFamily = AmiriFontFamily,
                            fontSize = (settings.tafsirFontSize + 3f).sp,
                            lineHeight = ((settings.tafsirFontSize + 3f) * 1.8f).sp,
                            color = textColor,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
