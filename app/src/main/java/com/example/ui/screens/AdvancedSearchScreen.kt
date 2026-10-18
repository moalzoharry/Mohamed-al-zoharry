package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchType
import com.example.data.model.SearchResult
import com.example.data.repository.QuranStaticData
import com.example.data.repository.SearchFilter
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily
import com.example.ui.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchScreen(
    viewModel: QuranViewModel,
    onBack: () -> Unit,
    onNavigateToAyah: (surahNumber: Int, ayahNumber: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val query by viewModel.searchQuery.collectAsState()
    val filter by viewModel.searchFilter.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val popularKeywords = listOf(
        "الصراط", "الرحمن", "الذين امنوا", "الصلاة", "كتب عليكم الصيام",
        "الله نور السماوات", "لا اله الا هو", "ابراهيم", "يوسف", "موسى",
        "الجنة", "الكوثر", "الرحمة", "الصبر"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "البحث المتقدم في القرآن والتفسير",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("search_back_button")
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
        modifier = modifier.testTag("advanced_search_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Search Text Field
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = {
                    Text(
                        text = "ابحث بدون تشكيل (مثال: الصراط، الرحمن، امنوا)...",
                        fontFamily = TajawalFontFamily,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input_field")
            )

            // Intelligent search indicator banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IslamicGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓ البحث الذكي: يبحث تلقائياً بدون تشكيل وبكافة صور الهمزات (أ، إ، آ / ة، ه / ي، ى)",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = IslamicGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Filter Chips: All / Quran / Jalalayn / Muyassar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchFilter.values().forEach { searchFilter ->
                    val isSelected = filter == searchFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onSearchFilterChanged(searchFilter) },
                        label = {
                            Text(
                                text = searchFilter.titleArabic,
                                fontFamily = TajawalFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("filter_chip_${searchFilter.name}")
                    )
                }
            }

            // 3. Quick Suggestions Tags
            if (query.isBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "كلمات بحث مقترحة ومفاهيم قرآنية:",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularKeywords.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    viewModel.onSearchQueryChanged(tag)
                                }
                            ) {
                                Text(
                                    text = tag,
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Results Header & Count
            if (query.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isSearching) "جاري البحث..." else "نتائج البحث (${results.size})",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            // 5. Results List or Empty State
            if (results.isEmpty() && query.isNotBlank() && !isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "لم يتم العثور على نتائج تطابق \"$query\"",
                            fontFamily = TajawalFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "جرب البحث بكلمة أبسط مثل \"رحمة\" أو بدون ال التعريف، أو تغيير نطاق البحث.",
                            fontFamily = TajawalFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results) { result ->
                        SearchResultItemCard(
                            result = result,
                            onClick = {
                                onNavigateToAyah(result.surahNumber, result.ayahNumber)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItemCard(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("search_result_${result.surahNumber}_${result.ayahNumber}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Surah Name & Ayah Number & Match Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(QuranGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "۞",
                            fontSize = 14.sp,
                            color = QuranGold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سورة ${result.surahName} : آية ${result.ayahNumber}",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Match Category Badge
                val (badgeBg, badgeText) = when (result.matchType) {
                    MatchType.QURAN -> Pair(IslamicGreen.copy(alpha = 0.15f), IslamicGreen)
                    MatchType.JALALAYN -> Pair(QuranGold.copy(alpha = 0.2f), QuranGold)
                    MatchType.MUYASSAR -> Pair(Color(0xFF2563EB).copy(alpha = 0.15f), Color(0xFF1D4ED8))
                    MatchType.SHAARAWI -> Pair(Color(0xFF0284C7).copy(alpha = 0.15f), Color(0xFF0284C7))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = result.matchType.titleArabic,
                        fontFamily = TajawalFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlighted snippet text
            val highlightedText = buildHighlightedString(
                fullText = result.snippet,
                query = result.highlightQuery
            )

            Text(
                text = highlightedText,
                fontFamily = AmiriFontFamily,
                fontSize = if (result.matchType == MatchType.QURAN) 18.sp else 15.sp,
                lineHeight = if (result.matchType == MatchType.QURAN) 28.sp else 23.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Jump CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فتح الآية في المصحف ➔",
                    fontFamily = TajawalFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Builds an AnnotatedString highlighting the query term with a warm amber background and bold weight.
 * Seamlessly matches and highlights even when user searches without Tashkeel!
 */
fun buildHighlightedString(fullText: String, query: String) = buildAnnotatedString {
    val normalizedFull = QuranStaticData.normalizeArabic(fullText)
    val normalizedQuery = QuranStaticData.normalizeArabic(query)

    if (normalizedQuery.isBlank() || !normalizedFull.contains(normalizedQuery)) {
        append(fullText)
        return@buildAnnotatedString
    }

    // 1. Direct exact or case-insensitive match
    val directIndex = fullText.indexOf(query, ignoreCase = true)
    if (directIndex != -1) {
        append(fullText.substring(0, directIndex))
        pushStyle(
            SpanStyle(
                background = QuranGold.copy(alpha = 0.35f),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB45309)
            )
        )
        append(fullText.substring(directIndex, directIndex + query.length))
        pop()
        append(fullText.substring(directIndex + query.length))
        return@buildAnnotatedString
    }

    // 2. Token-level matching across diacritics
    val words = fullText.split(" ")
    val queryTokens = normalizedQuery.split(" ").filter { it.isNotBlank() }
    val qLen = queryTokens.size.coerceAtLeast(1)

    var matchStart = -1
    var matchEnd = -1

    if (words.size >= qLen) {
        for (i in 0..(words.size - qLen)) {
            val window = words.subList(i, i + qLen).joinToString(" ")
            val normWindow = QuranStaticData.normalizeArabic(window)
            if (normWindow.contains(normalizedQuery)) {
                val start = fullText.indexOf(window)
                if (start != -1) {
                    matchStart = start
                    matchEnd = start + window.length
                    break
                }
            }
        }
    }

    if (matchStart != -1 && matchEnd <= fullText.length) {
        append(fullText.substring(0, matchStart))
        pushStyle(
            SpanStyle(
                background = QuranGold.copy(alpha = 0.35f),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB45309)
            )
        )
        append(fullText.substring(matchStart, matchEnd))
        pop()
        append(fullText.substring(matchEnd))
    } else {
        append(fullText)
    }
}
