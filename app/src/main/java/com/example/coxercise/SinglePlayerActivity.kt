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
import com.example.coxercise.listeners.sensorEvent.OrientationDetector
import com.example.coxercise.listeners.sensorEvent.StepDetector
import kotlin.math.roundToInt
import kotlin.math.sqrt


class SinglePlayerActivity: AppCompatActivity() {

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

        sensorManager.registerListener(StepDetector(stepDisplay), stepSensor, SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(OrientationDetector(windowManager, angleDisplay), orientationSensor,
            SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
    }
}