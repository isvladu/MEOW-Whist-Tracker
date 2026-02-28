package com.isvladu.meowwhisttracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.isvladu.meowwhisttracker.R
import com.isvladu.meowwhisttracker.databinding.ItemTricksBinding
import com.isvladu.meowwhisttracker.model.Round

class TricksAdapter(
    private val round: Round,
    private val playerNames: List<String>,
    private val onTricksChanged: (total: Int) -> Unit
) : RecyclerView.Adapter<TricksAdapter.VH>() {

    private val taken = IntArray(playerNames.size) { 0 }

    inner class VH(val binding: ItemTricksBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTricksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val pr = round.playerRounds[position]
        val maxTricks = round.cardCount

        holder.binding.tvPlayerName.text = playerNames[position]
        holder.binding.tvBidLabel.text = "Licitație: ${pr.bid}"
        holder.binding.tvTricksValue.text = taken[position].toString()
        updateHitMiss(holder, position)

        holder.binding.btnTricksMinus.setOnClickListener {
            if (taken[position] > 0) {
                taken[position]--
                holder.binding.tvTricksValue.text = taken[position].toString()
                updateHitMiss(holder, position)
                onTricksChanged(taken.sum())
            }
        }

        holder.binding.btnTricksPlus.setOnClickListener {
            if (taken[position] < maxTricks) {
                taken[position]++
                holder.binding.tvTricksValue.text = taken[position].toString()
                updateHitMiss(holder, position)
                onTricksChanged(taken.sum())
            }
        }
    }

    private fun updateHitMiss(holder: VH, position: Int) {
        val pr = round.playerRounds[position]
        when {
            taken[position] == pr.bid -> {
                holder.binding.tvHitMiss.text = "✓"
                holder.binding.tvHitMiss.setTextColor(
                    ContextCompat.getColor(holder.binding.root.context, R.color.success)
                )
            }
            else -> {
                holder.binding.tvHitMiss.text = "✗"
                holder.binding.tvHitMiss.setTextColor(
                    ContextCompat.getColor(holder.binding.root.context, R.color.error_red)
                )
            }
        }
    }

    override fun getItemCount(): Int = playerNames.size

    fun getTaken(): List<Int> = taken.toList()

    fun getTotal(): Int = taken.sum()
}
