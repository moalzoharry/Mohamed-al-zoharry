package com.example.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.ReverentRecitation
import com.example.data.model.ReverentRecitationsData
import com.example.ui.components.ContentProtectionBadge
import com.example.ui.components.ContentProtectionWarningDialog
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReverentRecitationsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    fun checkIsOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    var isOnline by remember { mutableStateOf(checkIsOnline()) }
    var currentPlayingId by remember { mutableStateOf<Int?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(1) }
    var showProtectionDialog by remember { mutableStateOf(false) }

    // Notification toggle for reverent recitations
    val prefs = remember { context.getSharedPreferences("reverent_recitations_prefs", Context.MODE_PRIVATE) }
    var notificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean("reverent_notifs_enabled", true))
    }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun playRecitation(recitation: ReverentRecitation) {
        if (!isOnline) {
            Toast.makeText(context, "يرجى الاتصال بالإنترنت للاستماع للتلاوات النادرة", Toast.LENGTH_LONG).show()
            return
        }

        try {
            mediaPlayer?.release()
            isBuffering = true
            currentPlayingId = recitation.id
            isPlaying = false

            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(recitation.audioUrl)
                setOnPreparedListener { prepared ->
                    isBuffering = false
                    durationMs = prepared.duration.coerceAtLeast(1)
                    prepared.start()
                    isPlaying = true
                }
                setOnCompletionListener {
                    isPlaying = false
                    progress = 1f
                }
                setOnErrorListener { _, _, _ ->
                    isBuffering = false
                    isPlaying = false
                    Toast.makeText(context, "تعذر تشغيل التلاوة، جاري المحاولة مرة أخرى", Toast.LENGTH_SHORT).show()
                    true
                }
            }
            mediaPlayer = mp
            mp.prepareAsync()
        } catch (e: Exception) {
            isBuffering = false
            isPlaying = false
            Toast.makeText(context, "خطأ في الاتصال بالبث الصوتي", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    val pos = mp.currentPosition
                    val dur = mp.duration.coerceAtLeast(1)
                    currentPositionMs = pos
                    durationMs = dur
                    progress = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                }
            }
            kotlinx.coroutines.delay(500)
        }
    }

    if (showProtectionDialog) {
        ContentProtectionWarningDialog(
            onDismissRequest = { showProtectionDialog = false },
            targetItemName = "التلاوات الخاشعة والنادرة"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "التلاوات الخاشعة والنادرة",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "روائع التسجيلات التاريخية لكبار قراء العالم الإسلامي",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = QuranGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("reverent_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    ContentProtectionBadge(
                        onClick = { showProtectionDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("reverent_recitations_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Internet Status Banner
            item {
                Spacer(modifier = Modifier.height(4.dp))
                if (!isOnline) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.12f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444).copy(alpha = 0.4f))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "أنت غير متصل بالإنترنت حالياً",
                                        fontFamily = TajawalFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                    Text(
                                        text = "تتطلب التلاوات النادرة اتصالاً نشطاً بالشبكة للاستماع المباشر.",
                                        fontFamily = TajawalFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { isOnline = checkIsOnline() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "إعادة فحص", fontFamily = TajawalFontFamily, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    // Online connection badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IslamicGreen.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "متصل بالإنترنت: البث الصوتي المباشر للتلاوات النادرة عالي النقاء جاهز",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = IslamicGreen
                            )
                        }
                    }
                }
            }

            // Notification Subscription Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = QuranGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "إشعار التلاوة الخاشعة اليومية",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تلقي تذكير بتلاوة نادرة ترطب قلبك وتذكرك بالقرآن",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                                prefs.edit().putBoolean("reverent_notifs_enabled", it).apply()
                                val msg = if (it) "تم تفعيل إشعارات التلاوات الخاشعة" else "تم إيقاف إشعارات التلاوات الخاشعة"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Active Player Bar if playing
            item {
                val activeItem = ReverentRecitationsData.list.firstOrNull { it.id == currentPlayingId }
                if (activeItem != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = IslamicGreen.copy(alpha = 0.15f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(IslamicGreen)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(IslamicGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Headphones, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = activeItem.title,
                                            fontFamily = TajawalFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${activeItem.reciterNameArabic} • ${activeItem.surahName}",
                                            fontFamily = TajawalFontFamily,
                                            fontSize = 11.sp,
                                            color = IslamicGreen
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Play / Pause
                                    Surface(
                                        shape = CircleShape,
                                        color = IslamicGreen,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clickable {
                                                mediaPlayer?.let { mp ->
                                                    if (mp.isPlaying) {
                                                        mp.pause()
                                                        isPlaying = false
                                                    } else {
                                                        mp.start()
                                                        isPlaying = true
                                                    }
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isBuffering) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = QuranGold)
                            } else {
                                Slider(
                                    value = progress,
                                    onValueChange = { newProg ->
                                        progress = newProg
                                        mediaPlayer?.let { mp ->
                                            val target = (newProg * durationMs).toInt()
                                            mp.seekTo(target)
                                        }
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = QuranGold,
                                        activeTrackColor = IslamicGreen
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recitations List
            items(ReverentRecitationsData.list, key = { it.id }) { item ->
                val isSelected = currentPlayingId == item.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) IslamicGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = if (isSelected) androidx.compose.ui.graphics.SolidColor(IslamicGreen)
                        else androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { playRecitation(item) }
                        .testTag("recitation_item_${item.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) IslamicGreen else QuranGold.copy(alpha = 0.18f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isSelected && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else QuranGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.reciterNameArabic,
                                        fontFamily = TajawalFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = IslamicGreen.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = item.tag,
                                            fontFamily = TajawalFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IslamicGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = item.title,
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = item.description,
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 10.sp,
                                    color = QuranGold,
                                    maxLines = 1
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = item.durationText,
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
