package com.example.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class QiblaState(
    val deviceHeading: Float = 0f, // 0..360 degrees (North = 0)
    val qiblaBearing: Float = 136.14f, // Bearing from user location to Kaaba
    val qiblaRelativeAngle: Float = 0f, // Angle to rotate pointer to point to Kaaba
    val isFacingQibla: Boolean = false, // True if pointing within +/- 3 degrees
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val hasCompassSensors: Boolean = true
)

class QiblaSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _state = MutableStateFlow(
        QiblaState(
            hasCompassSensors = (accelerometer != null && magnetometer != null)
        )
    )
    val state: StateFlow<QiblaState> = _state.asStateFlow()

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rMatrix = FloatArray(9)
    private val iMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var hasGravity = false
    private var hasGeomagnetic = false
    private var smoothedHeading = 0f

    // Current coordinates (default to Cairo, Egypt)
    private var userLatitude: Double = 30.0444
    private var userLongitude: Double = 31.2357
    private var calculatedQiblaBearing: Float = 136.14f

    init {
        updateCoordinates(userLatitude, userLongitude)
    }

    fun updateCoordinates(lat: Double, lng: Double) {
        userLatitude = lat
        userLongitude = lng
        calculatedQiblaBearing = calculateBearingToKaaba(lat, lng)
        updateQiblaState(_state.value.deviceHeading, _state.value.accuracy)
    }

    fun startListening() {
        smoothedHeading = _state.value.deviceHeading
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        hasGravity = false
        hasGeomagnetic = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val alpha = 0.25f // Low-pass filter coefficient for smoothing raw readings

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity[0] = alpha * event.values[0] + (1 - alpha) * gravity[0]
                gravity[1] = alpha * event.values[1] + (1 - alpha) * gravity[1]
                gravity[2] = alpha * event.values[2] + (1 - alpha) * gravity[2]
                hasGravity = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic[0] = alpha * event.values[0] + (1 - alpha) * geomagnetic[0]
                geomagnetic[1] = alpha * event.values[1] + (1 - alpha) * geomagnetic[1]
                geomagnetic[2] = alpha * event.values[2] + (1 - alpha) * geomagnetic[2]
                hasGeomagnetic = true
            }
        }

        if (hasGravity && hasGeomagnetic) {
            val success = SensorManager.getRotationMatrix(rMatrix, iMatrix, gravity, geomagnetic)
            if (success) {
                SensorManager.getOrientation(rMatrix, orientation)
                var rawAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                rawAzimuth = (rawAzimuth + 360f) % 360f

                // Smooth angle interpolation around 0/360 boundary
                smoothedHeading = smoothAngle(smoothedHeading, rawAzimuth, 0.15f)

                updateQiblaState(smoothedHeading, event.accuracy)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _state.value = _state.value.copy(accuracy = accuracy)
    }

    private fun updateQiblaState(heading: Float, accuracy: Int) {
        // Relative angle pointing towards Kaaba
        // pointerAngle = (QiblaBearing - DeviceHeading)
        var relativeAngle = (calculatedQiblaBearing - heading + 360f) % 360f

        // Check if facing Qibla within +/- 3 degrees
        val diffFromQibla = Math.abs(relativeAngle - if (relativeAngle > 180f) 360f else 0f)
        val isFacing = diffFromQibla <= 3.5f

        _state.value = _state.value.copy(
            deviceHeading = heading,
            qiblaBearing = calculatedQiblaBearing,
            qiblaRelativeAngle = relativeAngle,
            isFacingQibla = isFacing,
            accuracy = accuracy,
            hasCompassSensors = (accelerometer != null && magnetometer != null)
        )
    }

    companion object {
        const val KAABA_LATITUDE = 21.422487
        const val KAABA_LONGITUDE = 39.826206

        /**
         * Calculates true great-circle bearing to Kaaba from any location on Earth.
         */
        fun calculateBearingToKaaba(lat1: Double, lon1: Double): Float {
            val lat1Rad = Math.toRadians(lat1)
            val lon1Rad = Math.toRadians(lon1)
            val lat2Rad = Math.toRadians(KAABA_LATITUDE)
            val lon2Rad = Math.toRadians(KAABA_LONGITUDE)

            val dLon = lon2Rad - lon1Rad

            val y = sin(dLon)
            val x = cos(lat1Rad) * tan(lat2Rad) - sin(lat1Rad) * cos(dLon)

            val initialBearing = Math.toDegrees(atan2(y, x))
            return ((initialBearing + 360.0) % 360.0).toFloat()
        }

        private fun smoothAngle(current: Float, target: Float, factor: Float): Float {
            var diff = (target - current) % 360f
            if (diff > 180f) diff -= 360f
            if (diff < -180f) diff += 360f
            return (current + diff * factor + 360f) % 360f
        }
    }
}
