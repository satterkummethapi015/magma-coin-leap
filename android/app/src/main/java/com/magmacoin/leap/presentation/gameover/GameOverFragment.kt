package com.magmacoin.leap.presentation.gameover

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.magmacoin.leap.R
import com.magmacoin.leap.core.di.ServiceLocator
import com.magmacoin.leap.core.di.ViewModelFactory
import com.magmacoin.leap.core.navigation.Navigator
import com.magmacoin.leap.databinding.FragmentGameOverBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GameOverFragment : Fragment() {

    private var binding: FragmentGameOverBinding? = null
    private var countUp: ValueAnimator? = null
    private var ribbonPulse: Animator? = null
    private var counted = false

    private val viewModel: GameOverViewModel by viewModels {
        ViewModelFactory {
            val args = arguments
            GameOverViewModel(
                ServiceLocator.progressRepository(requireContext()),
                args?.getInt(ARG_ALTITUDE) ?: 0,
                args?.getInt(ARG_TOKENS) ?: 0,
                args?.getInt(ARG_LEAPS) ?: 0,
                args?.getBoolean(ARG_NEW_BEST) ?: false
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = FragmentGameOverBinding.inflate(inflater, container, false)
        binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val current = binding ?: return
        current.btnAgain.setOnClickListener {
            try {
                Navigator.replayFromResult(parentFragmentManager)
            } catch (e: Exception) {
            }
        }
        current.btnMenu.setOnClickListener {
            try {
                Navigator.backToMenu(parentFragmentManager)
            } catch (e: Exception) {
            }
        }
        playEntry(current)
        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> render(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun render(state: GameOverUiState) {
        val current = binding ?: return
        val context = context ?: return
        val verdictColor = if (state.summit) R.color.ember_500 else R.color.magma_500
        current.resultVerdict.setText(
            if (state.summit) R.string.result_summit else R.string.result_ended
        )
        current.resultVerdict.setTextColor(ContextCompat.getColor(context, verdictColor))
        current.resultTokens.text = getString(R.string.fmt_tokens_gain, state.tokens)
        current.resultAltitude.text = getString(R.string.fmt_count, state.altitude)
        if (!counted && state.altitude > 0) {
            counted = true
            startCountUp(state.altitude)
        }
        current.resultStatLeaps.bind(
            getString(R.string.stat_leaps),
            getString(R.string.fmt_count, state.leaps),
            ContextCompat.getColor(context, R.color.text_primary)
        )
        current.resultStatTokens.bind(
            getString(R.string.stat_tokens),
            getString(R.string.fmt_count, state.balance),
            ContextCompat.getColor(context, R.color.ember_500)
        )
        current.resultStatBest.bind(
            getString(R.string.stat_best),
            getString(R.string.fmt_metres, state.bestAltitude),
            ContextCompat.getColor(context, R.color.magma_500)
        )
        current.resultStatLeaps.setVisibleIfNonZero(state.leaps)
        current.resultStatTokens.setVisibleIfNonZero(state.balance)
        current.resultStatBest.setVisibleIfNonZero(state.bestAltitude)
        var shown = 0
        if (state.leaps > 0) {
            shown += 1
        }
        if (state.balance > 0) {
            shown += 1
        }
        if (state.bestAltitude > 0) {
            shown += 1
        }
        current.resultStatRow.visibility = if (shown >= 2) View.VISIBLE else View.GONE
        if (state.newBest) {
            current.resultRibbon.visibility = View.VISIBLE
            startRibbonPulse(current)
        } else {
            current.resultRibbon.visibility = View.GONE
        }
    }

    private fun startCountUp(target: Int) {
        try {
            countUp?.cancel()
            val animator = ValueAnimator.ofInt(0, target)
            animator.duration = 700L
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { value ->
                try {
                    val safe = binding ?: return@addUpdateListener
                    val raw = value.animatedValue
                    if (raw is Int) {
                        safe.resultAltitude.text = getString(R.string.fmt_count, raw)
                    }
                } catch (e: Exception) {
                }
            }
            countUp = animator
            animator.start()
        } catch (e: Exception) {
        }
    }

    private fun startRibbonPulse(current: FragmentGameOverBinding) {
        try {
            if (ribbonPulse != null) {
                return
            }
            val animator = ObjectAnimator.ofFloat(current.resultRibbon, View.ALPHA, 0.6f, 1f)
            animator.duration = 900L
            animator.repeatCount = 5
            animator.repeatMode = ValueAnimator.REVERSE
            ribbonPulse = animator
            animator.start()
        } catch (e: Exception) {
        }
    }

    private fun playEntry(current: FragmentGameOverBinding) {
        try {
            current.resultHero.alpha = 0f
            current.resultHero.scaleX = 0.9f
            current.resultHero.scaleY = 0.9f
            current.resultHero.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(360L)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
            current.resultStatLeaps.alpha = 0f
            current.resultStatTokens.alpha = 0f
            current.resultStatBest.alpha = 0f
            current.resultStatLeaps.animate().alpha(1f).setStartDelay(120L).setDuration(240L).start()
            current.resultStatTokens.animate().alpha(1f).setStartDelay(190L).setDuration(240L).start()
            current.resultStatBest.animate().alpha(1f).setStartDelay(260L).setDuration(240L).start()
        } catch (e: Exception) {
        }
    }

    override fun onDestroyView() {
        try {
            countUp?.cancel()
            ribbonPulse?.cancel()
            val current = binding
            current?.resultHero?.animate()?.cancel()
            current?.resultStatLeaps?.animate()?.cancel()
            current?.resultStatTokens?.animate()?.cancel()
            current?.resultStatBest?.animate()?.cancel()
        } catch (e: Exception) {
        }
        countUp = null
        ribbonPulse = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_ALTITUDE = "arg_altitude"
        private const val ARG_TOKENS = "arg_tokens"
        private const val ARG_LEAPS = "arg_leaps"
        private const val ARG_NEW_BEST = "arg_new_best"

        fun newInstance(
            altitude: Int,
            tokens: Int,
            leaps: Int,
            newBest: Boolean
        ): GameOverFragment {
            val fragment = GameOverFragment()
            val args = Bundle()
            args.putInt(ARG_ALTITUDE, altitude)
            args.putInt(ARG_TOKENS, tokens)
            args.putInt(ARG_LEAPS, leaps)
            args.putBoolean(ARG_NEW_BEST, newBest)
            fragment.arguments = args
            return fragment
        }
    }
}
