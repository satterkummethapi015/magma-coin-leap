package com.magmacoin.leap.presentation.game

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.magmacoin.leap.R
import com.magmacoin.leap.core.config.GameConfig
import com.magmacoin.leap.core.di.ServiceLocator
import com.magmacoin.leap.core.di.ViewModelFactory
import com.magmacoin.leap.core.navigation.Navigator
import com.magmacoin.leap.databinding.FragmentGameBinding
import com.magmacoin.leap.domain.model.RunResult
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GameFragment : Fragment() {

    private var binding: FragmentGameBinding? = null
    private var hintRunnable: Runnable? = null
    private var resultRunnable: Runnable? = null
    private var navigated = false
    private var lastTokens = -1
    private var lastAltitude = -1
    private var lastLocked = true
    private var lastContrast = false

    private val viewModel: GameViewModel by viewModels {
        ViewModelFactory {
            val context = requireContext()
            GameViewModel(
                ServiceLocator.progressRepository(context),
                ServiceLocator.gearRepository(context),
                ServiceLocator.generateRowUseCase(),
                ServiceLocator.resolveLeapUseCase(),
                ServiceLocator.saveRunUseCase(context)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = FragmentGameBinding.inflate(inflater, container, false)
        binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val current = binding ?: return
        current.laneLeft.setLabel(getString(R.string.lane_left), 0)
        current.laneMid.setLabel(getString(R.string.lane_mid), 1)
        current.laneRight.setLabel(getString(R.string.lane_right), 2)
        current.laneLeft.setOnLeapListener { lane -> onLane(lane) }
        current.laneMid.setOnLeapListener { lane -> onLane(lane) }
        current.laneRight.setOnLeapListener { lane -> onLane(lane) }
        current.btnBack.setOnClickListener {
            try {
                Navigator.back(parentFragmentManager)
            } catch (e: Exception) {
            }
        }
        current.gameAltitudeValue.text = getString(R.string.fmt_metres, 0)
        current.gameTokenBadge.setCount(0)
        scheduleHintFade(current)
        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> render(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.result
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { result -> handleResult(result) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onLane(lane: Int) {
        val current = binding ?: return
        if (!viewModel.canLeap()) {
            return
        }
        viewModel.beginLeap()
        current.gameHint.animate().cancel()
        current.gameHint.alpha = 0f
        current.gameBoard.animateLeap(lane) {
            try {
                viewModel.completeLeap(lane)
            } catch (e: Exception) {
            }
        }
    }

    private fun scheduleHintFade(current: FragmentGameBinding) {
        val task = Runnable {
            try {
                val safe = binding
                if (safe == null || !isAdded || view == null) {
                    return@Runnable
                }
                safe.gameHint.animate().alpha(0f).setDuration(320L).start()
            } catch (e: Exception) {
            }
        }
        hintRunnable = task
        current.root.postDelayed(task, GameConfig.HINT_FADE_MS)
    }

    private fun render(state: GameUiState) {
        val current = binding ?: return
        if (state.altitude != lastAltitude) {
            lastAltitude = state.altitude
            current.gameAltitudeValue.text = getString(R.string.fmt_metres, state.altitude)
        }
        if (state.runTokens != lastTokens) {
            val first = lastTokens < 0
            lastTokens = state.runTokens
            current.gameTokenBadge.setCount(state.runTokens)
            if (!first) {
                current.gameTokenBadge.pulse()
            }
        }
        current.gameFuse.setProgress(state.fuseFraction)
        current.gameBoard.render(
            state.rows,
            state.explorerLane,
            state.branchLane,
            state.ventPulse,
            state.highContrast,
            state.finished
        )
        val locked = !state.lanesEnabled
        if (locked != lastLocked) {
            lastLocked = locked
            current.laneLeft.setLocked(locked)
            current.laneMid.setLocked(locked)
            current.laneRight.setLocked(locked)
        }
        if (state.highContrast != lastContrast && !locked) {
            lastContrast = state.highContrast
            current.laneLeft.setHighContrast(state.highContrast)
            current.laneMid.setHighContrast(state.highContrast)
            current.laneRight.setHighContrast(state.highContrast)
        }
    }

    private fun handleResult(result: RunResult?) {
        if (result == null || navigated) {
            return
        }
        navigated = true
        val current = binding ?: return
        try {
            current.gameFuse.pause()
            current.gameBoard.flashFail()
            current.gameFlash.animate().cancel()
            current.gameFlash.alpha = 0f
            current.gameFlash.animate().alpha(0.85f).setDuration(150L).start()
            if (viewModel.hapticEnabled) {
                current.root.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } catch (e: Exception) {
        }
        val task = Runnable {
            try {
                if (!isAdded || view == null) {
                    return@Runnable
                }
                val manager = parentFragmentManager
                Navigator.toResult(
                    manager,
                    result.altitude,
                    result.tokens,
                    result.leaps,
                    result.newBest
                )
            } catch (e: Exception) {
            }
        }
        resultRunnable = task
        current.root.postDelayed(task, GameConfig.RESULT_DELAY_MS)
    }

    override fun onDestroyView() {
        try {
            val current = binding
            val root = current?.root
            hintRunnable?.let { task -> root?.removeCallbacks(task) }
            resultRunnable?.let { task -> root?.removeCallbacks(task) }
            current?.gameBoard?.cancelAnimations()
            current?.gameHint?.animate()?.cancel()
            current?.gameFlash?.animate()?.cancel()
            current?.gameTokenBadge?.stop()
            current?.laneLeft?.detach()
            current?.laneMid?.detach()
            current?.laneRight?.detach()
        } catch (e: Exception) {
        }
        hintRunnable = null
        resultRunnable = null
        binding = null
        super.onDestroyView()
    }
}
