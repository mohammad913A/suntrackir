package com.example.suntrackir

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class TimeGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var sunProgress: Float = 0.5f
    var moonProgress: Float = 0.5f
    var sunriseMinutes: Int = 360
    var sunsetMinutes: Int = 1080
    var moonriseMinutes: Int = 0
    var moonsetMinutes: Int = 1440

    private val sunPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val moonPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val pathSun = Path()
        val pathMoon = Path()

        // Simulate altitude (simplified as sinusoidal curve)
        for (x in 0..width step 10) {
            val t = x.toFloat() / w
            val minutes = (t * 1440).toInt()
            val sunY = if (minutes in sunriseMinutes..sunsetMinutes) {
                h / 2 * (1 - sin(t * Math.PI).toFloat())
            } else {
                h
            }
            val moonY = if (minutes in moonriseMinutes..moonsetMinutes) {
                h / 2 * (1 - sin((t + 0.5f) * Math.PI).toFloat())
            } else {
                h
            }

            if (x == 0) {
                pathSun.moveTo(x.toFloat(), sunY)
                pathMoon.moveTo(x.toFloat(), moonY)
            } else {
                pathSun.lineTo(x.toFloat(), sunY)
                pathMoon.lineTo(x.toFloat(), moonY)
            }
        }

        canvas.drawPath(pathSun, sunPaint)
        canvas.drawPath(pathMoon, moonPaint)
    }
}