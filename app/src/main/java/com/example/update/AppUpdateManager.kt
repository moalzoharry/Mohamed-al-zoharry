package com.example.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppUpdateInfo(
    val hasUpdate: Boolean = false,
    val latestVersionName: String = "2.1.0",
    val latestVersionCode: Int = 21,
    val releaseNotesArabic: String = "• إضافة شاشة الافتتاحية الملكية وشعار الفرقان بلس.\n• ضبط دقيق لمواقيت الصلاة عبر GPS وحل فارق التوقيت.\n• خيارات جودة وتسجيل خطبة الجمعة (أعلى جودة وأقل حجم).\n• التقويم الهجري ومناسبات الأيام البيض وصيام الإثنين والخميس.",
    val releaseNotesEnglish: String = "• Royal animated splash screen with glowing Quranic verse.\n• Precise GPS prayer times calculation & timezone fix.\n• Friday Sermon high-efficiency recording profiles.\n• Full Hijri Calendar with Sunnah fasting & Islamic events.",
    val downloadUrl: String = "market://details?id=com.example",
    val isMandatory: Boolean = false
)

object AppUpdateManager {

    private val _updateState = MutableStateFlow<AppUpdateInfo?>(null)
    val updateState: StateFlow<AppUpdateInfo?> = _updateState.asStateFlow()

    fun getCurrentVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "2.0.0"
        } catch (_: Exception) {
            "2.0.0"
        }
    }

    fun getCurrentVersionCode(context: Context): Int {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (_: Exception) {
            1
        }
    }

    /**
     * Checks for simulated or remote update availability.
     */
    fun checkForUpdates(context: Context, forceShowSimulation: Boolean = false) {
        val currentCode = getCurrentVersionCode(context)
        // If simulated or newer version available
        val simulatedLatestCode = 21 // Next version
        if (forceShowSimulation || simulatedLatestCode > currentCode) {
            _updateState.value = AppUpdateInfo(
                hasUpdate = true,
                latestVersionName = "2.1.0",
                latestVersionCode = simulatedLatestCode,
                downloadUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
            )
        } else {
            _updateState.value = null
        }
    }

    fun dismissUpdate() {
        _updateState.value = null
    }

    fun openUpdateStore(context: Context, downloadUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        }
    }
}
