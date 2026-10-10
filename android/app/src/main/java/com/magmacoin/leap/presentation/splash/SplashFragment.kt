package com.magmacoin.leap.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.magmacoin.leap.core.navigation.Navigator
import com.magmacoin.leap.databinding.FragmentSplashBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private var animator: SplashAnimator? = null
    private var navigateRunnable: Runnable? = null
    private var navigated = false

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = FragmentSplashBinding.inflate(inflater, container, false)
        binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val current = binding ?: return
        val created = SplashAnimator(current)
        animator = created
        created.prepare()
        created.playEntry()
        created.startLoops()
        viewModel.ready
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { ready ->
                if (ready) {
                    goToMenu()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun goToMenu() {
        if (navigated) {
            return
        }
        navigated = true
        val root = binding?.root ?: return
        val task = Runnable {
            try {
                if (!isAdded || view == null) {
                    return@Runnable
                }
                viewModel.consume()
                Navigator.toMenu(parentFragmentManager)
            } catch (e: Exception) {
            }
        }
        navigateRunnable = task
        root.post(task)
    }

    override fun onDestroyView() {
        try {
            val root = binding?.root
            navigateRunnable?.let { task -> root?.removeCallbacks(task) }
            animator?.cancel()
        } catch (e: Exception) {
        }
        navigateRunnable = null
        animator = null
        binding = null
        super.onDestroyView()
    }
}
