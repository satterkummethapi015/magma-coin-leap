package com.magmacoin.leap.presentation.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.magmacoin.leap.R
import com.magmacoin.leap.core.di.ServiceLocator
import com.magmacoin.leap.databinding.DialogGearLockerBinding
import com.magmacoin.leap.presentation.menu.MenuFragment

class GearLockerDialog : DialogFragment() {

    private var binding: DialogGearLockerBinding? = null
    private var adapter: GearAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MagmaCoinLeap_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = DialogGearLockerBinding.inflate(inflater, container, false)
        binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val current = binding ?: return
        val created = GearAdapter { item -> purchase(item.id) }
        adapter = created
        current.gearList.layoutManager = LinearLayoutManager(requireContext())
        current.gearList.adapter = created
        current.btnGearClose.setOnClickListener { dismissAllowingStateLoss() }
        current.btnGearRetry.setOnClickListener { load() }
        load()
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

    private fun load() {
        val current = binding ?: return
        try {
            val repository = ServiceLocator.gearRepository(requireContext())
            val items = repository.catalogue()
            val balance = repository.balance()
            if (items.isEmpty()) {
                showError(current)
                return
            }
            current.gearList.visibility = View.VISIBLE
            current.gearError.visibility = View.GONE
            current.gearTokenBadge.setCount(balance)
            adapter?.submit(items, balance)
        } catch (e: Exception) {
            showError(current)
        }
    }

    private fun showError(current: DialogGearLockerBinding) {
        current.gearList.visibility = View.GONE
        current.gearError.visibility = View.VISIBLE
    }

    private fun purchase(id: String) {
        try {
            val bought = ServiceLocator.purchaseGearUseCase(requireContext()).invoke(id)
            if (bought) {
                binding?.gearTokenBadge?.pulse()
            }
            load()
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
            binding?.gearList?.adapter = null
            binding?.gearTokenBadge?.stop()
        } catch (e: Exception) {
        }
        adapter = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "gear_locker_dialog"
    }
}
