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
import com.example.coxercise.listeners.sensorEvent.StepDetector
import kotlin.math.roundToInt
import kotlin.math.sqrt


class SinglePlayerActivity: AppCompatActivity() {

    private var magnitudePrevious = 0.0
    private var stepCount = 0
    private var angle = 0
    private lateinit var stepDisplay: TextView
    private lateinit var angleDisplay: TextView
    private lateinit var binding: ActivitySinglePlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySinglePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stepDisplay = findViewById(R.id.stepsValue)
        angleDisplay = findViewById(R.id.angleValue)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val orientationDetector = object: SensorEventListener {
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

        sensorManager.registerListener(StepDetector(stepDisplay), stepSensor, SensorManager.SENSOR_DELAY_NORMAL,
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