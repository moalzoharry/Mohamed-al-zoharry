package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.data.model.QuranReciter
import com.example.data.model.RepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentSurah: Int = 1,
    val currentAyah: Int = 1,
    val surahName: String = "",
    val reciter: QuranReciter = QuranReciter.ALAFASY,
    val repeatMode: RepeatMode = RepeatMode.ONCE,
    val currentRepeatCount: Int = 1,
    val error: String? = null
)

class QuranAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    var onAyahChanged: ((Int, Int) -> Unit)? = null
    private var totalAyahsInCurrentSurah: Int = 7

    fun setReciter(reciter: QuranReciter) {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(reciter = reciter)
        if (currentState.isPlaying || currentState.isBuffering) {
            playAyah(
                surahNumber = currentState.currentSurah,
                ayahNumber = currentState.currentAyah,
                surahName = currentState.surahName,
                totalAyahs = totalAyahsInCurrentSurah,
                reciter = reciter,
                repeatMode = currentState.repeatMode
            )
        }
    }

    fun setRepeatMode(repeatMode: RepeatMode) {
        _playerState.value = _playerState.value.copy(
            repeatMode = repeatMode,
            currentRepeatCount = 1
        )
    }

    fun cycleRepeatMode(): RepeatMode {
        val current = _playerState.value.repeatMode
        val allModes = RepeatMode.values()
        val nextIndex = (allModes.indexOf(current) + 1) % allModes.size
        val nextMode = allModes[nextIndex]
        setRepeatMode(nextMode)
        return nextMode
    }

    fun playAyah(
        surahNumber: Int,
        ayahNumber: Int,
        surahName: String,
        totalAyahs: Int,
        reciter: QuranReciter = _playerState.value.reciter,
        repeatMode: RepeatMode = _playerState.value.repeatMode,
        repeatCount: Int = 1
    ) {
        totalAyahsInCurrentSurah = totalAyahs
        _playerState.value = _playerState.value.copy(
            isBuffering = true,
            isPlaying = false,
            currentSurah = surahNumber,
            currentAyah = ayahNumber,
            surahName = surahName,
            reciter = reciter,
            repeatMode = repeatMode,
            currentRepeatCount = repeatCount,
            error = null
        )

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            val surahFormatted = String.format("%03d", surahNumber)
            val ayahFormatted = String.format("%03d", ayahNumber)
            val audioUrl = "${reciter.baseUrl}$surahFormatted$ayahFormatted.mp3"

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    mp.start()
                    _playerState.value = _playerState.value.copy(
                        isPlaying = true,
                        isBuffering = false
                    )
                    onAyahChanged?.invoke(surahNumber, ayahNumber)
                }
                setOnCompletionListener {
                    handleAyahCompletion(surahNumber, ayahNumber, surahName, totalAyahs)
                }
                setOnErrorListener { _, _, _ ->
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        isBuffering = false,
                        error = "تعذر تشغيل التلاوة الصوتية، يرجى التحقق من الاتصال بالإنترنت"
                    )
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isBuffering = false,
                error = e.localizedMessage ?: "خطأ في تشغيل الصوت"
            )
        }
    }

    private fun handleAyahCompletion(
        surahNumber: Int,
        ayahNumber: Int,
        surahName: String,
        totalAyahs: Int
    ) {
        val currentState = _playerState.value
        val repeatMode = currentState.repeatMode
        val currentRepeat = currentState.currentRepeatCount

        if (repeatMode == RepeatMode.INFINITE) {
            // Replay same ayah indefinitely
            playAyah(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName,
                totalAyahs = totalAyahs,
                reciter = currentState.reciter,
                repeatMode = repeatMode,
                repeatCount = currentRepeat + 1
            )
        } else if (currentRepeat < repeatMode.count) {
            // Replay same ayah until requested repeat count is reached
            playAyah(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName,
                totalAyahs = totalAyahs,
                reciter = currentState.reciter,
                repeatMode = repeatMode,
                repeatCount = currentRepeat + 1
            )
        } else {
            // Move to next ayah in surah
            if (ayahNumber < totalAyahs) {
                playAyah(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber + 1,
                    surahName = surahName,
                    totalAyahs = totalAyahs,
                    reciter = currentState.reciter,
                    repeatMode = repeatMode,
                    repeatCount = 1
                )
            } else {
                _playerState.value = currentState.copy(isPlaying = false, currentRepeatCount = 1)
            }
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playerState.value = _playerState.value.copy(isPlaying = false)
            } else {
                player.start()
                _playerState.value = _playerState.value.copy(isPlaying = true)
            }
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _playerState.value = _playerState.value.copy(
            isPlaying = false,
            isBuffering = false,
            currentRepeatCount = 1
        )
    }

    fun nextAyah() {
        val state = _playerState.value
        if (state.currentAyah < totalAyahsInCurrentSurah) {
            playAyah(
                surahNumber = state.currentSurah,
                ayahNumber = state.currentAyah + 1,
                surahName = state.surahName,
                totalAyahs = totalAyahsInCurrentSurah,
                reciter = state.reciter,
                repeatMode = state.repeatMode,
                repeatCount = 1
            )
        }
    }

    fun previousAyah() {
        val state = _playerState.value
        if (state.currentAyah > 1) {
            playAyah(
                surahNumber = state.currentSurah,
                ayahNumber = state.currentAyah - 1,
                surahName = state.surahName,
                totalAyahs = totalAyahsInCurrentSurah,
                reciter = state.reciter,
                repeatMode = state.repeatMode,
                repeatCount = 1
            )
        }
    }

    fun release() {
        stop()
    }
}
