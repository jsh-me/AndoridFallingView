package com.jshme.fallingview

import android.widget.ImageView
import kotlin.random.Random

data class TomatoModel(
    val tomato: ImageView,
    val speed: Float = Random.nextInt(2, 10).toFloat(),
)
