package com.isvladu.meowwhisttracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isvladu.meowwhisttracker.databinding.ItemPlayerSetupBinding

class PlayerSetupAdapter(private var count: Int) :
    RecyclerView.Adapter<PlayerSetupAdapter.VH>() {

    private val names = Array(6) { "" }

    inner class VH(val binding: ItemPlayerSetupBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPlayerSetupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.tvPlayerNumber.text = (position + 1).toString()
        holder.binding.etPlayerName.hint = "Jucător ${position + 1}"
        holder.binding.etPlayerName.setText(names[position])
        holder.binding.etPlayerName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                names[holder.bindingAdapterPosition] = holder.binding.etPlayerName.text?.toString() ?: ""
            }
        }
    }

    override fun getItemCount(): Int = count

    fun setCount(newCount: Int) {
        // Save current texts before notifying
        count = newCount
        notifyDataSetChanged()
    }

    fun getNames(): List<String> {
        // Flush any focused field values not yet saved
        return (0 until count).map { i ->
            names[i].ifBlank { "Jucător ${i + 1}" }
        }
    }
}
