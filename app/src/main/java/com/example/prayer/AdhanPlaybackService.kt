package com.example.prayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.io.File

/**
 * Foreground Service dedicated to playing the Adhan fully and without interruption.
 * - Holds a PARTIAL_WAKE_LOCK so the CPU does not sleep during playback
 * - Acquires exclusive audio focus with USAGE_ALARM so other apps do not duck or cut it off
 * - Shows an ongoing foreground notification with prayer details and a prominent "إيقاف الأذان" (Stop) action
 * - Releases resources cleanly upon completion or when user explicitly cancels
 */
class AdhanPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_ADHAN) {
            stopAdhanAndSelf()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val cityName = intent?.getStringExtra(EXTRA_CITY_NAME) ?: "مدينتك"
        val muezzinName = intent?.getStringExtra(EXTRA_MUEZZIN_NAME) ?: "أذان الحرم المكي الشريف"
        val audioUrl = intent?.getStringExtra(EXTRA_AUDIO_URL)
        val customAudioPath = intent?.getStringExtra(EXTRA_CUSTOM_AUDIO_PATH)

        // Acquire WakeLock so CPU doesn't sleep mid-adhan
        acquireWakeLock()

        // Start Foreground immediately with high priority notification
        val notification = buildForegroundNotification(prayerName, cityName, muezzinName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Request Audio Focus
        requestAlarmAudioFocus()

        // Begin playback
        startPlayback(audioUrl, customAudioPath)

        return START_NOT_STICKY
    }

    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AdhanApp::AdhanPlaybackWakeLock").apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 minutes max safety timeout
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun requestAlarmAudioFocus() {
        val am = audioManager ?: return
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener { focusChange ->
                    // Do not allow other casual media to cut off the holy adhan
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        // User explicitly started another high priority app (e.g. phone call)
                        stopAdhanAndSelf()
                    }
                }
                .build()
            audioFocusRequest = focusRequest
            am.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        stopAdhanAndSelf()
                    }
                },
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(null)
        }
    }

    private fun startPlayback(audioUrl: String?, customAudioPath: String?) {
        try {
            mediaPlayer?.release()
            mediaPlayer = null

            // 1. Check custom audio from device storage first
            if (!customAudioPath.isNullOrEmpty()) {
                val customFile = File(customAudioPath)
                if (customFile.exists() && customFile.length() > 0) {
                    playUri(Uri.fromFile(customFile))
                    return
                }
            }

            // 2. Play network audio URL
            if (!audioUrl.isNullOrEmpty()) {
                Thread {
                    try {
                        val directUrl = resolveRedirectUrl(audioUrl)
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
                                    setDataSource(applicationContext, Uri.parse(directUrl), headers)
                                    setOnPreparedListener { player ->
                                        player.start()
                                    }
                                    setOnCompletionListener {
                                        stopAdhanAndSelf()
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        playLocalFallback()
                                        true
                                    }
                                }
                                mediaPlayer = mp
                                mp.prepareAsync()
                            } catch (_: Exception) {
                                playLocalFallback()
                            }
                        }
                    } catch (_: Exception) {
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            playLocalFallback()
                        }
                    }
                }.start()
                return
            }

            // 3. Fallback to local raw takbeer audio
            playLocalFallback()
        } catch (_: Exception) {
            playLocalFallback()
        }
    }

    private fun playUri(uri: Uri) {
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(applicationContext, uri)
                setOnPreparedListener { player ->
                    player.start()
                }
                setOnCompletionListener {
                    stopAdhanAndSelf()
                }
                setOnErrorListener { _, _, _ ->
                    playLocalFallback()
                    true
                }
            }
            mediaPlayer = mp
            mp.prepareAsync()
        } catch (_: Exception) {
            playLocalFallback()
        }
    }

    private fun playLocalFallback() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null

            val mp = MediaPlayer.create(applicationContext, R.raw.adhan_takbeer)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setOnCompletionListener {
                    stopAdhanAndSelf()
                }
            }
            mediaPlayer = mp
            mp?.start()
        } catch (_: Exception) {
            stopAdhanAndSelf()
        }
    }

    private fun stopAdhanAndSelf() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        abandonAudioFocus()
        releaseWakeLock()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تشغيل الأذان المبارك",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "خدمة تشغيل الأذان الصوتي ومواقيت الصلاة"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(
        prayerName: String,
        cityName: String,
        muezzinName: String
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            1001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AdhanPlaybackService::class.java).apply {
            action = ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("حان الآن موعد أذان $prayerName")
            .setContentText("أقم صلاتك • $muezzinName ($cityName)")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "حان الآن موعد أذان $prayerName حسب توقيت $cityName\n" +
                    "بصوت: $muezzinName\n" +
                    "«اللهم رب هذه الدعوة التامة والصلاة القائمة، آت سيدنا محمداً الوسيلة والفضيلة»"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "✕ إيقاف الأذان", stopPendingIntent)
            .build()
    }

    override fun onDestroy() {
        stopAdhanAndSelf()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "prayer_adhan_playback_v2"
        const val NOTIFICATION_ID = 9001

        const val ACTION_START_ADHAN = "com.example.prayer.ACTION_START_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.prayer.ACTION_STOP_ADHAN"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_MUEZZIN_NAME = "extra_muezzin_name"
        const val EXTRA_AUDIO_URL = "extra_audio_url"
        const val EXTRA_CUSTOM_AUDIO_PATH = "extra_custom_audio_path"

        fun start(
            context: Context,
            prayerName: String,
            cityName: String,
            muezzinName: String,
            audioUrl: String?,
            customAudioPath: String? = null
        ) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_START_ADHAN
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_CITY_NAME, cityName)
                putExtra(EXTRA_MUEZZIN_NAME, muezzinName)
                putExtra(EXTRA_AUDIO_URL, audioUrl)
                putExtra(EXTRA_CUSTOM_AUDIO_PATH, customAudioPath)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_STOP_ADHAN
            }
            context.startService(intent)
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
}
