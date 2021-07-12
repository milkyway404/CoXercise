package com.example.coxercise

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.coxercise.databinding.ActivitySinglePlayerBinding
import kotlin.math.sqrt


class SinglePlayerActivity: AppCompatActivity() {

    private var magnitudePrevious = 0.0
    private var stepCount = 0
    private lateinit var stepDisplay: TextView
    private lateinit var binding: ActivitySinglePlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySinglePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stepDisplay = findViewById(R.id.stepsValue)
        
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val stepDetector = object: SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
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
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // TODO("Not yet implemented")
            }
        }

        sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_NORMAL)
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