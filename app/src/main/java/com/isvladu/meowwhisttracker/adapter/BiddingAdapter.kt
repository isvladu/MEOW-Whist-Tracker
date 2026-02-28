package com.isvladu.meowwhisttracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isvladu.meowwhisttracker.databinding.ItemBiddingBinding
import com.isvladu.meowwhisttracker.model.Round

class BiddingAdapter(
    private val round: Round,
    private val playerNames: List<String>,
    private val forbiddenBid: Int?
) : RecyclerView.Adapter<BiddingAdapter.VH>() {

    // Ordered bidding: starts from player AFTER dealer, dealer is last
    private val biddingOrder: List<Int> = run {
        val n = playerNames.size
        val dealer = round.dealerIndex
        (1..n).map { (dealer + it) % n }
    }

    private val bids = IntArray(playerNames.size) { 0 }

    inner class VH(val binding: ItemBiddingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBiddingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val playerIndex = biddingOrder[position]
        val isDealer = playerIndex == round.dealerIndex
        val maxBid = round.cardCount

        holder.binding.tvPlayerName.text = playerNames[playerIndex]
        holder.binding.chipDealer.visibility = if (isDealer) View.VISIBLE else View.GONE
        holder.binding.tvBidValue.text = bids[playerIndex].toString()

        if (isDealer && forbiddenBid != null) {
            holder.binding.tvForbiddenNote.visibility = View.VISIBLE
            holder.binding.tvForbiddenNote.text = "Nu poți licita $forbiddenBid"
        } else {
            holder.binding.tvForbiddenNote.visibility = View.GONE
        }

        holder.binding.btnBidMinus.setOnClickListener {
            if (bids[playerIndex] > 0) {
                bids[playerIndex]--
                holder.binding.tvBidValue.text = bids[playerIndex].toString()
            }
        }

        holder.binding.btnBidPlus.setOnClickListener {
            if (bids[playerIndex] < maxBid) {
                val next = bids[playerIndex] + 1
                if (isDealer && forbiddenBid != null && next == forbiddenBid) {
                    if (next + 1 <= maxBid) bids[playerIndex] = next + 1
                } else {
                    bids[playerIndex] = next
                }
                // Skip forbidden for dealer
                if (isDealer && forbiddenBid != null && bids[playerIndex] == forbiddenBid) {
                    bids[playerIndex] = if (bids[playerIndex] + 1 <= maxBid) bids[playerIndex] + 1 else bids[playerIndex] - 1
                }
                holder.binding.tvBidValue.text = bids[playerIndex].toString()
            }
        }
    }

    override fun getItemCount(): Int = playerNames.size

    /**
     * Returns bids in player index order (not bidding order).
     */
    fun getBids(): List<Int> = bids.toList()

    /**
     * Validates that dealer's bid is not the forbidden one.
     */
    fun isValid(): Boolean {
        val dealerBid = bids[round.dealerIndex]
        return forbiddenBid == null || dealerBid != forbiddenBid
    }
}
