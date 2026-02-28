package com.isvladu.meowwhisttracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isvladu.meowwhisttracker.HistoryEntry
import com.isvladu.meowwhisttracker.databinding.ItemHistoryGameBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val items: List<HistoryEntry>,
    private val onClick: (HistoryEntry) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    inner class VH(val binding: ItemHistoryGameBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHistoryGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = items[position]
        val state = entry.state
        holder.binding.tvHistoryDate.text = dateFormat.format(Date(entry.timestamp))
        holder.binding.tvHistoryPlayers.text = state.players.joinToString(" · ") { it.name }
        val maxScore = state.players.maxOf { it.totalScore }
        val winners = state.players.filter { it.totalScore == maxScore }
        holder.binding.tvHistoryWinner.text = winners.joinToString(", ") { it.name }
        holder.itemView.setOnClickListener { onClick(entry) }
    }

    override fun getItemCount(): Int = items.size
}
