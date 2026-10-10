package com.magmacoin.leap.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.magmacoin.leap.R
import com.magmacoin.leap.databinding.ViewStatCardBinding

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStatCardBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.bg_stat_card)
        val padH = resources.getDimensionPixelSize(R.dimen.gap_s)
        val padV = resources.getDimensionPixelSize(R.dimen.gap_m)
        setPadding(padH, padV, padH, padV)
        val defaults = ContextCompat.getColor(context, R.color.ember_500)
        var label = ""
        var value = ""
        var accent = defaults
        if (attrs != null) {
            val typed = context.obtainStyledAttributes(attrs, R.styleable.StatCardView, defStyleAttr, 0)
            label = typed.getString(R.styleable.StatCardView_statLabel) ?: ""
            value = typed.getString(R.styleable.StatCardView_statValue) ?: ""
            accent = typed.getColor(R.styleable.StatCardView_statAccent, defaults)
            typed.recycle()
        }
        bind(label, value, accent)
    }

    fun bind(label: String, value: String, accent: Int) {
        binding.statLabel.text = label
        binding.statValue.text = value
        binding.statValue.setTextColor(accent)
        val dot = ContextCompat.getDrawable(context, R.drawable.bg_accent_dot)?.mutate()
        dot?.setTint(accent)
        binding.statAccentDot.background = dot
        contentDescription = label + " " + value
    }

    fun setVisibleIfNonZero(value: Int) {
        visibility = if (value > 0) View.VISIBLE else View.GONE
    }
}
