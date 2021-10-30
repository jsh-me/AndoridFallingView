package com.jshme.fallingview

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.jshme.fallingview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding

    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null

    private val fallingModels: MutableList<TomatoModel> = mutableListOf()
    private val randomImageRes = listOf(
        R.drawable.tomato_green,
        R.drawable.tomato_red,
        R.drawable.tomato_yellow,
        R.drawable.tomato_yellow2,
        R.drawable.tomato_red2,
        R.drawable.tomato_green2,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSensorManager()
        initTouchListener()
    }

    private fun initSensorManager() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTouchListener() {
        binding.root.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val touchedX = event.x
                    val touchedY = event.y
                    addTomatoView(touchedX, touchedY)
                    true
                }
                else -> true
            }
        }
    }

    private fun addTomatoView(touchedX: Float, touchedY: Float) {
        val tomato = ImageView(this).apply {
            setBackgroundResource(randomImageRes.random())
            layoutParams = LinearLayout.LayoutParams(TOMATO_SIZE, TOMATO_SIZE)
            /**
             * 좌표는 뷰의 왼쪽 상단이 기준점
             */
            x = touchedX - TOMATO_SIZE / 2
            y = touchedY - TOMATO_SIZE / 2
        }
        binding.root.addView(tomato)
        fallingModels.add(TomatoModel(tomato))
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == accelerometerSensor) {
            fallingModels.map {
                val exX = event!!.values[0] * it.speed
                val exY = event.values[1] * it.speed

                /**
                 * 오른쪽은 -, 왼쪽은 + 좌표
                 * 위는 -, 아래는 + 좌표
                 */
                with(it.tomato) {
                    x -= exX
                    y += exY

                    if (y > getRealRootViewHeight()) y = getRealRootViewHeight().toFloat()
                    if (y < 0) y = 0f

                    if (x > getRealRootViewWidth()) x = getRealRootViewWidth().toFloat()
                    if (x < 0) x = 0f
                }
            }
        }
    }

    private fun getRealRootViewWidth(): Int {
        return window.decorView.width - TOMATO_SIZE
    }

    private fun getRealRootViewHeight(): Int {
        return if (Build.VERSION.SDK_INT < 30) {
            window.decorView.height - TOMATO_SIZE - window.decorView.rootWindowInsets.run {
                systemWindowInsetTop + systemWindowInsetBottom
            }
        } else {
            val insets = window.decorView.rootWindowInsets.displayCutout?.run {
                safeInsetBottom + safeInsetTop
            } ?: 0
            window.decorView.height - TOMATO_SIZE - insets
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    companion object {
        private const val TOMATO_SIZE = 140
    }
}
