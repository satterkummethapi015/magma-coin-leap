package com.magmacoin.leap.presentation.menu

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
import com.magmacoin.leap.databinding.FragmentMenuBinding
import com.magmacoin.leap.presentation.dialog.GearLockerDialog
import com.magmacoin.leap.presentation.dialog.SettingsDialog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MenuFragment : Fragment() {

    private var binding: FragmentMenuBinding? = null
    private var breathing: Animator? = null

    private val viewModel: MenuViewModel by viewModels {
        ViewModelFactory { MenuViewModel(ServiceLocator.progressRepository(requireContext())) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = FragmentMenuBinding.inflate(inflater, container, false)
        binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val current = binding ?: return
        current.btnPlay.setOnClickListener {
            try {
                Navigator.toGame(parentFragmentManager)
            } catch (e: Exception) {
            }
        }
        current.btnGear.setOnClickListener {
            try {
                GearLockerDialog().show(childFragmentManager, GearLockerDialog.TAG)
            } catch (e: Exception) {
            }
        }
        current.btnSettings.setOnClickListener {
            try {
                SettingsDialog().show(childFragmentManager, SettingsDialog.TAG)
            } catch (e: Exception) {
            }
        }
        childFragmentManager.setFragmentResultListener(RESULT_KEY, viewLifecycleOwner) { _, _ ->
            viewModel.refresh()
        }
        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> render(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        playEntry(current)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun render(state: MenuUiState) {
        val current = binding ?: return
        val host = context ?: return
        current.menuTokenBadge.setCount(state.tokens)
        if (state.bestAltitude > 0) {
            current.menuBestRow.visibility = View.VISIBLE
            current.menuBestText.text = getString(R.string.fmt_best_metres, state.bestAltitude)
        } else {
            current.menuBestRow.visibility = View.GONE
        }
        val showStats = state.bestAltitude > 0 && state.runs > 0
        if (showStats) {
            current.menuStatRow.visibility = View.VISIBLE
            current.menuFreshHint.visibility = View.GONE
            current.menuStatBest.bind(
                getString(R.string.stat_best_altitude),
                getString(R.string.fmt_metres, state.bestAltitude),
                ContextCompat.getColor(host, R.color.ember_500)
            )
            current.menuStatRuns.bind(
                getString(R.string.stat_runs),
                getString(R.string.fmt_count, state.runs),
                ContextCompat.getColor(host, R.color.magma_500)
            )
        } else {
            current.menuStatRow.visibility = View.GONE
            current.menuFreshHint.visibility = View.VISIBLE
        }
    }

    private fun playEntry(current: FragmentMenuBinding) {
        try {
            current.menuSheet.translationY = SHEET_OFFSET
            current.menuSheet.alpha = 0f
            current.menuSheet.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(380L)
                .setInterpolator(DecelerateInterpolator())
                .start()

            current.menuCtaWrap.scaleX = 0.94f
            current.menuCtaWrap.scaleY = 0.94f
            current.menuCtaWrap.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(120L)
                .setDuration(300L)
                .setInterpolator(OvershootInterpolator(1.4f))
                .start()

            current.menuStatBest.alpha = 0f
            current.menuStatRuns.alpha = 0f
            current.menuStatBest.animate().alpha(1f).setStartDelay(120L).setDuration(260L).start()
            current.menuStatRuns.animate().alpha(1f).setStartDelay(180L).setDuration(260L).start()

            val breathe = ObjectAnimator.ofFloat(
                current.menuExplorer,
                View.TRANSLATION_Y,
                0f,
                EXPLORER_RISE
            )
            breathe.duration = 2200L
            breathe.repeatCount = ValueAnimator.INFINITE
            breathe.repeatMode = ValueAnimator.REVERSE
            breathe.interpolator = DecelerateInterpolator()
            breathing = breathe
            breathe.start()
        } catch (e: Exception) {
        }
    }

    override fun onDestroyView() {
        try {
            breathing?.cancel()
            val current = binding
            current?.menuSheet?.animate()?.cancel()
            current?.menuCtaWrap?.animate()?.cancel()
            current?.menuStatBest?.animate()?.cancel()
            current?.menuStatRuns?.animate()?.cancel()
            current?.menuExplorer?.animate()?.cancel()
            current?.menuTokenBadge?.stop()
        } catch (e: Exception) {
        }
        breathing = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val RESULT_KEY = "menu_refresh"
        private const val SHEET_OFFSET = 110f
        private const val EXPLORER_RISE = -18f
    }
}
