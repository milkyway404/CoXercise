package com.example.coxercise

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Surface
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.coxercise.databinding.ActivitySinglePlayerBinding
import kotlin.math.roundToInt
import kotlin.math.sqrt


class SinglePlayerActivity: AppCompatActivity() {

    private var magnitudePrevious = 0.0
    private var stepCount = 0
    private var angle = 0
    private lateinit var stepDisplay: TextView
    private lateinit var angleDisplay: TextView
    private lateinit var binding: ActivitySinglePlayerBinding

    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySinglePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stepDisplay = findViewById(R.id.stepsValue)
        angleDisplay = findViewById(R.id.angleValue)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val stepDetector = object: SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) {
                    return
                }

                System.arraycopy(event.values, 0, accelerometerReading, 0,
                    accelerometerReading.size)

                val xAcc = event.values[0]
                val yAcc = event.values[1]
                val zAcc = event.values[2]

                val magnitude = sqrt((xAcc * xAcc + yAcc * yAcc + zAcc * zAcc).toDouble())
                print(magnitude)
                val magnitudeDelta = magnitude - magnitudePrevious
                magnitudePrevious = magnitude

                // change this for sensitivity
                if (magnitudeDelta > 4) {
                    stepCount++
                    stepDisplay.text = stepCount.toString()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // TODO("Not yet implemented")
            }
        }

        val orientationDetector = object: SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) {
                    return
                }



                System.arraycopy(
                    event.values, 0, magnetometerReading, 0,
                    magnetometerReading.size,
                )

                val rotationMatrix = FloatArray(16)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                var worldAxisForDeviceAxisX: Int
                var worldAxisForDeviceAxisY: Int

                // Remap the axes as if the device screen was the instrument panel,
                // and adjust the rotation matrix for the device orientation.

                when (windowManager.defaultDisplay.orientation) {
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

                /*

                val rotationMatrix = FloatArray(9)
                val orientationAngles = FloatArray(3)

                SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading,
                    magnetometerReading)
                val orientation = SensorManager.getOrientation(rotationMatrix,
                    orientationAngles) */

                // convert azimuth to degrees
                val degrees = Math.toDegrees(orientationValuesV[0].toDouble())

                // round to int
                angle = degrees.roundToInt()

                // can later get direction such as N, NS, NSW etc if needed

                angleDisplay.text = angle.toString()
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // TODO("Not yet implemented")
            }
        }

        sensorManager.registerListener(stepDetector, stepSensor, SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(orientationDetector, orientationSensor,
            SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onResume() {
        super.onResume()
        print("RESUMING...")

        val sharedPreferences = getPreferences(MODE_PRIVATE)
        stepCount = sharedPreferences.getInt("stepCount", 0)
    }

    override fun onPause() {
        super.onPause()
        print("PAUSING...")

        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.putInt("stepCount", stepCount)
        editor.apply()
    }

    override fun onStop() {
        super.onStop()
        print("STOPPING...")

        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.putInt("stepCount", stepCount)
        editor.apply()
    }
}