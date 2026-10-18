package com.example.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.prayer.CustomAdhanItem
import com.example.prayer.CustomAdhanRepository
import com.example.prayer.PrayerType
import com.example.ui.components.ContentProtectionBadge
import com.example.ui.components.ContentProtectionWarningDialog
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.QuranGold
import com.example.ui.theme.TajawalFontFamily
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomAdhansScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    val repository = remember { CustomAdhanRepository(context) }
    val adhans by repository.adhansFlow.collectAsState()

    var showNameDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var adhanTitleInput by remember { mutableStateOf("") }

    var playingAdhanId by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var showProtectionDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingUri = uri
            adhanTitleInput = "أذان بصوت الشيخ "
            showNameDialog = true
        }
    }

    fun playPreview(item: CustomAdhanItem) {
        if (playingAdhanId == item.id && isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
        } else if (playingAdhanId == item.id && !isPlaying && mediaPlayer != null) {
            mediaPlayer?.start()
            isPlaying = true
        } else {
            mediaPlayer?.release()
            mediaPlayer = null
            try {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(item.filePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        isPlaying = false
                        playingAdhanId = null
                    }
                }
                mediaPlayer = mp
                playingAdhanId = item.id
                isPlaying = true
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "تعذر تشغيل الملف الصوتي", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ملفات الأذان المخصصة",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "رفع وتخصيص أذان مستقل لكل صلاة",
                            fontFamily = TajawalFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("custom_adhans_back_button")
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { audioPicker.launch("audio/*") },
                containerColor = IslamicGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_custom_adhan_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة أذان جديد"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = IslamicGreen.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGreen.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(IslamicGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "حرية كاملة في تخصيص نداء الحق",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = IslamicGreen
                        )
                        Text(
                            text = "يمكنك رفع أكثر من أذان بصوت مؤذنك المفضل، وتحديد كل أذان لصلاة معينة كالفجر أو المغرب، أو جعله عاماً لجميع الصلوات.",
                            fontFamily = TajawalFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (adhans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📢", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد ملفات أذان مخصصة حالياً",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "اضغط على زر (+) بالأسفل لاختيار ملف صوتي (MP3 / M4A) من هاتفك",
                            fontFamily = TajawalFontFamily,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { audioPicker.launch("audio/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("رفع أول أذان الآن", fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "الأذانات المحفوظة (${adhans.size}):",
                            fontFamily = TajawalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = QuranGold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(adhans, key = { it.id }) { item ->
                        val isThisPlaying = (playingAdhanId == item.id && isPlaying)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_adhan_card_${item.id}"),
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
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(if (isThisPlaying) QuranGold else IslamicGreen)
                                                .clickable { playPreview(item) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isThisPlaying) "إيقاف" else "استماع",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
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
                                                text = if (item.assignedPrayers.isEmpty()) "مخصص: لجميع الصلوات"
                                                else "مخصص لـ: " + item.assignedPrayers.joinToString("، ") { it.titleArabic },
                                                fontFamily = TajawalFontFamily,
                                                fontSize = 11.sp,
                                                color = if (item.assignedPrayers.isEmpty()) IslamicGreen else QuranGold
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            if (playingAdhanId == item.id) {
                                                mediaPlayer?.stop()
                                                isPlaying = false
                                                playingAdhanId = null
                                            }
                                            repository.deleteAdhan(item.id)
                                            Toast.makeText(context, "تم حذف الأذان", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف الأذان",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "تخصيص لصلوات محددة (اضغط للتعيين):",
                                    fontFamily = TajawalFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val prayers = listOf(
                                        PrayerType.FAJR,
                                        PrayerType.DHUHR,
                                        PrayerType.ASR,
                                        PrayerType.MAGHRIB,
                                        PrayerType.ISHA
                                    )

                                    prayers.forEach { prayer ->
                                        val isAssigned = item.assignedPrayers.contains(prayer)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isAssigned) IslamicGreen else MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isAssigned) IslamicGreen else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier.clickable {
                                                repository.togglePrayerAssignment(item.id, prayer)
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isAssigned) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = prayer.titleArabic,
                                                    fontFamily = TajawalFontFamily,
                                                    fontWeight = if (isAssigned) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.sp,
                                                    color = if (isAssigned) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Name dialog for uploaded adhan
    if (showNameDialog && pendingUri != null) {
        AlertDialog(
            onDismissRequest = {
                showNameDialog = false
                pendingUri = null
            },
            title = {
                Text(
                    text = "تسمية الأذان المرفوع",
                    fontFamily = TajawalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "أدخل اسماً أو وصفاً للأذان لتمييزه (مثال: أذان الحرم المكي، أذان الفجر...):",
                        fontFamily = TajawalFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = adhanTitleInput,
                        onValueChange = { adhanTitleInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingUri
                        if (uri != null) {
                            repository.addCustomAdhanFromUri(uri, adhanTitleInput)
                            Toast.makeText(context, "تم حفظ الأذان بنجاح", Toast.LENGTH_SHORT).show()
                        }
                        showNameDialog = false
                        pendingUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حفظ الأذان", fontFamily = TajawalFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNameDialog = false
                        pendingUri = null
                    }
                ) {
                    Text("إلغاء", fontFamily = TajawalFontFamily)
                }
            }
        )
    }

    if (showProtectionDialog) {
        ContentProtectionWarningDialog(onDismissRequest = { showProtectionDialog = false })
    }
}
