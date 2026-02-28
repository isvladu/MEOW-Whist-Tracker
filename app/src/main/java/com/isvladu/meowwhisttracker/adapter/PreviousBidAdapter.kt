package com.isvladu.meowwhisttracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isvladu.meowwhisttracker.databinding.ItemPreviousBidBinding

data class PreviousBid(val playerName: String, val bid: Int)

class PreviousBidAdapter(private val items: List<PreviousBid>) :
    RecyclerView.Adapter<PreviousBidAdapter.VH>() {

    inner class VH(val binding: ItemPreviousBidBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPreviousBidBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvPrevPlayerName.text = item.playerName
        holder.binding.tvPrevBidValue.text = "${item.bid}"
    }

    override fun getItemCount(): Int = items.size
}
