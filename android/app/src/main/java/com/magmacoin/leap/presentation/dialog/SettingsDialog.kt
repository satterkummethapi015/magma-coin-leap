package com.magmacoin.leap.presentation.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.magmacoin.leap.R
import com.magmacoin.leap.core.config.GameConfig
import com.magmacoin.leap.core.di.ServiceLocator
import com.magmacoin.leap.databinding.DialogSettingsBinding
import com.magmacoin.leap.domain.repository.ProgressRepository
import com.magmacoin.leap.presentation.menu.MenuFragment

class SettingsDialog : DialogFragment() {

    private var binding: DialogSettingsBinding? = null
    private var repository: ProgressRepository? = null
    private var resetRunnable: Runnable? = null
    private var resetArmed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MagmaCoinLeap_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = DialogSettingsBinding.inflate(inflater, container, false)
        binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val current = binding ?: return
        val store = ServiceLocator.progressRepository(requireContext())
        repository = store
        current.switchHaptic.isChecked = store.isHapticEnabled()
        current.switchVent.isChecked = store.isVentPulseEnabled()
        current.switchContrast.isChecked = store.isHighContrastEnabled()
        describe(current.switchHaptic)
        describe(current.switchVent)
        describe(current.switchContrast)
        current.switchHaptic.setOnCheckedChangeListener { button, checked ->
            store.setHapticEnabled(checked)
            describe(button)
        }
        current.switchVent.setOnCheckedChangeListener { button, checked ->
            store.setVentPulseEnabled(checked)
            describe(button)
        }
        current.switchContrast.setOnCheckedChangeListener { button, checked ->
            store.setHighContrastEnabled(checked)
            describe(button)
        }
        current.btnReset.setOnClickListener { onReset() }
        current.btnSettingsClose.setOnClickListener { dismissAllowingStateLoss() }
    }

    override fun onStart() {
        super.onStart()
        try {
            dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } catch (e: Exception) {
        }
    }

    private fun describe(button: CompoundButton) {
        val state = getString(if (button.isChecked) R.string.state_on else R.string.state_off)
        ViewCompat.setStateDescription(button, state)
    }

    private fun onReset() {
        val current = binding ?: return
        if (!resetArmed) {
            resetArmed = true
            current.btnReset.setText(R.string.settings_reset_confirm)
            val task = Runnable {
                try {
                    val safe = binding
                    if (safe == null || !isAdded) {
                        return@Runnable
                    }
                    resetArmed = false
                    safe.btnReset.setText(R.string.settings_reset)
                } catch (e: Exception) {
                }
            }
            resetRunnable = task
            current.root.postDelayed(task, GameConfig.RESET_CONFIRM_MS)
            return
        }
        resetArmed = false
        try {
            resetRunnable?.let { task -> current.root.removeCallbacks(task) }
            repository?.resetProgress()
            current.btnReset.setText(R.string.settings_reset)
            current.switchHaptic.isChecked = true
            current.switchVent.isChecked = true
            current.switchContrast.isChecked = false
        } catch (e: Exception) {
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        try {
            setFragmentResult(MenuFragment.RESULT_KEY, Bundle())
        } catch (e: Exception) {
        }
        super.onDismiss(dialog)
    }

    override fun onDestroyView() {
        try {
            resetRunnable?.let { task -> binding?.root?.removeCallbacks(task) }
        } catch (e: Exception) {
        }
        resetRunnable = null
        repository = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "settings_dialog"
    }
}
