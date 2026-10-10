package com.magmacoin.leap.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.magmacoin.leap.R
import com.magmacoin.leap.databinding.ViewTokenBadgeBinding

class TokenBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewTokenBadgeBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundResource(R.drawable.bg_token_badge)
        val padH = resources.getDimensionPixelSize(R.dimen.gap_m)
        val padV = resources.getDimensionPixelSize(R.dimen.gap_xs)
        setPadding(padH, padV, padH, padV)
        setCount(0)
    }

    fun setCount(value: Int) {
        binding.badgeCount.text = context.getString(R.string.fmt_count, value)
        contentDescription = context.getString(R.string.cd_coin) + " " + value
    }

    fun pulse() {
        try {
            animate().cancel()
            scaleX = 1f
            scaleY = 1f
            animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(90L)
                .withEndAction {
                    try {
                        if (!isAttachedToWindow) {
                            return@withEndAction
                        }
                        animate().scaleX(1f).scaleY(1f).setDuration(90L).start()
                    } catch (e: Exception) {
                    }
                }
                .start()
        } catch (e: Exception) {
        }
    }

    fun stop() {
        try {
            animate().cancel()
            scaleX = 1f
            scaleY = 1f
        } catch (e: Exception) {
        }
    }
}
