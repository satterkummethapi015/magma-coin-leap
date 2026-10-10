package com.magmacoin.leap.core.ui

import android.view.MotionEvent
import android.view.View

object PressScaleHelper {

    private const val PRESS_SCALE = 0.96f
    private const val DOWN_MS = 120L
    private const val UP_MS = 160L

    fun attach(view: View) {
        view.setOnTouchListener { target, event ->
            try {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> scale(target, PRESS_SCALE, DOWN_MS)
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> scale(target, 1f, UP_MS)
                    else -> Unit
                }
            } catch (e: Exception) {
            }
            false
        }
    }

    fun release(view: View) {
        try {
            view.setOnTouchListener(null)
            view.animate().cancel()
            view.scaleX = 1f
            view.scaleY = 1f
        } catch (e: Exception) {
        }
    }

    private fun scale(target: View, value: Float, duration: Long) {
        target.animate().cancel()
        target.animate()
            .scaleX(value)
            .scaleY(value)
            .setDuration(duration)
            .start()
    }
}
