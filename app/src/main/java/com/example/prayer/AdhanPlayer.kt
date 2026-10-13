package com.example.prayer

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdhanPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val activeMuezzin: AdhanMuezzin? = null,
    val error: String? = null
)

class AdhanPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val _state = MutableStateFlow(AdhanPlayerState())
    val state: StateFlow<AdhanPlayerState> = _state.asStateFlow()

    fun playMuezzin(muezzin: AdhanMuezzin) {
        if (_state.value.isPlaying && _state.value.activeMuezzin == muezzin) {
            stop()
            return
        }

        stop()
        _state.value = AdhanPlayerState(
            isPlaying = false,
            isBuffering = true,
            activeMuezzin = muezzin,
            error = null
        )

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(muezzin.audioUrl))
                setOnPreparedListener { player ->
                    player.start()
                    _state.value = AdhanPlayerState(
                        isPlaying = true,
                        isBuffering = false,
                        activeMuezzin = muezzin
                    )
                }
                setOnCompletionListener {
                    stop()
                }
                setOnErrorListener { _, _, _ ->
                    // Fallback to local raw takbeer audio when network fails
                    playLocalFallback(muezzin)
                    true
                }
            }
            mediaPlayer = mp
            mp.prepareAsync()
        } catch (_: Exception) {
            playLocalFallback(muezzin)
        }
    }

    private fun playLocalFallback(muezzin: AdhanMuezzin) {
        try {
            mediaPlayer?.release()
            mediaPlayer = null

            val mp = MediaPlayer.create(context, R.raw.adhan_takbeer).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnCompletionListener {
                    stop()
                }
            }
            mediaPlayer = mp
            mp?.start()
            _state.value = AdhanPlayerState(
                isPlaying = true,
                isBuffering = false,
                activeMuezzin = muezzin,
                error = "تم تشغيل التكبيرات المحلية بدون إنترنت"
            )
        } catch (e: Exception) {
            _state.value = AdhanPlayerState(
                isPlaying = false,
                isBuffering = false,
                error = "تعذر تشغيل صوت الأذان"
            )
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _state.value = AdhanPlayerState(
            isPlaying = false,
            isBuffering = false,
            activeMuezzin = null,
            error = null
        )
    }

    fun release() {
        stop()
    }
}
