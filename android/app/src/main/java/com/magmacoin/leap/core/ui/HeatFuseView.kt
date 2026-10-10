package com.magmacoin.leap.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.magmacoin.leap.R

class HeatFuseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var progress = 1f
    private var paused = false
    private var radius = 0f

    init {
        trackPaint.color = ContextCompat.getColor(context, R.color.basalt_600)
        fillPaint.color = ContextCompat.getColor(context, R.color.magma_500)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = h / 2f
        if (w > 0) {
            fillPaint.shader = LinearGradient(
                0f,
                0f,
                w.toFloat(),
                0f,
                ContextCompat.getColor(context, R.color.ember_500),
                ContextCompat.getColor(context, R.color.magma_500),
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) {
            return
        }
        rect.set(0f, 0f, w, h)
        canvas.drawRoundRect(rect, radius, radius, trackPaint)
        val filled = w * progress
        if (filled > 0f) {
            rect.set(0f, 0f, filled, h)
            canvas.drawRoundRect(rect, radius, radius, fillPaint)
        }
    }

    fun setProgress(value: Float) {
        if (paused) {
            return
        }
        val clamped = when {
            value < 0f -> 0f
            value > 1f -> 1f
            else -> value
        }
        if (clamped != progress) {
            progress = clamped
            invalidate()
        }
    }

    fun reset() {
        paused = false
        progress = 1f
        invalidate()
    }

    fun pause() {
        paused = true
    }
}
