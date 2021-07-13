package com.example.coxercise.listeners.sensorEvent

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import android.widget.TextView
import kotlin.math.roundToInt

class OrientationDetector constructor(
    private val windowManager: WindowManager,
    private val angleDisplay: TextView): SensorEventListener {

    private var angle = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }

        val rotationMatrix = FloatArray(16)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        val worldAxisForDeviceAxisX: Int
        val worldAxisForDeviceAxisY: Int

        // Remap the axes as if the device screen was the instrument panel,
        // and adjust the rotation matrix for the device orientation.
        // default display is used for api < 30 so should be used for our purposes still.
        when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_90 -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X
            }
            Surface.ROTATION_180 -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z
            }
            Surface.ROTATION_270 -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z
                worldAxisForDeviceAxisY = SensorManager.AXIS_X
            }
            Surface.ROTATION_0 -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_X
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z
            }
            else -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_X
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z
            }
        }

        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            rotationMatrix, worldAxisForDeviceAxisX,
            worldAxisForDeviceAxisY, adjustedRotationMatrix
        )


        val orientationValuesV = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationValuesV)

        // convert azimuth to degrees
        val degrees = Math.toDegrees(orientationValuesV[0].toDouble())

        // round to int
        angle = degrees.roundToInt()

        // can later get direction such as N, NS, NSW etc if needed

        angleDisplay.text = angle.toString()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not important for now, could add checks for low accuracy
        // if needed.
    }
}