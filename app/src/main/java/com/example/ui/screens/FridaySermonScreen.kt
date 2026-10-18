package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.sermon.FridaySermonItem
import com.example.sermon.FridaySermonManager
import com.example.sermon.RecordingState
import com.example.sermon.SermonAudioQuality
import com.example.ui.components.ContentProtectionBadge
import com.example.ui.components.ContentProtectionWarningDialog
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridaySermonScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    val manager = remember { FridaySermonManager(context) }
    val recordingState by manager.recordingState.collectAsState()
    val sermonsList by manager.sermonsList.collectAsState()
    val selectedQuality by manager.selectedQualityFlow.collectAsState()

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showQualitySheet by remember { mutableStateOf(false) }
    val qualitySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var sermonTitle by remember { mutableStateOf("") }
    var mosqueName by remember { mutableStateOf("") }
    var speakerName by remember { mutableStateOf("") }
    var saveToExternal by remember { mutableStateOf(false) } // false = Internal, true = External SD

    var playingSermonId by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var playbackDurationMs by remember { mutableIntStateOf(0) }
    var currentPlaybackMs by remember { mutableIntStateOf(0) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var showProtectionDialog by remember { mutableStateOf(false) }

    // Audio recording permission launcher
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
        if (granted) {
            val started = manager.startRecording(useExternalStorage = saveToExternal, quality = selectedQuality)
            if (started) {
                elapsedSeconds = 0L
                Toast.makeText(context, "بدأ تسجيل خطبة الجمعة الآن...", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "يتطلب تسجيل الخطبة إذن الميكروفون", Toast.LENGTH_LONG).show()
        }
    }

    // Elapsed timer while recording
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.RECORDING) {
            while (true) {
                delay(1000)
                elapsedSeconds += 1
            }
        }
    }

    // Playback progress updater
    LaunchedEffect(isPlaying, playingSermonId) {
        if (isPlaying && mediaPlayer != null) {
            while (true) {
                try {
                    val current = mediaPlayer?.currentPosition ?: 0
                    val total = mediaPlayer?.duration ?: 1
                    currentPlaybackMs = current
                    playbackDurationMs = total
                    playbackProgress = if (total > 0) current.toFloat() / total.toFloat() else 0f
                } catch (e: Exception) {}
                delay(500)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
            if (recordingState != RecordingState.IDLE) {
                manager.cancelRecording()
            }
        }
    }

    fun playSermon(item: FridaySermonItem) {
        if (playingSermonId == item.id && isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
        } else if (playingSermonId == item.id && !isPlaying && mediaPlayer != null) {
            mediaPlayer?.start()
            isPlaying = true
        } else {
            mediaPlayer?.release()
            mediaPlayer = null
            try {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(item.filePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        isPlaying = false
                        playbackProgress = 0f
                        playingSermonId = null
                    }
                }
                mediaPlayer = mp
                playingSermonId = item.id
                isPlaying = true
                playbackDurationMs = mp.duration
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "تعذر تشغيل التسجيل", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareSermon(item: FridaySermonItem) {
        try {
            val file = File(item.filePath)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, item.title)
                putExtra(Intent.EXTRA_TEXT, "${item.title}\nمسجد: ${item.mosqueName} - الخطيب: ${item.speakerName}\nتطبيق الفرقان بلس")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة تسجيل خطبة الجمعة"))
        } catch (e: Exception) {
            // Fallback generic send
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "تسجيل خطبة الجمعة: ${item.title}\nمسجد ${item.mosqueName}")
            }
            context.startActivity(Intent.createChooser(sendIntent, "مشاركة خطبة الجمعة"))
        }
    }

    fun formatSeconds(sec: Long): String {
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }

    fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return "%.1f ميجابايت".format(mb)
    }

    // Animation for recording pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "تسجيل خطبة الجمعة",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "أرشيف ونقاء صوتي مع خيار الحفظ بالهاتف أو الذاكرة",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("friday_sermon_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع"
                        )
                    }
                },
                actions = {
                    ContentProtectionBadge(
                        onClick = { showProtectionDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. RECORDING STUDIO CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (recordingState) {
                        RecordingState.RECORDING -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                        RecordingState.PAUSED -> QuranGold.copy(alpha = 0.15f)
                        RecordingState.IDLE -> IslamicGreen.copy(alpha = 0.08f)
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    when (recordingState) {
                        RecordingState.RECORDING -> MaterialTheme.colorScheme.error
                        RecordingState.PAUSED -> QuranGold
                        RecordingState.IDLE -> IslamicGreen.copy(alpha = 0.4f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (recordingState) {
                        RecordingState.IDLE -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(IslamicGreen)
                                    .clickable {
                                        if (hasRecordPermission) {
                                            val ok = manager.startRecording(
                                                useExternalStorage = saveToExternal,
                                                quality = selectedQuality
                                            )
                                            if (ok) elapsedSeconds = 0L
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                    .testTag("start_recording_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "بدء التسجيل",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "اضغط لبدء تسجيل خطبة الجمعة",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تسجيل بنقاء عالي وحفظ فوري بأعلى جودة وأقل حجم",
                                fontFamily = TajawalFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 1. Audio Quality & Format Selector
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, QuranGold.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showQualitySheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = "🎙️", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = selectedQuality.titleArabic,
                                                    fontFamily = TajawalFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (selectedQuality.isRecommended) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = IslamicGreen.copy(alpha = 0.2f)
                                                    ) {
                                                        Text(
                                                            text = "موصى به",
                                                            fontFamily = TajawalFontFamily,
                                                            fontSize = 9.sp,
                                                            color = IslamicGreen,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${selectedQuality.estimatedSizePerMinute} • ${selectedQuality.descriptionArabic}",
                                                fontFamily = TajawalFontFamily,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Text(
                                        text = "تغيير النسق",
                                        fontFamily = TajawalFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = QuranGold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 2. Storage location quick toggle before recording
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { saveToExternal = !saveToExternal }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (saveToExternal) Icons.Default.SdCard else Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = QuranGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (saveToExternal) "وجهة الحفظ: الذاكرة الخارجية (بطاقة SD)" else "وجهة الحفظ: ذاكرة الهاتف الداخلية",
                                            fontFamily = TajawalFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "تبديل",
                                        fontFamily = TajawalFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = IslamicGreen
                                    )
                                }
                            }
                        }

                        RecordingState.RECORDING, RecordingState.PAUSED -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .scale(if (recordingState == RecordingState.RECORDING) pulseScale else 1f)
                                    .clip(CircleShape)
                                    .background(if (recordingState == RecordingState.RECORDING) MaterialTheme.colorScheme.error else QuranGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (recordingState == RecordingState.RECORDING) Icons.Default.Mic else Icons.Default.Pause,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = formatSeconds(elapsedSeconds),
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                color = if (recordingState == RecordingState.RECORDING) MaterialTheme.colorScheme.error else QuranGold
                            )

                            Text(
                                text = if (recordingState == RecordingState.RECORDING) "جاري تسجيل الخطبة بصوت نقي..." else "التسجيل متوقف مؤقتاً",
                                fontFamily = TajawalFontFamily,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Recording Controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Pause / Resume
                                IconButton(
                                    onClick = {
                                        if (recordingState == RecordingState.RECORDING) {
                                            manager.pauseRecording()
                                        } else {
                                            manager.resumeRecording()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(
                                        imageVector = if (recordingState == RecordingState.RECORDING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "إيقاف مؤقت",
                                        tint = QuranGold
                                    )
                                }

                                // Stop & Save
                                Button(
                                    onClick = {
                                        sermonTitle = "خطبة الجمعة"
                                        mosqueName = ""
                                        speakerName = ""
                                        showSaveDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إنهاء وحفظ الخطبة", fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold)
                                }

                                // Cancel
                                IconButton(
                                    onClick = {
                                        manager.cancelRecording()
                                        Toast.makeText(context, "تم إلغاء التسجيل", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "إلغاء",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. SAVED SERMONS ARCHIVE LIST
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "أرشيف خطب الجمعة المسجلة (${sermonsList.size}):",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = QuranGold
                        )
                    }
                }

                if (sermonsList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🕌", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "لا توجد خطب مسجلة حتى الآن",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "عند ذهابك لصلاة الجمعة المباركة، اضغط على زر التسجيل لتوثيق الخطبة والاستماع إليها لاحقاً ومشاركتها.",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(sermonsList, key = { it.id }) { item ->
                        val isThisPlaying = (playingSermonId == item.id && isPlaying)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sermon_item_${item.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isThisPlaying) QuranGold.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isThisPlaying) QuranGold else MaterialTheme.colorScheme.outlineVariant
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(if (isThisPlaying) QuranGold else IslamicGreen)
                                                .clickable { playSermon(item) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isThisPlaying) "إيقاف" else "تشغيل",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = item.title,
                                                fontFamily = TajawalFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "المسجد: ${item.mosqueName} • الخطيب: ${item.speakerName}",
                                                fontFamily = TajawalFontFamily,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${item.dateFormatted} • ${formatSeconds(item.durationSeconds.toLong())} • ${formatBytes(item.sizeBytes)}",
                                                    fontFamily = TajawalFontFamily,
                                                    fontSize = 10.sp,
                                                    color = QuranGold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (item.isExternalStorage) QuranGold.copy(alpha = 0.2f) else IslamicGreen.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = if (item.isExternalStorage) "ذاكرة SD" else "ذاكرة الهاتف",
                                                        fontFamily = TajawalFontFamily,
                                                        fontSize = 9.sp,
                                                        color = if (item.isExternalStorage) QuranGold else IslamicGreen,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                ) {
                                                    Text(
                                                        text = item.qualityTitle,
                                                        fontFamily = TajawalFontFamily,
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { shareSermon(item) }) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "مشاركة",
                                                tint = IslamicGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (playingSermonId == item.id) {
                                                    mediaPlayer?.stop()
                                                    isPlaying = false
                                                    playingSermonId = null
                                                }
                                                manager.deleteSermon(item.id)
                                                Toast.makeText(context, "تم حذف الخطبة", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                if (isThisPlaying) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Slider(
                                        value = playbackProgress,
                                        onValueChange = { newProgress ->
                                            playbackProgress = newProgress
                                            mediaPlayer?.let { mp ->
                                                val targetMs = (newProgress * mp.duration).toInt()
                                                mp.seekTo(targetMs)
                                            }
                                        },
                                        colors = SliderDefaults.colors(
                                            thumbColor = QuranGold,
                                            activeTrackColor = QuranGold
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatSeconds((currentPlaybackMs / 1000).toLong()),
                                            fontFamily = TajawalFontFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatSeconds((playbackDurationMs / 1000).toLong()),
                                            fontFamily = TajawalFontFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }

    // Save sermon dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "حفظ وتوثيق خطبة الجمعة",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "عنوان الخطبة وموضوعها:",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = sermonTitle,
                        onValueChange = { sermonTitle = it },
                        placeholder = { Text("مثال: فضل بر الوالدين، الاستغفار...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "اسم المسجد:",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = mosqueName,
                        onValueChange = { mosqueName = it },
                        placeholder = { Text("مثال: مسجد النور، المسجد الكبير...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "اسم الشيخ / الخطيب:",
                        fontFamily = TajawalFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = speakerName,
                        onValueChange = { speakerName = it },
                        placeholder = { Text("مثال: فضيلة الشيخ محمد...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎙️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "نسق التسجيل: ${selectedQuality.titleArabic}",
                                    fontFamily = TajawalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "الحجم التقديري: ${selectedQuality.estimatedSizePerMinute}",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "اختر مكان حفظ الملف الصوتي:",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = QuranGold
                    )

                    // Radio 1: الذاكرة الداخلية
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { saveToExternal = false }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = !saveToExternal,
                            onClick = { saveToExternal = false },
                            colors = RadioButtonDefaults.colors(selectedColor = IslamicGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "الذاكرة الداخلية للهاتف (Internal Storage)",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "حفظ آمن وسريع داخل مجلد التطبيق المخصص",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Radio 2: الذاكرة الخارجية
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { saveToExternal = true }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = saveToExternal,
                            onClick = { saveToExternal = true },
                            colors = RadioButtonDefaults.colors(selectedColor = IslamicGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "الذاكرة الخارجية / بطاقة SD (External Storage)",
                                fontFamily = TajawalFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "توفير مساحة الهاتف وسهولة الوصول للملف ونقله",
                                fontFamily = TajawalFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val saved = manager.stopAndSaveRecording(
                            title = sermonTitle,
                            mosqueName = mosqueName,
                            speakerName = speakerName,
                            useExternalStorage = saveToExternal
                        )
                        showSaveDialog = false
                        if (saved != null) {
                            Toast.makeText(context, "تم حفظ خطبة الجمعة بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تأكيد الحفظ", fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveDialog = false }
                ) {
                    Text("إلغاء", fontFamily = TajawalFontFamily)
                }
            }
        )
    }

    // Audio Quality Selection Sheet
    if (showQualitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showQualitySheet = false },
            sheetState = qualitySheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "نسق وجودة تسجيل خطبة الجمعة",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "اختر النسق الأنسب لهاتفك بين أعلى جودة صوتية وأقل حجم تخزين",
                    fontFamily = TajawalFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                SermonAudioQuality.values().forEach { quality ->
                    val isSelected = quality == selectedQuality
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) IslamicGreen.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, IslamicGreen) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                manager.setAudioQuality(quality)
                                showQualitySheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    manager.setAudioQuality(quality)
                                    showQualitySheet = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = IslamicGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = quality.titleArabic,
                                        fontFamily = TajawalFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) IslamicGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (quality.isRecommended) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = IslamicGreen
                                        ) {
                                            Text(
                                                text = "موصى به",
                                                fontFamily = TajawalFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = quality.descriptionArabic,
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = QuranGold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "معدل الحجم: ${quality.estimatedSizePerMinute}",
                                        fontFamily = TajawalFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = QuranGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showQualitySheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                ) {
                    Text(
                        text = "تم الاختيار",
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    if (showProtectionDialog) {
        ContentProtectionWarningDialog(onDismissRequest = { showProtectionDialog = false })
    }
}
