package com.example.suntrackir

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withSave
import kotlin.math.*

class SunMoonAnimationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var currentMinutes: Int = 720
    var sunriseMinutes: Int = 360
    var sunsetMinutes: Int = 1080
    var moonriseMinutes: Int = 0
    var moonsetMinutes: Int = 1440
    var sunProgress: Float = 0f
    var moonProgress: Float = 0f
    var moonPhase: Float = 0.5f

    var astronomicalTwilightRange: Pair<Int, Int>? = null
    var nauticalTwilightRange: Pair<Int, Int>? = null
    var civilTwilightRange: Pair<Int, Int>? = null
    var goldenHourMorningRange: Pair<Int, Int>? = null
    var goldenHourEveningRange: Pair<Int, Int>? = null
    var blueHourMorningRange: Pair<Int, Int>? = null
    var blueHourEveningRange: Pair<Int, Int>? = null

    private val flatFactor = 0.65f // Flatter arc for natural look
    private val glowPaint = Paint().apply {
        isAntiAlias = true
        setShadowLayer(20f, 0f, 0f, Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2
        val cy = h * 0.9f
        val radius = min(w / 2, h * 0.35f) * flatFactor
        val arcRect = RectF(cx - radius, cy - radius * flatFactor, cx + radius, cy + radius * flatFactor)

        // Dynamic background
        if (!isDayTime()) {
            canvas.drawColor(Color.parseColor("#001020"))
            drawStars(canvas, 80, width, height)
        }

        // Draw twilight arcs
        drawTwilightArc(canvas, arcRect, astronomicalTwilightRange, Color.parseColor("#0D47A1"), 16f)
        drawTwilightArc(canvas, arcRect, nauticalTwilightRange, Color.parseColor("#1565C0"), 14f)
        drawTwilightArc(canvas, arcRect, civilTwilightRange, Color.parseColor("#42A5F5"), 12f)
        drawTwilightArc(canvas, arcRect, goldenHourMorningRange, Color.parseColor("#FFD54F"), 10f)
        drawTwilightArc(canvas, arcRect, goldenHourEveningRange, Color.parseColor("#FFA000"), 10f)
        drawTwilightArc(canvas, arcRect, blueHourMorningRange, Color.parseColor("#64B5F6"), 8f)
        drawTwilightArc(canvas, arcRect, blueHourEveningRange, Color.parseColor("#2196F3"), 8f)

        // Draw sky path
        canvas.drawArc(arcRect, 180f, 180f, false, Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        })

        // Draw sun with glow
        if (isDayTime()) {
            drawBody(canvas, cx, cy, radius, sunProgress, Color.YELLOW, true)
        }

        // Draw moon with phase and glow
        if (isMoonVisible()) {
            drawBody(canvas, cx, cy, radius, moonProgress, Color.LTGRAY, false)
            drawMoonPhase(canvas, cx, cy, radius)
        }

        // Draw status text
        val sunStatus = when {
            currentMinutes < sunriseMinutes -> "پیش از طلوع"
            currentMinutes in sunriseMinutes..sunriseMinutes + 30 -> "طلوع خورشید"
            currentMinutes in (sunriseMinutes + sunsetMinutes) / 2 - 30..(sunriseMinutes + sunsetMinutes) / 2 + 30 -> "اوج خورشید"
            currentMinutes in sunsetMinutes - 30..sunsetMinutes -> "غروب خورشید"
            else -> "خورشید در آسمان"
        }
        val moonStatus = when {
            moonPhase < 0.05f -> "ماه نو"
            moonPhase > 0.95f -> "ماه کامل"
            currentMinutes in moonriseMinutes..moonriseMinutes + 30 -> "طلوع ماه"
            currentMinutes in moonsetMinutes - 30..moonsetMinutes -> "غروب ماه"
            else -> "ماه در آسمان"
        }
        drawStatusText(canvas, "$sunStatus / $moonStatus", cx, cy - radius - 30f)
    }

    private fun isDayTime(): Boolean = currentMinutes in sunriseMinutes..sunsetMinutes
    private fun isMoonVisible(): Boolean = currentMinutes in moonriseMinutes..moonsetMinutes

    private fun drawBody(canvas: Canvas, cx: Float, cy: Float, r: Float, progress: Float, color: Int, isSun: Boolean) {
        val angle = 180f + progress * 180f
        val x = cx + r * cos(Math.toRadians(angle.toDouble())).toFloat()
        val y = cy - r * sin(Math.toRadians(angle.toDouble())).toFloat() * flatFactor

        canvas.withSave {
            // Draw glow
            if (isSun) {
                glowPaint.color = Color.argb(100, 255, 255, 0)
                drawCircle(x, y, 35f, glowPaint)
            }
            // Draw body
            drawCircle(x, y, 25f, Paint().apply {
                this.color = color
                style = Paint.Style.FILL
                isAntiAlias = true
            })
        }
    }

    private fun drawMoonPhase(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val angle = 180f + moonProgress * 180f
        val x = cx + r * cos(Math.toRadians(angle.toDouble())).toFloat()
        val y = cy - r * sin(Math.toRadians(angle.toDouble())).toFloat() * flatFactor

        val brightness = 1 - abs(0.5f - moonPhase) * 2
        val moonColor = Color.argb(255, (200 * brightness).toInt(), (200 * brightness).toInt(), (200 * brightness).toInt())

        canvas.withSave {
            // Draw glow
            glowPaint.color = Color.argb(80, 200, 200, 200)
            drawCircle(x, y, 30f, glowPaint)
            // Draw moon
            drawCircle(x, y, 20f, Paint().apply {
                color = moonColor
                style = Paint.Style.FILL
                isAntiAlias = true
            })
            // Draw phase
            val offset = 20f * (1 - 2 * moonPhase)
            val phaseRect = RectF(x - 20f + offset, y - 20f, x + 20f + offset, y + 20f)
            drawArc(phaseRect, 90f, 180f, true, Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
                isAntiAlias = true
            })
        }
    }

    private fun drawTwilightArc(canvas: Canvas, rect: RectF, range: Pair<Int, Int>?, color: Int, strokeWidth: Float) {
        if (range == null) return
        val fullDay = 1440f
        val startAngle = 180f + (range.first / fullDay) * 180f
        val endAngle = 180f + (range.second / fullDay) * 180f
        val sweep = (endAngle - startAngle).let { if (it < 0) it + 180 else it }.coerceIn(1f, 180f)

        canvas.drawArc(rect, startAngle, sweep, false, Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            isAntiAlias = true
        })
    }

    private fun drawStatusText(canvas: Canvas, text: String, x: Float, y: Float) {
        canvas.drawText(text, x, y, Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(10f, 0f, 0f, Color.BLACK)
        })
    }

    private fun drawStars(canvas: Canvas, count: Int, width: Int, height: Int) {
        val paint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        repeat(count) {
            val x = (Math.random() * width).toFloat()
            val y = (Math.random() * height * 0.7f).toFloat()
            val size = (1 + Math.random() * 3).toFloat()
            canvas.drawCircle(x, y, size, paint)
        }
    }
}