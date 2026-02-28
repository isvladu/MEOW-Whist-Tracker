package com.isvladu.meowwhisttracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.isvladu.meowwhisttracker.R
import com.isvladu.meowwhisttracker.databinding.ItemScoreboardBinding
import com.isvladu.meowwhisttracker.model.Player
import com.isvladu.meowwhisttracker.model.Round

data class ScoreboardEntry(
    val rank: Int,
    val name: String,
    val delta: Int,
    val total: Int
)

class ScoreboardAdapter(private var entries: List<ScoreboardEntry>) :
    RecyclerView.Adapter<ScoreboardAdapter.VH>() {

    inner class VH(val binding: ItemScoreboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemScoreboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = entries[position]
        val ctx = holder.binding.root.context

        holder.binding.tvRank.text = entry.rank.toString()
        holder.binding.tvPlayerName.text = entry.name
        holder.binding.tvTotalScore.text = entry.total.toString()

        if (entry.delta >= 0) {
            holder.binding.tvScoreDelta.text = "+${entry.delta}"
            holder.binding.tvScoreDelta.setTextColor(
                ContextCompat.getColor(ctx, R.color.success)
            )
        } else {
            holder.binding.tvScoreDelta.text = entry.delta.toString()
            holder.binding.tvScoreDelta.setTextColor(
                ContextCompat.getColor(ctx, R.color.error_red)
            )
        }
    }

    override fun getItemCount(): Int = entries.size

    fun update(newEntries: List<ScoreboardEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    companion object {
        fun buildEntries(players: List<Player>, currentRound: Round?): List<ScoreboardEntry> {
            val sorted = players.sortedByDescending { it.totalScore }
            return sorted.mapIndexed { index, player ->
                val delta = currentRound?.playerRounds
                    ?.firstOrNull { it.playerId == player.id }
                    ?.score ?: 0
                ScoreboardEntry(
                    rank = index + 1,
                    name = player.name,
                    delta = delta,
                    total = player.totalScore
                )
            }
        }
    }
}
