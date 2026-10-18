package com.example.sermon

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class FridaySermonItem(
    val id: String,
    val title: String,
    val mosqueName: String,
    val speakerName: String,
    val dateFormatted: String,
    val filePath: String,
    val durationSeconds: Int = 0,
    val sizeBytes: Long = 0L,
    val isExternalStorage: Boolean = false,
    val qualityTitle: String = "AAC عالي الكفاءة"
)

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED
}

enum class SermonAudioQuality(
    val id: String,
    val titleArabic: String,
    val descriptionArabic: String,
    val formatExtension: String,
    val outputFormat: Int,
    val audioEncoder: Int,
    val bitRate: Int,
    val sampleRate: Int,
    val estimatedSizePerMinute: String,
    val isRecommended: Boolean = false
) {
    HIGH_EFFICIENCY_AAC(
        id = "aac_he",
        titleArabic = "AAC عالي الكفاءة (موصى به - أعلى نقاء وأقل حجم)",
        descriptionArabic = "نقاء صوتي فائق لصوت الخطيب ومكبرات الصوت مع استهلاك مساحة منخفض جداً (~20 ميجابايت لساعة كاملة)",
        formatExtension = "m4a",
        outputFormat = MediaRecorder.OutputFormat.MPEG_4,
        audioEncoder = MediaRecorder.AudioEncoder.AAC,
        bitRate = 64_000,
        sampleRate = 44_100,
        estimatedSizePerMinute = "0.4 ميجابايت / دقيقة",
        isRecommended = true
    ),
    STUDIO_AAC(
        id = "aac_studio",
        titleArabic = "جودة استوديو نقية (High AAC - 128 kbps)",
        descriptionArabic = "أعلى دقة تسجيل وتفاصيل صوتية بجودة الاستوديو (حوالي 45 ميجابايت للساعة)",
        formatExtension = "m4a",
        outputFormat = MediaRecorder.OutputFormat.MPEG_4,
        audioEncoder = MediaRecorder.AudioEncoder.AAC,
        bitRate = 128_000,
        sampleRate = 44_100,
        estimatedSizePerMinute = "0.9 ميجابايت / دقيقة",
        isRecommended = false
    ),
    COMPACT_VOICE(
        id = "voice_compact",
        titleArabic = "توفير فائق للمساحة (Voice Saver - 32 kbps)",
        descriptionArabic = "حجم اقتصادي جداً ومناسب للهواتف ذات الذاكرة المحدودة وتسجيل الخطب الطويلة (~10 ميجابايت للساعة)",
        formatExtension = "m4a",
        outputFormat = MediaRecorder.OutputFormat.MPEG_4,
        audioEncoder = MediaRecorder.AudioEncoder.AAC,
        bitRate = 32_000,
        sampleRate = 22_050,
        estimatedSizePerMinute = "0.2 ميجابايت / دقيقة",
        isRecommended = false
    )
}

class FridaySermonManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("friday_sermons_prefs", Context.MODE_PRIVATE)

    private val internalDir: File = File(context.filesDir, "FridaySermons").apply {
        if (!exists()) mkdirs()
    }

    private val externalDir: File? = try {
        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.let {
            File(it, "FridaySermons").apply { if (!exists()) mkdirs() }
        }
    } catch (e: Exception) {
        null
    }

    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTimeMs: Long = 0L
    private var isTargetExternal: Boolean = false
    private var activeQuality: SermonAudioQuality = loadAudioQuality()

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _selectedQualityFlow = MutableStateFlow(loadAudioQuality())
    val selectedQualityFlow: StateFlow<SermonAudioQuality> = _selectedQualityFlow.asStateFlow()

    private val _sermonsList = MutableStateFlow<List<FridaySermonItem>>(loadSermons())
    val sermonsList: StateFlow<List<FridaySermonItem>> = _sermonsList.asStateFlow()

    private fun loadAudioQuality(): SermonAudioQuality {
        val qId = prefs.getString("sermon_audio_quality", SermonAudioQuality.HIGH_EFFICIENCY_AAC.id)
        return SermonAudioQuality.values().firstOrNull { it.id == qId } ?: SermonAudioQuality.HIGH_EFFICIENCY_AAC
    }

    fun setAudioQuality(quality: SermonAudioQuality) {
        _selectedQualityFlow.value = quality
        prefs.edit().putString("sermon_audio_quality", quality.id).apply()
    }

    private fun loadSermons(): List<FridaySermonItem> {
        val jsonStr = prefs.getString("sermons_json_list", null) ?: return emptyList()
        val list = mutableListOf<FridaySermonItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val title = obj.getString("title")
                val mosque = obj.optString("mosqueName", "")
                val speaker = obj.optString("speakerName", "")
                val date = obj.optString("dateFormatted", "")
                val path = obj.getString("filePath")
                val dur = obj.optInt("durationSeconds", 0)
                val size = obj.optLong("sizeBytes", 0L)
                val isExt = obj.optBoolean("isExternalStorage", false)
                val quality = obj.optString("qualityTitle", "AAC عالي الكفاءة")
                if (File(path).exists()) {
                    list.add(FridaySermonItem(id, title, mosque, speaker, date, path, dur, size, isExt, quality))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveSermons(list: List<FridaySermonItem>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("mosqueName", item.mosqueName)
                put("speakerName", item.speakerName)
                put("dateFormatted", item.dateFormatted)
                put("filePath", item.filePath)
                put("durationSeconds", item.durationSeconds)
                put("sizeBytes", item.sizeBytes)
                put("isExternalStorage", item.isExternalStorage)
                put("qualityTitle", item.qualityTitle)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("sermons_json_list", jsonArray.toString()).apply()
        _sermonsList.value = list
    }

    fun startRecording(
        useExternalStorage: Boolean = false,
        quality: SermonAudioQuality = _selectedQualityFlow.value
    ): Boolean {
        if (_recordingState.value != RecordingState.IDLE) return false

        activeQuality = quality
        isTargetExternal = useExternalStorage && (externalDir != null)
        val targetDir = if (isTargetExternal) externalDir!! else internalDir
        val id = UUID.randomUUID().toString()
        val file = File(targetDir, "sermon_$id.${quality.formatExtension}")
        currentRecordingFile = file

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        return try {
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(quality.outputFormat)
                setAudioEncoder(quality.audioEncoder)
                setAudioEncodingBitRate(quality.bitRate)
                setAudioSamplingRate(quality.sampleRate)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            recordingStartTimeMs = System.currentTimeMillis()
            _recordingState.value = RecordingState.RECORDING
            true
        } catch (e: Exception) {
            e.printStackTrace()
            currentRecordingFile?.delete()
            currentRecordingFile = null
            _recordingState.value = RecordingState.IDLE
            false
        }
    }

    fun pauseRecording() {
        if (_recordingState.value == RecordingState.RECORDING && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
                _recordingState.value = RecordingState.PAUSED
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resumeRecording() {
        if (_recordingState.value == RecordingState.PAUSED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
                _recordingState.value = RecordingState.RECORDING
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopAndSaveRecording(
        title: String,
        mosqueName: String,
        speakerName: String,
        useExternalStorage: Boolean = isTargetExternal
    ): FridaySermonItem? {
        val file = currentRecordingFile ?: return null
        val durationSec = ((System.currentTimeMillis() - recordingStartTimeMs) / 1000).toInt().coerceAtLeast(1)

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        _recordingState.value = RecordingState.IDLE

        // Move to external if requested and currently in internal or vice versa
        var finalFile = file
        if (useExternalStorage && externalDir != null && file.parentFile != externalDir) {
            val dest = File(externalDir, file.name)
            if (file.renameTo(dest)) {
                finalFile = dest
            }
        } else if (!useExternalStorage && file.parentFile != internalDir) {
            val dest = File(internalDir, file.name)
            if (file.renameTo(dest)) {
                finalFile = dest
            }
        }

        val dateStr = SimpleDateFormat("yyyy/MM/dd - EEEE", Locale("ar")).format(Date())
        val defaultTitle = "خطبة الجمعة المباركة - $dateStr"
        val item = FridaySermonItem(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { defaultTitle },
            mosqueName = mosqueName.ifBlank { "المسجد" },
            speakerName = speakerName.ifBlank { "الخطيب" },
            dateFormatted = dateStr,
            filePath = finalFile.absolutePath,
            durationSeconds = durationSec,
            sizeBytes = finalFile.length(),
            isExternalStorage = useExternalStorage,
            qualityTitle = activeQuality.titleArabic
        )

        val updated = listOf(item) + _sermonsList.value
        saveSermons(updated)
        currentRecordingFile = null
        return item
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        currentRecordingFile?.delete()
        currentRecordingFile = null
        _recordingState.value = RecordingState.IDLE
    }

    fun deleteSermon(id: String) {
        val item = _sermonsList.value.firstOrNull { it.id == id }
        if (item != null) {
            try {
                File(item.filePath).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val updated = _sermonsList.value.filter { it.id != id }
            saveSermons(updated)
        }
    }
}
