package com.magmacoin.leap.presentation.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.magmacoin.leap.R
import com.magmacoin.leap.databinding.ItemGearBinding
import com.magmacoin.leap.domain.model.GearItem

class GearAdapter(private val onPurchase: (GearItem) -> Unit) :
    RecyclerView.Adapter<GearAdapter.GearViewHolder>() {

    private val items = ArrayList<GearItem>()
    private var balance = 0

    fun submit(newItems: List<GearItem>, newBalance: Int) {
        items.clear()
        items.addAll(newItems)
        balance = newBalance
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GearViewHolder {
        val binding = ItemGearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GearViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GearViewHolder, position: Int) {
        holder.bind(items[position], balance, onPurchase)
    }

    override fun getItemCount(): Int = items.size

    class GearViewHolder(private val binding: ItemGearBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GearItem, balance: Int, onPurchase: (GearItem) -> Unit) {
            val context = binding.root.context
            val name = context.getString(item.nameRes)
            binding.gearName.text = name
            binding.gearEffect.text = context.getString(item.effectRes)
            binding.gearCost.text = context.getString(R.string.fmt_count, item.cost)
            if (item.owned) {
                binding.gearCostChip.visibility = View.GONE
                binding.gearOwned.visibility = View.VISIBLE
                binding.root.alpha = 1f
                binding.root.isEnabled = false
                binding.root.setStrokeColor(ContextCompat.getColor(context, R.color.ember_500))
                binding.root.setOnClickListener(null)
                binding.root.isClickable = false
            } else {
                val affordable = balance >= item.cost
                binding.gearCostChip.visibility = View.VISIBLE
                binding.gearOwned.visibility = View.GONE
                binding.root.alpha = if (affordable) 1f else 0.5f
                binding.root.isEnabled = affordable
                binding.root.isClickable = affordable
                binding.root.setStrokeColor(
                    ContextCompat.getColor(
                        context,
                        if (affordable) R.color.gold_stroke else R.color.text_muted
                    )
                )
                binding.root.setOnClickListener {
                    try {
                        if (affordable) {
                            onPurchase(item)
                        }
                    } catch (e: Exception) {
                    }
                }
            }
            binding.root.contentDescription = name
        }
    }
}
