package com.fibcam.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Reads device accelerometer to compute pitch/roll for level indicator.
 * Emits device tilt in degrees suitable for horizon overlay rendering.
 */
class AccelerometerHelper @Inject constructor(
    context: Context
) : SensorEventListener {

    companion object {
        private const val TAG = "AccelerometerHelper"
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _tiltDegrees = MutableStateFlow(0f)
    val tiltDegrees: StateFlow<Float> = _tiltDegrees.asStateFlow()

    private var isListening = false

    fun startListening() {
        if (isListening || accelerometer == null) return
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        }
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Compute pitch (tilt angle) from gravity vector
        val magnitude = sqrt(x * x + y * y + z * z)
        val normalizedX = x / magnitude
        val normalizedZ = z / magnitude

        // Pitch: angle from horizontal (-90 to +90)
        val pitch = Math.toDegrees(atan2(normalizedZ.toDouble(), normalizedX.toDouble())).toFloat()

        _tiltDegrees.value = pitch
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed
    }
}