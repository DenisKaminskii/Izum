package com.izum.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt

fun getBackgroundGradient(@ColorInt color: Int): GradientDrawable {
    val deltaRed = 35
    val deltaGreen = 22
    val deltaBlue = 14

    val red = Color.red(color)
    val green = Color.green(color)
    val blue = Color.blue(color)

    val darkerRed = (red - deltaRed).coerceAtLeast(0)
    val darkerGreen = (green - deltaGreen).coerceAtLeast(0)
    val darkerBlue = (blue - deltaBlue).coerceAtLeast(0)

    return GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(color, Color.rgb(darkerRed, darkerGreen, darkerBlue))
    )
}