package com.example.coxercise.listeners.sensorEvent

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.widget.TextView
import kotlin.math.sqrt

private const val STEPS_INTERVAL_MS = 200
// the smaller the more sensitive
private const val SENSOR_SENSITIVITY_THRESHOLD = 4

class StepDetector constructor(private val stepDisplay: TextView): SensorEventListener {

    private var magnitudePrevious = 0.0
    private var stepCount = 0
    private var lastUpdateTime = System.currentTimeMillis()

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }

        val xAcc = event.values[0]
        val yAcc = event.values[1]
        val zAcc = event.values[2]

        val magnitude = sqrt((xAcc * xAcc + yAcc * yAcc + zAcc * zAcc).toDouble())
        print(magnitude)
        val magnitudeDelta = magnitude - magnitudePrevious
        magnitudePrevious = magnitude

        // change this for sensitivity
        // adds a delay between when steps are registered so one
        // step isn't registered as multiple
        if (magnitudeDelta >= SENSOR_SENSITIVITY_THRESHOLD &&
                System.currentTimeMillis() - lastUpdateTime > STEPS_INTERVAL_MS) {
            stepCount++
            stepDisplay.text = stepCount.toString()
            lastUpdateTime = System.currentTimeMillis()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not important for now, could add checks for low accuracy
        // if needed.
    }
}