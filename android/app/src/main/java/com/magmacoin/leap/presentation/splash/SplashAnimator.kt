package com.magmacoin.leap.presentation.splash

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.magmacoin.leap.databinding.FragmentSplashBinding

class SplashAnimator(private val binding: FragmentSplashBinding) {

    private val running = ArrayList<Animator>()

    fun prepare() {
        try {
            binding.medallion.alpha = 0f
            binding.medallion.scaleX = START_SCALE
            binding.medallion.scaleY = START_SCALE
            binding.emberRing.alpha = 0f
            binding.splashTitle.alpha = 0f
            binding.splashTitle.translationY = TITLE_OFFSET
            binding.splashSubtitle.alpha = 0f
            binding.splashProgress.alpha = 0f
            binding.splashCaption.alpha = 0f
            binding.splashFooter.alpha = 0f
            binding.moteA.alpha = 0f
            binding.moteB.alpha = 0f
            binding.moteC.alpha = 0f
        } catch (e: Exception) {
        }
    }

    fun playEntry() {
        try {
            val medallionScaleX = ObjectAnimator.ofFloat(binding.medallion, View.SCALE_X, START_SCALE, 1f)
            val medallionScaleY = ObjectAnimator.ofFloat(binding.medallion, View.SCALE_Y, START_SCALE, 1f)
            val medallionFade = ObjectAnimator.ofFloat(binding.medallion, View.ALPHA, 0f, 1f)
            val medallionSet = AnimatorSet()
            medallionSet.playTogether(medallionScaleX, medallionScaleY, medallionFade)
            medallionSet.duration = MEDALLION_MS
            medallionSet.interpolator = OvershootInterpolator(1.1f)
            track(medallionSet)

            val ringFade = ObjectAnimator.ofFloat(binding.emberRing, View.ALPHA, 0f, 1f)
            ringFade.duration = MEDALLION_MS
            ringFade.interpolator = DecelerateInterpolator()
            track(ringFade)

            val titleFade = ObjectAnimator.ofFloat(binding.splashTitle, View.ALPHA, 0f, 1f)
            val titleSlide = ObjectAnimator.ofFloat(binding.splashTitle, View.TRANSLATION_Y, TITLE_OFFSET, 0f)
            val titleSet = AnimatorSet()
            titleSet.playTogether(titleFade, titleSlide)
            titleSet.duration = TITLE_MS
            titleSet.startDelay = TITLE_DELAY_MS
            titleSet.interpolator = DecelerateInterpolator()
            track(titleSet)

            track(fadeIn(binding.splashSubtitle, SUBTITLE_MS, SUBTITLE_DELAY_MS))
            track(fadeIn(binding.splashProgress, SUBTITLE_MS, PROGRESS_DELAY_MS))
            track(fadeIn(binding.splashCaption, SUBTITLE_MS, CAPTION_DELAY_MS))
            track(fadeIn(binding.splashFooter, SUBTITLE_MS, FOOTER_DELAY_MS))
        } catch (e: Exception) {
        }
    }

    fun startLoops() {
        try {
            val ringAlpha = ObjectAnimator.ofFloat(binding.emberRing, View.ALPHA, 0.35f, 0.8f)
            val ringScaleX = ObjectAnimator.ofFloat(binding.emberRing, View.SCALE_X, 1f, 1.12f)
            val ringScaleY = ObjectAnimator.ofFloat(binding.emberRing, View.SCALE_Y, 1f, 1.12f)
            val loops = listOf(ringAlpha, ringScaleX, ringScaleY)
            for (animator in loops) {
                animator.duration = RING_LOOP_MS
                animator.startDelay = RING_LOOP_DELAY_MS
                animator.repeatCount = ValueAnimator.INFINITE
                animator.repeatMode = ValueAnimator.REVERSE
                animator.interpolator = AccelerateDecelerateInterpolator()
                track(animator)
            }
            track(moteLoop(binding.moteA, MOTE_A_MS, 0L))
            track(moteLoop(binding.moteB, MOTE_B_MS, 320L))
            track(moteLoop(binding.moteC, MOTE_C_MS, 640L))
        } catch (e: Exception) {
        }
    }

    fun cancel() {
        try {
            for (animator in running) {
                animator.removeAllListeners()
                animator.cancel()
            }
            running.clear()
            binding.medallion.animate().cancel()
            binding.emberRing.animate().cancel()
            binding.splashTitle.animate().cancel()
        } catch (e: Exception) {
        }
    }

    private fun moteLoop(target: View, duration: Long, delay: Long): Animator {
        val rise = ObjectAnimator.ofFloat(target, View.TRANSLATION_Y, 0f, MOTE_RISE)
        val fade = ObjectAnimator.ofFloat(target, View.ALPHA, 0.9f, 0f)
        rise.repeatCount = ValueAnimator.INFINITE
        rise.repeatMode = ValueAnimator.RESTART
        fade.repeatCount = ValueAnimator.INFINITE
        fade.repeatMode = ValueAnimator.RESTART
        val set = AnimatorSet()
        set.playTogether(rise, fade)
        set.duration = duration
        set.startDelay = delay
        set.interpolator = DecelerateInterpolator()
        return set
    }

    private fun fadeIn(target: View, duration: Long, delay: Long): Animator {
        val animator = ObjectAnimator.ofFloat(target, View.ALPHA, 0f, 1f)
        animator.duration = duration
        animator.startDelay = delay
        animator.interpolator = DecelerateInterpolator()
        return animator
    }

    private fun track(animator: Animator) {
        running.add(animator)
        animator.start()
    }

    companion object {
        private const val START_SCALE = 0.72f
        private const val TITLE_OFFSET = 56f
        private const val MOTE_RISE = -150f
        private const val MEDALLION_MS = 520L
        private const val TITLE_MS = 420L
        private const val TITLE_DELAY_MS = 220L
        private const val SUBTITLE_MS = 360L
        private const val SUBTITLE_DELAY_MS = 420L
        private const val PROGRESS_DELAY_MS = 560L
        private const val CAPTION_DELAY_MS = 680L
        private const val FOOTER_DELAY_MS = 800L
        private const val RING_LOOP_MS = 1600L
        private const val RING_LOOP_DELAY_MS = 560L
        private const val MOTE_A_MS = 2400L
        private const val MOTE_B_MS = 2800L
        private const val MOTE_C_MS = 3200L
    }
}
