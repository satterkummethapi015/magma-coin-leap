package com.magmacoin.leap.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.magmacoin.leap.R
import com.magmacoin.leap.databinding.ViewLaneButtonBinding

class LaneButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewLaneButtonBinding.inflate(LayoutInflater.from(context), this)
    private var laneIndex = 0
    private var locked = false
    private var listener: ((Int) -> Unit)? = null

    init {
        setBackgroundResource(R.drawable.bg_lane_button)
        isClickable = true
        isFocusable = true
        minimumHeight = resources.getDimensionPixelSize(R.dimen.lane_button_h)
        if (attrs != null) {
            val typed = context.obtainStyledAttributes(attrs, R.styleable.LaneButtonView, defStyleAttr, 0)
            laneIndex = typed.getInt(R.styleable.LaneButtonView_laneIndex, 0)
            val label = typed.getString(R.styleable.LaneButtonView_laneLabel)
            typed.recycle()
            if (label != null) {
                setLabel(label, laneIndex)
            }
        }
        setOnClickListener {
            try {
                if (!locked) {
                    listener?.invoke(laneIndex)
                }
            } catch (e: Exception) {
            }
        }
        PressScaleHelper.attach(this)
    }

    fun setLabel(label: String, index: Int) {
        laneIndex = index
        binding.laneLabel.text = label
        contentDescription = label
        refreshStateDescription()
    }

    fun setOnLeapListener(block: (Int) -> Unit) {
        listener = block
    }

    fun setLocked(value: Boolean) {
        locked = value
        isEnabled = !value
        alpha = if (value) 0.45f else 1f
        binding.laneLabel.setTextColor(
            ContextCompat.getColor(
                context,
                if (value) R.color.text_muted else R.color.text_primary
            )
        )
        refreshStateDescription()
    }

    fun setHighContrast(enabled: Boolean) {
        binding.laneLabel.setTextColor(
            ContextCompat.getColor(
                context,
                if (enabled) R.color.ember_500 else R.color.text_primary
            )
        )
    }

    fun detach() {
        listener = null
        PressScaleHelper.release(this)
    }

    private fun refreshStateDescription() {
        val state = context.getString(
            if (locked) R.string.lane_state_locked else R.string.lane_state_ready
        )
        val label = binding.laneLabel.text?.toString() ?: ""
        ViewCompat.setStateDescription(this, context.getString(R.string.fmt_lane_state, label, state))
    }
}
