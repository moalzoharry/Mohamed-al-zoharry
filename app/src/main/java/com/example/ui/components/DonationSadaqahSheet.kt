package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.QuranGoldLight
import com.example.ui.theme.TajawalFontFamily

enum class DonationLanguage(val code: String, val label: String) {
    ARABIC("ar", "العربية"),
    ENGLISH("en", "English"),
    FRENCH("fr", "Français"),
    URDU("ur", "اردو"),
    INDONESIAN("id", "Indonesia")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationSadaqahSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var selectedLang by remember { mutableStateOf(DonationLanguage.ARABIC) }
    var selectedTierIndex by remember { mutableStateOf(1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
                .testTag("donation_sadaqah_sheet")
        ) {
            // Language selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = QuranGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اللغة / Language:",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(DonationLanguage.values()) { lang ->
                        FilterChip(
                            selected = selectedLang == lang,
                            onClick = { selectedLang = lang },
                            label = { Text(lang.label, fontSize = 11.sp, fontFamily = TajawalFontFamily) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IslamicGreen.copy(alpha = 0.15f),
                                selectedLabelColor = IslamicGreen
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Header Hero Banner with Quranic Ayah
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(IslamicGreen, Color(0xFF073822), Color(0xFF031A0F))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = QuranGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    tint = QuranGoldLight,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = when (selectedLang) {
                                DonationLanguage.ARABIC -> "المساهمة في الصدقة الجارية وتطوير الفرقان"
                                DonationLanguage.ENGLISH -> "Sadaqah Jariyah & App Continuity Support"
                                DonationLanguage.FRENCH -> "Sadaqah Jariyah & Soutien au Projet"
                                DonationLanguage.URDU -> "صدقہ جاریہ اور ایپ سپورٹ"
                                DonationLanguage.INDONESIAN -> "Dukungan Sedekah Jariyah & Aplikasi"
                            },
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = QuranGoldLight,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "﴿ وَمَا تُقَدِّمُوا لِأَنفُسِكُم مِّنْ خَيْرٍ تَجِدُوهُ عِندَ اللَّهِ هُوَ خَيْرًا وَأَعْظَمَ أَجْرًا ﴾",
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFFFF7DB),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pure statement about not trading in Quran
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = IslamicGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (selectedLang) {
                            DonationLanguage.ARABIC -> "تطبيق الفرقان بلس وقف إسلامي مجاني 100% لوجه الله تعالى وخالٍ تماماً من أي إعلانات. نحن لا نبيع ولا نتربح من كلام الله. هذه المساهمة اختيارية تماماً لمن يريد المشاركة في تكاليف خوادم التلاوات والسيرفرات والتطوير المستمر."
                            DonationLanguage.ENGLISH -> "Al-Furqan Plus is 100% free Waqf for the sake of Allah, completely ad-free. We never monetize or profit from the Quran. Any support is purely voluntary to help cover cloud servers and continuous updates."
                            DonationLanguage.FRENCH -> "Al-Furqan Plus est un Waqf 100% gratuit pour Allah, sans aucune publicité. Tout soutien est facultatif pour couvrir les coûts des serveurs et du développement continu."
                            DonationLanguage.URDU -> "الفرقان پلس اللہ کی رضا کے لیے مکمل مفت اور اشتہارات سے پاک وقف ہے۔ یہ تعاون صرف سرورز اور مسلسل ترقی کے اخراجات میں شرکت کے لیے ہے۔"
                            DonationLanguage.INDONESIAN -> "Al-Furqan Plus adalah wakaf 100% gratis karena Allah, bebas iklan. Dukungan bersifat sukarela untuk biaya server audio dan pengembangan berkelanjutan."
                        },
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Voluntary Tiers
            Text(
                text = when (selectedLang) {
                    DonationLanguage.ARABIC -> "مجالات المساهمة الاختيارية:"
                    DonationLanguage.ENGLISH -> "Optional Support Options:"
                    DonationLanguage.FRENCH -> "Options de soutien:"
                    DonationLanguage.URDU -> "تعاون کے اختیارات:"
                    DonationLanguage.INDONESIAN -> "Pilihan Dukungan:"
                },
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            val tiers = listOf(
                Pair("سيرفرات التلاوات الصوتية والتفاسير", "Audio & Recitation Servers Hosting"),
                Pair("دعم استمرار التطوير ومواقيت الصلاة الدقيقة", "Continuous Updates & Accurate Prayer API"),
                Pair("سهم صدقة جارية ونشر الخير", "General Sadaqah Jariyah Share")
            )

            tiers.forEachIndexed { index, (arTitle, enTitle) ->
                val isSelected = selectedTierIndex == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) IslamicGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) IslamicGreen else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedTierIndex = index }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = if (isSelected) IslamicGreen else QuranGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedLang == DonationLanguage.ARABIC) arTitle else enTitle,
                            fontFamily = TajawalFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary action button (Google Play In-App Billing or Support confirmation)
            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        if (selectedLang == DonationLanguage.ARABIC)
                            "تقبل الله منا ومنكم صالح الأعمال وجعلها صدقة جارية في ميزان حسناتكم"
                        else
                            "May Allah accept your good deeds and grant you continuous rewards",
                        Toast.LENGTH_LONG
                    ).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.VolunteerActivism, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (selectedLang) {
                        DonationLanguage.ARABIC -> "المساهمة الآن (وفق متجر جوجل الرسمي)"
                        DonationLanguage.ENGLISH -> "Contribute Now (via Google Play)"
                        DonationLanguage.FRENCH -> "Contribuer maintenant"
                        DonationLanguage.URDU -> "ابھی تعاون کریں"
                        DonationLanguage.INDONESIAN -> "Berkontribusi Sekarang"
                    },
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SADAQAH OF SHARING BUTTON (الدال على الخير كفاعله)
            OutlinedButton(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "تطبيق الفرقان بلس Al-Furqan Plus الشامل: القرآن الكريم بتفاسيره وتلاواته، مواقيت الصلاة والأذان، اتجاه القبلة، الأذكار والسبحة، وقف مجاني خالص لوجه الله تعالى:\nhttps://play.google.com/store/apps/details?id=${context.packageName}"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة صدقة النشر"))
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = QuranGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (selectedLang) {
                        DonationLanguage.ARABIC -> "صدقة النشر (الدال على الخير كفاعله)"
                        DonationLanguage.ENGLISH -> "Share App (Sadaqah of Spreading Good)"
                        DonationLanguage.FRENCH -> "Partager l'application (Sadaqah)"
                        DonationLanguage.URDU -> "ایپ شیئر کریں (صدقہ جاریہ)"
                        DonationLanguage.INDONESIAN -> "Bagikan Aplikasi (Pahala Sedekah)"
                    },
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
