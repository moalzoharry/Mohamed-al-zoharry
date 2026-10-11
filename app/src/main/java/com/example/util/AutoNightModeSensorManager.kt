package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sensor manager for ambient light detection and time-based night mode automation.
 * Helps reduce eye strain in low-light environments by activating night reading mode.
 */
class AutoNightModeSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _isLowLightDetected = MutableStateFlow(false)
    val isLowLightDetected: StateFlow<Boolean> = _isLowLightDetected.asStateFlow()

    private val _ambientLux = MutableStateFlow<Float?>(null)
    val ambientLux: StateFlow<Float?> = _ambientLux.asStateFlow()

    private var isListening = false

    fun startListening() {
        if (!isListening && lightSensor != null) {
            sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        }
    }

    fun stopListening() {
        if (isListening) {
            sensorManager?.unregisterListener(this)
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values.getOrNull(0) ?: return
            _ambientLux.value = lux
            // Under 15 lux corresponds to a dark or poorly lit room, indicating high risk of eye strain
            val isLowLight = lux < 15.0f
            if (_isLowLightDetected.value != isLowLight) {
                _isLowLightDetected.value = isLowLight
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        /**
         * Checks whether the current time is considered night/evening (19:00 - 06:00).
         */
        fun isNightTimeByClock(): Boolean {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return hour >= 19 || hour < 6
        }
    }
}
