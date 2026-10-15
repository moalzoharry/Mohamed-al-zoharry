package com.example.prayer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class AdhanPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val activeMuezzin: AdhanMuezzin? = null,
    val isCustomActive: Boolean = false,
    val customDisplayName: String? = null,
    val error: String? = null
)

class AdhanPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _state = MutableStateFlow(AdhanPlayerState())
    val state: StateFlow<AdhanPlayerState> = _state.asStateFlow()

    private fun acquireWakeLock() {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AdhanPlayer::WakeLock").apply {
                setReferenceCounted(false)
                acquire(8 * 60 * 1000L)
            }
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun requestFocus() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener { focus ->
                    if (focus == AudioManager.AUDIOFOCUS_LOSS) {
                        stop()
                    }
                }
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { focus -> if (focus == AudioManager.AUDIOFOCUS_LOSS) stop() },
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    fun playMuezzin(muezzin: AdhanMuezzin) {
        if (_state.value.isPlaying && _state.value.activeMuezzin == muezzin && !_state.value.isCustomActive) {
            stop()
            return
        }

        stop()
        acquireWakeLock()
        requestFocus()

        _state.value = AdhanPlayerState(
            isPlaying = false,
            isBuffering = true,
            activeMuezzin = muezzin,
            isCustomActive = false,
            error = null
        )

        Thread {
            try {
                val directUrl = resolveRedirectUrl(muezzin.audioUrl)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        val mp = MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .build()
                            )
                            val headers = mapOf("User-Agent" to "Mozilla/5.0 (Android; Mobile)")
                            setDataSource(context, Uri.parse(directUrl), headers)
                            setOnPreparedListener { player ->
                                player.start()
                                _state.value = AdhanPlayerState(
                                    isPlaying = true,
                                    isBuffering = false,
                                    activeMuezzin = muezzin,
                                    isCustomActive = false
                                )
                            }
                            setOnCompletionListener {
                                stop()
                            }
                            setOnErrorListener { _, _, _ ->
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
            } catch (_: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    playLocalFallback(muezzin)
                }
            }
        }.start()
    }

    fun playCustomAudio(filePathOrUri: String, displayName: String) {
        if (_state.value.isPlaying && _state.value.isCustomActive) {
            stop()
            return
        }

        stop()
        acquireWakeLock()
        requestFocus()

        _state.value = AdhanPlayerState(
            isPlaying = false,
            isBuffering = true,
            activeMuezzin = null,
            isCustomActive = true,
            customDisplayName = displayName,
            error = null
        )

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                val file = File(filePathOrUri)
                if (file.exists()) {
                    setDataSource(context, Uri.fromFile(file))
                } else {
                    setDataSource(context, Uri.parse(filePathOrUri))
                }
                setOnPreparedListener { player ->
                    player.start()
                    _state.value = AdhanPlayerState(
                        isPlaying = true,
                        isBuffering = false,
                        activeMuezzin = null,
                        isCustomActive = true,
                        customDisplayName = displayName
                    )
                }
                setOnCompletionListener {
                    stop()
                }
                setOnErrorListener { _, _, _ ->
                    _state.value = AdhanPlayerState(
                        isPlaying = false,
                        isBuffering = false,
                        error = "تعذر تشغيل الملف الصوتي المخصص"
                    )
                    releaseWakeLock()
                    abandonFocus()
                    true
                }
            }
            mediaPlayer = mp
            mp.prepareAsync()
        } catch (e: Exception) {
            _state.value = AdhanPlayerState(
                isPlaying = false,
                isBuffering = false,
                error = "خطأ في قراءة ملف الأذان من الهاتف"
            )
            releaseWakeLock()
            abandonFocus()
        }
    }

    private fun playLocalFallback(muezzin: AdhanMuezzin) {
        try {
            mediaPlayer?.release()
            mediaPlayer = null

            val mp = MediaPlayer.create(context, R.raw.adhan_takbeer)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ALARM)
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
                error = "تم تشغيل التكبيرات المحلية لضمان عدم الانقطاع"
            )
        } catch (e: Exception) {
            _state.value = AdhanPlayerState(
                isPlaying = false,
                isBuffering = false,
                error = "تعذر تشغيل صوت الأذان"
            )
            releaseWakeLock()
            abandonFocus()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        releaseWakeLock()
        abandonFocus()

        _state.value = AdhanPlayerState(
            isPlaying = false,
            isBuffering = false,
            activeMuezzin = null,
            isCustomActive = false,
            customDisplayName = null,
            error = null
        )
    }

    fun release() {
        stop()
    }

    private fun resolveRedirectUrl(rawUrl: String): String {
        var currentUrl = rawUrl
        var redirectCount = 0
        while (redirectCount < 5) {
            try {
                val connection = java.net.URL(currentUrl).openConnection() as java.net.HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "HEAD"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                val responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    val location = connection.getHeaderField("Location") ?: break
                    currentUrl = location
                    redirectCount++
                } else {
                    break
                }
            } catch (_: Exception) {
                break
            }
        }
        return currentUrl
    }
}
