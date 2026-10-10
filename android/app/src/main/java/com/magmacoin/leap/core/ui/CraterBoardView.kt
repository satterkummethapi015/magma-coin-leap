package com.magmacoin.leap.core.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.magmacoin.leap.R
import com.magmacoin.leap.core.config.GameConfig
import com.magmacoin.leap.domain.model.Cell
import com.magmacoin.leap.domain.model.CraterRow
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

class CraterBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val boardPad = resources.getDimensionPixelSize(R.dimen.board_pad)
    private val boardStroke = resources.getDimensionPixelSize(R.dimen.board_stroke)
    private val laneGap = resources.getDimensionPixelSize(R.dimen.lane_gap)
    private val rowGap = resources.getDimensionPixelSize(R.dimen.row_gap)
    private val cellRadius = resources.getDimension(R.dimen.cell_radius)
    private val hopHeight = resources.getDimension(R.dimen.leap_hop)
    private val shakeAmp = resources.getDimension(R.dimen.shake_amp)

    private val slabPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val slabStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val branchPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ventPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val flashPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rockDrawable: Drawable? =
        AppCompatResources.getDrawable(context, R.drawable.sprite_rock)?.mutate()
    private val coinDrawable: Drawable? =
        AppCompatResources.getDrawable(context, R.drawable.sprite_coin)?.mutate()
    private val ventDrawable: Drawable? =
        AppCompatResources.getDrawable(context, R.drawable.sprite_vent)?.mutate()
    private val explorerDrawable: Drawable? =
        AppCompatResources.getDrawable(context, R.drawable.sprite_explorer)?.mutate()

    private val rect = RectF()
    private val boardRect = RectF()

    private var laneW = 0
    private var rowH = 0
    private var boardW = 0
    private var boardH = 0
    private var originX = 0f
    private var originY = 0f

    private var rows: List<CraterRow> = emptyList()
    private var explorerLane = 1
    private var fromLane = 1
    private var targetLane = 1
    private var branchLane = -1
    private var ventPulseEnabled = true
    private var highContrast = false
    private var dimmed = false

    private var leaping = false
    private var leapProgress = 0f
    private var scrollFraction = 0f
    private var pulsePhase = 0f
    private var flashAlpha = 0f

    private var leapAnimator: ValueAnimator? = null
    private var pulseAnimator: ValueAnimator? = null
    private var flashAnimator: ValueAnimator? = null
    private var shakeAnimator: ObjectAnimator? = null

    init {
        slabPaint.style = Paint.Style.FILL
        slabPaint.color = ContextCompat.getColor(context, R.color.basalt_700)
        slabStrokePaint.style = Paint.Style.STROKE
        slabStrokePaint.strokeWidth = boardStroke.toFloat()
        slabStrokePaint.color = ContextCompat.getColor(context, R.color.gold_stroke)
        branchPaint.style = Paint.Style.STROKE
        branchPaint.strokeWidth = boardStroke.toFloat()
        branchPaint.color = ContextCompat.getColor(context, R.color.ember_500)
        glowPaint.style = Paint.Style.FILL
        glowPaint.color = ContextCompat.getColor(context, R.color.ember_300)
        ventPaint.style = Paint.Style.FILL
        ventPaint.color = ContextCompat.getColor(context, R.color.magma_500)
        flashPaint.style = Paint.Style.FILL
        flashPaint.color = ContextCompat.getColor(context, R.color.magma_500)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val frame = boardPad + boardStroke
        val usableW = w - 2 * frame - (GameConfig.LANES - 1) * laneGap
        val usableH = h - 2 * frame - (GameConfig.VISIBLE_ROWS - 1) * rowGap
        laneW = if (usableW > 0) floor(usableW / GameConfig.LANES.toFloat()).toInt() else 0
        rowH = if (usableH > 0) floor(usableH / GameConfig.VISIBLE_ROWS.toFloat()).toInt() else 0
        boardW = laneW * GameConfig.LANES + (GameConfig.LANES - 1) * laneGap + 2 * frame
        boardH = rowH * GameConfig.VISIBLE_ROWS + (GameConfig.VISIBLE_ROWS - 1) * rowGap + 2 * frame
        originX = maxOf(0f, (w - boardW) / 2f)
        originY = maxOf(0f, (h - boardH) / 2f)
        boardRect.set(originX, originY, originX + boardW, originY + boardH)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startPulse()
    }

    override fun onDetachedFromWindow() {
        cancelAnimations()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (laneW <= 0 || rowH <= 0 || rows.isEmpty()) {
            return
        }
        canvas.save()
        canvas.clipRect(boardRect)
        val shift = scrollFraction * (rowH + rowGap)
        val count = min(rows.size, GameConfig.BUFFER_ROWS)
        for (index in 0 until count) {
            drawRow(canvas, rows[index], index, shift)
        }
        drawExplorer(canvas)
        canvas.restore()
        if (flashAlpha > 0f) {
            flashPaint.alpha = clampAlpha(flashAlpha * 150f)
            canvas.drawRoundRect(boardRect, cellRadius * 2f, cellRadius * 2f, flashPaint)
            flashPaint.alpha = 255
        }
    }

    fun render(
        newRows: List<CraterRow>,
        lane: Int,
        branch: Int,
        ventPulse: Boolean,
        contrast: Boolean,
        locked: Boolean
    ) {
        rows = newRows
        branchLane = branch
        ventPulseEnabled = ventPulse
        highContrast = contrast
        dimmed = locked
        if (!leaping) {
            explorerLane = lane
            fromLane = lane
            targetLane = lane
        }
        invalidate()
    }

    fun animateLeap(lane: Int, onEnd: () -> Unit) {
        try {
            leapAnimator?.cancel()
            fromLane = explorerLane
            targetLane = lane
            leaping = true
            var cancelled = false
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = GameConfig.LEAP_ANIM_MS
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { value ->
                try {
                    val raw = value.animatedValue
                    if (raw is Float) {
                        leapProgress = raw
                        scrollFraction = raw
                        invalidate()
                    }
                } catch (e: Exception) {
                }
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    try {
                        cancelled = true
                    } catch (e: Exception) {
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    try {
                        leaping = false
                        leapProgress = 0f
                        scrollFraction = 0f
                        explorerLane = targetLane
                        fromLane = targetLane
                        invalidate()
                        if (!cancelled && isAttachedToWindow) {
                            onEnd()
                        }
                    } catch (e: Exception) {
                    }
                }
            })
            leapAnimator = animator
            animator.start()
        } catch (e: Exception) {
            onEnd()
        }
    }

    fun flashFail() {
        try {
            shakeAnimator?.cancel()
            val shake = ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 0f, shakeAmp)
            shake.duration = GameConfig.FAIL_ANIM_MS
            shake.interpolator = CycleInterpolator(3f)
            shakeAnimator = shake
            shake.start()
            flashAnimator?.cancel()
            val flash = ValueAnimator.ofFloat(0f, 1f, 0f)
            flash.duration = GameConfig.FAIL_ANIM_MS
            flash.interpolator = AccelerateDecelerateInterpolator()
            flash.addUpdateListener { value ->
                try {
                    val raw = value.animatedValue
                    if (raw is Float) {
                        flashAlpha = raw
                        invalidate()
                    }
                } catch (e: Exception) {
                }
            }
            flashAnimator = flash
            flash.start()
        } catch (e: Exception) {
        }
    }

    fun cancelAnimations() {
        try {
            leapAnimator?.cancel()
            pulseAnimator?.cancel()
            flashAnimator?.cancel()
            shakeAnimator?.cancel()
            leapAnimator = null
            pulseAnimator = null
            flashAnimator = null
            shakeAnimator = null
            translationX = 0f
        } catch (e: Exception) {
        }
    }

    private fun startPulse() {
        try {
            pulseAnimator?.cancel()
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = 840L
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener { value ->
                try {
                    val raw = value.animatedValue
                    if (raw is Float) {
                        pulsePhase = raw
                        if (ventPulseEnabled) {
                            invalidate()
                        }
                    }
                } catch (e: Exception) {
                }
            }
            pulseAnimator = animator
            animator.start()
        } catch (e: Exception) {
        }
    }

    private fun drawRow(canvas: Canvas, row: CraterRow, rowIndex: Int, shift: Float) {
        for (lane in 0 until GameConfig.LANES) {
            val left = cellLeft(lane)
            val top = cellTop(rowIndex, shift)
            rect.set(left, top, left + laneW, top + rowH)
            drawCell(canvas, row.cells.getOrNull(lane), rowIndex, lane)
        }
    }

    private fun drawCell(canvas: Canvas, cell: Cell?, rowIndex: Int, lane: Int) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val inset = rect.width() * 0.06f
        canvas.drawRoundRect(rect, cellRadius, cellRadius, slabPaint)
        slabStrokePaint.alpha = if (highContrast) 255 else 140
        canvas.drawRoundRect(rect, cellRadius, cellRadius, slabStrokePaint)
        when (cell) {
            is Cell.Rock -> drawFit(canvas, rockDrawable, cx, cy, rect.width() - inset, rect.height() - inset, 255)
            is Cell.Coin -> {
                drawFit(canvas, rockDrawable, cx, cy, rect.width() - inset, rect.height() - inset, 210)
                glowPaint.alpha = 60
                canvas.drawCircle(cx, cy, min(rect.width(), rect.height()) * 0.30f, glowPaint)
                glowPaint.alpha = 255
                val coinSize = min(rect.width(), rect.height()) * 0.46f
                drawFit(canvas, coinDrawable, cx, cy, coinSize, coinSize, 255)
            }
            is Cell.Vent -> {
                drawFit(canvas, ventDrawable, cx, cy, rect.width() - inset, rect.height() - inset, 255)
                val phase = if (ventPulseEnabled) pulsePhase else 0.5f
                ventPaint.alpha = clampAlpha(60f + 60f * phase)
                canvas.drawCircle(cx, cy, min(rect.width(), rect.height()) * (0.30f + 0.06f * phase), ventPaint)
                ventPaint.alpha = 255
            }
            else -> Unit
        }
        if (rowIndex == 1 && cell is Cell.Coin) {
            branchPaint.alpha = if (lane == branchLane) 255 else 120
            canvas.drawRoundRect(rect, cellRadius, cellRadius, branchPaint)
            branchPaint.alpha = 255
        }
    }

    private fun drawExplorer(canvas: Canvas) {
        val size = min(laneW, rowH) * 0.58f
        val startX = cellLeft(fromLane) + laneW / 2f
        val endX = cellLeft(targetLane) + laneW / 2f
        val x = startX + (endX - startX) * leapProgress
        val baseY = cellTop(0, 0f) + rowH / 2f
        val y = baseY - hopHeight * sin(Math.PI.toFloat() * leapProgress)
        val alpha = if (dimmed) 150 else 255
        drawFit(canvas, explorerDrawable, x, y, size, size, alpha)
    }

    private fun drawFit(
        canvas: Canvas,
        drawable: Drawable?,
        cx: Float,
        cy: Float,
        maxW: Float,
        maxH: Float,
        alpha: Int
    ) {
        if (drawable == null || maxW <= 0f || maxH <= 0f) {
            return
        }
        val iw = maxOf(1, drawable.intrinsicWidth).toFloat()
        val ih = maxOf(1, drawable.intrinsicHeight).toFloat()
        val scale = min(maxW / iw, maxH / ih)
        val w = iw * scale
        val h = ih * scale
        drawable.alpha = alpha
        drawable.setBounds(
            (cx - w / 2f).toInt(),
            (cy - h / 2f).toInt(),
            (cx + w / 2f).toInt(),
            (cy + h / 2f).toInt()
        )
        drawable.draw(canvas)
    }

    private fun cellLeft(lane: Int): Float =
        originX + boardPad + boardStroke + lane * (laneW + laneGap)

    private fun cellTop(rowIndex: Int, shift: Float): Float =
        originY + boardPad + boardStroke +
            (GameConfig.VISIBLE_ROWS - 1 - rowIndex) * (rowH + rowGap) + shift

    private fun clampAlpha(value: Float): Int {
        val raw = value.toInt()
        return when {
            raw < 0 -> 0
            raw > 255 -> 255
            else -> raw
        }
    }
}
