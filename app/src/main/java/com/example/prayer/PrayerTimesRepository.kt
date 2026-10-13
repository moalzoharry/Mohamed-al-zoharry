package com.example.prayer

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class PrayerTimesRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
    val adhanPlayer = AdhanPlayer(context)

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _selectedCityFlow = MutableStateFlow(loadSelectedCity())
    val selectedCityFlow: StateFlow<CityPreset> = _selectedCityFlow.asStateFlow()

    private val _selectedMethodFlow = MutableStateFlow(loadMethod())
    val selectedMethodFlow: StateFlow<CalculationMethod> = _selectedMethodFlow.asStateFlow()

    private val _selectedMuezzinFlow = MutableStateFlow(loadMuezzin())
    val selectedMuezzinFlow: StateFlow<AdhanMuezzin> = _selectedMuezzinFlow.asStateFlow()

    private val _scheduleFlow = MutableStateFlow(buildScheduleNow())
    val scheduleFlow: StateFlow<PrayerSchedule> = _scheduleFlow.asStateFlow()

    init {
        // Live ticker updating countdown every 1 second
        scope.launch {
            while (isActive) {
                _scheduleFlow.value = buildScheduleNow()
                delay(1000L)
            }
        }

        // Schedule alarms on launch
        scope.launch {
            PrayerAlarmScheduler.rescheduleAll(context)
        }
    }

    private fun loadSelectedCity(): CityPreset {
        val name = prefs.getString("city_name", PresetCities.defaultCity.nameArabic) ?: PresetCities.defaultCity.nameArabic
        val country = prefs.getString("city_country", PresetCities.defaultCity.countryArabic) ?: PresetCities.defaultCity.countryArabic
        val lat = prefs.getFloat("city_lat", PresetCities.defaultCity.latitude.toFloat()).toDouble()
        val lng = prefs.getFloat("city_lng", PresetCities.defaultCity.longitude.toFloat()).toDouble()
        val methodName = prefs.getString("calc_method", PresetCities.defaultCity.defaultMethod.name) ?: PresetCities.defaultCity.defaultMethod.name
        val method = try { CalculationMethod.valueOf(methodName) } catch (_: Exception) { PresetCities.defaultCity.defaultMethod }

        return CityPreset(name, country, lat, lng, method)
    }

    private fun loadMethod(): CalculationMethod {
        val methodName = prefs.getString("calc_method", CalculationMethod.EGYPTIAN.name) ?: CalculationMethod.EGYPTIAN.name
        return try { CalculationMethod.valueOf(methodName) } catch (_: Exception) { CalculationMethod.EGYPTIAN }
    }

    private fun loadMuezzin(): AdhanMuezzin {
        val muezzinId = prefs.getString("selected_muezzin", AdhanMuezzin.MAKKAH.name) ?: AdhanMuezzin.MAKKAH.name
        return try { AdhanMuezzin.valueOf(muezzinId) } catch (_: Exception) { AdhanMuezzin.MAKKAH }
    }

    fun setCity(city: CityPreset) {
        _selectedCityFlow.value = city
        _selectedMethodFlow.value = city.defaultMethod
        prefs.edit()
            .putString("city_name", city.nameArabic)
            .putString("city_country", city.countryArabic)
            .putFloat("city_lat", city.latitude.toFloat())
            .putFloat("city_lng", city.longitude.toFloat())
            .putString("calc_method", city.defaultMethod.name)
            .apply()

        _scheduleFlow.value = buildScheduleNow()
        PrayerAlarmScheduler.rescheduleAll(context)
    }

    fun setCoordinates(cityName: String, countryName: String, latitude: Double, longitude: Double) {
        val customCity = CityPreset(cityName, countryName, latitude, longitude, _selectedMethodFlow.value)
        setCity(customCity)
    }

    fun setCalculationMethod(method: CalculationMethod) {
        _selectedMethodFlow.value = method
        prefs.edit().putString("calc_method", method.name).apply()
        _scheduleFlow.value = buildScheduleNow()
        PrayerAlarmScheduler.rescheduleAll(context)
    }

    fun setSelectedMuezzin(muezzin: AdhanMuezzin) {
        _selectedMuezzinFlow.value = muezzin
        prefs.edit().putString("selected_muezzin", muezzin.name).apply()
    }

    fun getAlertMode(prayer: PrayerType): AdhanAlertMode {
        val modeStr = prefs.getString("alert_mode_${prayer.id}", AdhanAlertMode.FULL_ADHAN.name)
        return try { AdhanAlertMode.valueOf(modeStr!!) } catch (_: Exception) { AdhanAlertMode.FULL_ADHAN }
    }

    fun cycleAlertMode(prayer: PrayerType): AdhanAlertMode {
        val current = getAlertMode(prayer)
        val allModes = AdhanAlertMode.values()
        val next = allModes[(allModes.indexOf(current) + 1) % allModes.size]
        prefs.edit().putString("alert_mode_${prayer.id}", next.name).apply()
        _scheduleFlow.value = buildScheduleNow()
        PrayerAlarmScheduler.rescheduleAll(context)
        return next
    }

    fun buildScheduleNow(): PrayerSchedule {
        val city = _selectedCityFlow.value
        val method = _selectedMethodFlow.value
        val nowCal = Calendar.getInstance()
        val nowMillis = nowCal.timeInMillis

        // Raw prayer times for today
        val todayRaw = PrayerTimesCalculator.calculate(city.latitude, city.longitude, nowCal, method)

        // Raw prayer times for tomorrow (used when current time is past today's Isha)
        val tomorrowCal = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowRaw = PrayerTimesCalculator.calculate(city.latitude, city.longitude, tomorrowCal, method)

        // Raw prayer times for yesterday (used for interval calculation when before Fajr)
        val yesterdayCal = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayRaw = PrayerTimesCalculator.calculate(city.latitude, city.longitude, yesterdayCal, method)

        val orderedTypes = listOf(
            PrayerType.FAJR,
            PrayerType.SUNRISE,
            PrayerType.DHUHR,
            PrayerType.ASR,
            PrayerType.SUNSET,
            PrayerType.MAGHRIB,
            PrayerType.ISHA
        )

        // Build items for today
        val todayItems = orderedTypes.map { type ->
            val raw = todayRaw[type] ?: PrayerRawTime(0, 0, "--:--", "--:--", 0L)
            PrayerTimeItem(
                type = type,
                timeFormatted12h = raw.formatted12h,
                time24h = raw.formatted24h,
                timestampMillis = raw.timestampMillis,
                alertMode = getAlertMode(type)
            )
        }

        // Determine next prayer and previous prayer for countdown & progress
        // Only prayers with adhan or sunrise count for countdown
        val countdownPrayerTypes = listOf(
            PrayerType.FAJR,
            PrayerType.SUNRISE,
            PrayerType.DHUHR,
            PrayerType.ASR,
            PrayerType.MAGHRIB,
            PrayerType.ISHA
        )

        val timeline = mutableListOf<Pair<PrayerType, Long>>()
        countdownPrayerTypes.forEach { t ->
            yesterdayRaw[t]?.let { timeline.add(Pair(t, it.timestampMillis)) }
        }
        countdownPrayerTypes.forEach { t ->
            todayRaw[t]?.let { timeline.add(Pair(t, it.timestampMillis)) }
        }
        countdownPrayerTypes.forEach { t ->
            tomorrowRaw[t]?.let { timeline.add(Pair(t, it.timestampMillis)) }
        }
        timeline.sortBy { it.second }

        var prevPrayerTimestamp = nowMillis - 3600000L
        var nextPrayerItem: PrayerTimeItem? = null
        var nextPrayerTimestamp = nowMillis + 3600000L

        for (i in timeline.indices) {
            val (type, time) = timeline[i]
            if (time > nowMillis) {
                nextPrayerTimestamp = time
                if (i > 0) {
                    prevPrayerTimestamp = timeline[i - 1].second
                }
                val rawForNext = if (time == todayRaw[type]?.timestampMillis) todayRaw[type] else tomorrowRaw[type]
                nextPrayerItem = PrayerTimeItem(
                    type = type,
                    timeFormatted12h = rawForNext?.formatted12h ?: "",
                    time24h = rawForNext?.formatted24h ?: "",
                    timestampMillis = time,
                    alertMode = getAlertMode(type),
                    isNext = true
                )
                break
            }
        }

        // Mark next and current item in todayItems list
        val markedTodayItems = todayItems.map { item ->
            val isNext = item.type == nextPrayerItem?.type && item.timestampMillis == nextPrayerItem?.timestampMillis
            val isCurrent = if (nextPrayerItem != null) {
                val indexInOrdered = countdownPrayerTypes.indexOf(nextPrayerItem.type)
                val prevType = if (indexInOrdered > 0) countdownPrayerTypes[indexInOrdered - 1] else countdownPrayerTypes.last()
                item.type == prevType
            } else false

            item.copy(isNext = isNext, isCurrent = isCurrent)
        }

        val remainingMillis = (nextPrayerTimestamp - nowMillis).coerceAtLeast(0L)
        val remainingSeconds = remainingMillis / 1000L

        val hours = remainingSeconds / 3600L
        val minutes = (remainingSeconds % 3600L) / 60L
        val seconds = remainingSeconds % 60L

        val countdownFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

        val totalInterval = (nextPrayerTimestamp - prevPrayerTimestamp).coerceAtLeast(1L)
        val elapsed = (nowMillis - prevPrayerTimestamp).coerceAtLeast(0L)
        val progressFraction = (elapsed.toFloat() / totalInterval.toFloat()).coerceIn(0f, 1f)

        return PrayerSchedule(
            dateGregorianFormatted = PrayerTimesCalculator.formatGregorianDateArabic(nowCal),
            dateHijriFormatted = PrayerTimesCalculator.formatHijriDateArabic(nowCal),
            cityName = "${city.nameArabic}، ${city.countryArabic}",
            items = markedTodayItems,
            nextPrayer = nextPrayerItem,
            remainingSeconds = remainingSeconds,
            countdownFormatted = countdownFormatted,
            progressFraction = progressFraction
        )
    }
}
