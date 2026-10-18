package com.example.prayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

sealed class LocationDetectionResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val cityName: String,
        val countryName: String,
        val timeZoneId: String,
        val recommendedMethod: CalculationMethod
    ) : LocationDetectionResult()

    data class Error(val messageArabic: String) : LocationDetectionResult()
}

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(context: Context): LocationDetectionResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext LocationDetectionResult.Error("يرجى منح إذن الوصول للموقع الجغرافي لتحديد مواقيت الصلاة بدقة.")
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext LocationDetectionResult.Error("خدمة تحديد الموقع غير متوفرة على هذا الجهاز.")

        // Check if any provider is enabled
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            return@withContext LocationDetectionResult.Error("خدمة الموقع (GPS) مغلقة. يرجى تشغيلها من إعدادات الهاتف.")
        }

        // Try getting last known location first (fast)
        var bestLocation: Location? = null
        if (isGpsEnabled) {
            val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (loc != null) bestLocation = loc
        }
        if (isNetworkEnabled) {
            val loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null && (bestLocation == null || loc.time > bestLocation.time)) {
                bestLocation = loc
            }
        }

        // If no recent location (or older than 15 minutes), request a fresh location update with timeout
        val maxAgeMs = 15 * 60 * 1000L
        val isLocationFresh = bestLocation != null && (System.currentTimeMillis() - bestLocation.time) < maxAgeMs

        if (!isLocationFresh) {
            val freshLocation = withTimeoutOrNull(8000L) {
                requestSingleUpdate(locationManager, isGpsEnabled, isNetworkEnabled)
            }
            if (freshLocation != null) {
                bestLocation = freshLocation
            }
        }

        val location = bestLocation
            ?: return@withContext LocationDetectionResult.Error("تعذر العثور على إشارة GPS حالياً. يرجى التأكد من تشغيل الموقع والمحاولة في مكان مكشوف.")

        // Reverse Geocoding to determine city and country in Arabic
        var cityName = "موقعي الحالي"
        var countryName = "مصر"
        try {
            val geocoder = Geocoder(context, Locale("ar"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "موقعي الحالي"
                    countryName = addr.countryName ?: "مصر"
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "موقعي الحالي"
                    countryName = addr.countryName ?: "مصر"
                }
            }
        } catch (_: Exception) {
            // Geocoder failure fallback (e.g. offline)
            cityName = "موقعي الحالي (GPS)"
        }

        val tzId = determineTimeZoneId(countryName, location.latitude, location.longitude)
        val recommendedMethod = determineCalculationMethod(countryName, location.latitude, location.longitude)

        LocationDetectionResult.Success(
            latitude = location.latitude,
            longitude = location.longitude,
            cityName = cityName,
            countryName = countryName,
            timeZoneId = tzId,
            recommendedMethod = recommendedMethod
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleUpdate(
        locationManager: LocationManager,
        isGps: Boolean,
        isNetwork: Boolean
    ): Location? = suspendCancellableCoroutine { cont ->
        val provider = when {
            isNetwork -> LocationManager.NETWORK_PROVIDER
            isGps -> LocationManager.GPS_PROVIDER
            else -> LocationManager.PASSIVE_PROVIDER
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                try {
                    locationManager.removeUpdates(this)
                } catch (_: Exception) {}
                if (cont.isActive) cont.resume(loc)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
            override fun onProviderEnabled(p: String) {}
            override fun onProviderDisabled(p: String) {}
        }

        try {
            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            cont.invokeOnCancellation {
                try {
                    locationManager.removeUpdates(listener)
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }

    private fun determineCalculationMethod(country: String, lat: Double, lng: Double): CalculationMethod {
        val lowerCountry = country.lowercase()
        return when {
            lowerCountry.contains("مصر") || lowerCountry.contains("egypt") -> CalculationMethod.EGYPTIAN
            lowerCountry.contains("سعودية") || lowerCountry.contains("saudi") ||
                    lowerCountry.contains("مكة") || lowerCountry.contains("كويت") ||
                    lowerCountry.contains("قطر") || lowerCountry.contains("بحرين") -> CalculationMethod.UMM_AL_QURA
            lowerCountry.contains("إمارات") || lowerCountry.contains("emirates") || lowerCountry.contains("دبي") -> CalculationMethod.DUBAI
            lowerCountry.contains("باكستان") || lowerCountry.contains("pakistan") || lowerCountry.contains("هند") -> CalculationMethod.KARACHI
            else -> CalculationMethod.MWL
        }
    }

    private fun determineTimeZoneId(country: String, lat: Double, lng: Double): String {
        val lowerCountry = country.lowercase()
        if (lowerCountry.contains("مصر") || lowerCountry.contains("egypt") || (lat in 21.0..32.5 && lng in 24.5..37.5)) {
            return "Africa/Cairo"
        }
        if (lowerCountry.contains("سعودية") || lowerCountry.contains("saudi") || lowerCountry.contains("كويت") || lowerCountry.contains("قطر") || lowerCountry.contains("بحرين")) {
            return "Asia/Riyadh"
        }
        if (lowerCountry.contains("إمارات") || lowerCountry.contains("emirates") || lowerCountry.contains("دبي") || lowerCountry.contains("dubai") || lowerCountry.contains("عمان") || lowerCountry.contains("oman")) {
            return "Asia/Dubai"
        }
        if (lowerCountry.contains("أردن") || lowerCountry.contains("jordan")) return "Asia/Amman"
        if (lowerCountry.contains("مغرب") || lowerCountry.contains("morocco")) return "Africa/Casablanca"
        if (lowerCountry.contains("جزائر") || lowerCountry.contains("algeria")) return "Africa/Algiers"
        if (lowerCountry.contains("تونس") || lowerCountry.contains("tunisia")) return "Africa/Tunis"
        if (lowerCountry.contains("عراق") || lowerCountry.contains("iraq")) return "Asia/Baghdad"
        if (lowerCountry.contains("فلسطين") || lowerCountry.contains("قدس") || lowerCountry.contains("palestine")) return "Asia/Jerusalem"
        if (lowerCountry.contains("تركيا") || lowerCountry.contains("turkey")) return "Europe/Istanbul"

        // If device default timezone rawOffset is reasonably close (+/- 2.5 hours) to geographic longitude offset, use it
        val defaultTz = TimeZone.getDefault()
        val expectedOffsetHours = lng / 15.0
        val actualOffsetHours = defaultTz.rawOffset / 3600000.0
        if (kotlin.math.abs(expectedOffsetHours - actualOffsetHours) <= 2.5) {
            return defaultTz.id
        }

        // Fallback to GMT offset string
        val roughOffset = kotlin.math.round(lng / 15.0).toInt()
        return if (roughOffset >= 0) "GMT+$roughOffset" else "GMT$roughOffset"
    }
}

