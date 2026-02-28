package com.isvladu.meowwhisttracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isvladu.meowwhisttracker.adapter.PreviousBid
import com.isvladu.meowwhisttracker.adapter.PreviousBidAdapter
import com.isvladu.meowwhisttracker.adapter.ScoreboardAdapter
import com.isvladu.meowwhisttracker.adapter.TricksAdapter
import com.isvladu.meowwhisttracker.databinding.ActivityGameBinding
import com.isvladu.meowwhisttracker.model.GamePhase
import com.isvladu.meowwhisttracker.model.GameState
import com.isvladu.meowwhisttracker.model.Round

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private lateinit var viewModel: GameViewModel

    private var tricksAdapter: TricksAdapter? = null
    private lateinit var scoreboardAdapter: ScoreboardAdapter
    private lateinit var roundResultAdapter: ScoreboardAdapter

    private var scoreboardVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[GameViewModel::class.java]

        if (viewModel.gameState.value == null) {
            if (!viewModel.restoreGame()) {
                finish()
                return
            }
        }

        scoreboardAdapter = ScoreboardAdapter(emptyList())
        binding.rvScoreboard.apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = scoreboardAdapter
        }

        roundResultAdapter = ScoreboardAdapter(emptyList())
        binding.rvRoundResult.apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = roundResultAdapter
        }

        binding.btnBackToMenu.setOnClickListener {
            finish()
        }

        binding.btnToggleScoreboard.setOnClickListener {
            scoreboardVisible = !scoreboardVisible
            binding.layoutScoreboard.visibility = if (scoreboardVisible) View.VISIBLE else View.GONE
            binding.btnToggleScoreboard.text = if (scoreboardVisible) "▼ Scoreboard" else "▲ Scoreboard"
        }

        binding.btnDetailedScoreboard.setOnClickListener {
            startActivity(Intent(this, DetailedScoreboardActivity::class.java))
        }

        binding.btnNextRound.setOnClickListener {
            binding.layoutRoundResult.visibility = View.GONE
            viewModel.nextRound()
        }

        viewModel.gameState.observe(this) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: GameState) {
        val round = state.currentRound
        val totalRounds = state.rounds.size
        val playerNames = state.players.map { it.name }

        binding.tvRoundLabel.text = "Runda ${state.currentRoundIndex + 1}/$totalRounds"
        binding.tvCardsLabel.text = "${round.cardCount} ${if (round.cardCount == 1) "carte" else "cărți"}"
        binding.tvTrumpLabel.text = "Atu: ${round.trump.symbol} ${round.trump.label}"

        val sbEntries = ScoreboardAdapter.buildEntries(state.players, null)
        scoreboardAdapter.update(sbEntries)

        when (state.phase) {
            GamePhase.BIDDING -> showBidding(state, round, playerNames)
            GamePhase.TRICKS -> showTricks(state, round, playerNames)
            GamePhase.ROUND_RESULT -> showRoundResult(state, round, playerNames)
            GamePhase.DONE -> {
                startActivity(Intent(this, ResultsActivity::class.java))
                finish()
            }
        }
    }

    private fun showBidding(state: GameState, round: Round, playerNames: List<String>) {
        binding.tvPhaseTitle.text = getString(R.string.bidding_title)
        binding.ivCatMood.setImageResource(R.drawable.ic_cat_thinking)
        binding.viewFlipper.displayedChild = 0

        val currentIdx = viewModel.getCurrentBidderPlayerIndex(state)
        val forbidden = viewModel.getForbiddenBid(state)
        val order = viewModel.getBiddingOrder(round, state.players.size)

        binding.tvCurrentBidderName.text = playerNames[currentIdx]
        binding.chipBidderIsDealer.visibility =
            if (currentIdx == round.dealerIndex) View.VISIBLE else View.GONE
        binding.tvBidForbidden.apply {
            visibility = if (forbidden != null) View.VISIBLE else View.GONE
            text = if (forbidden != null) "Nu poți licita $forbidden" else ""
        }

        val previousBids = (0 until state.currentBidderPosition).map { pos ->
            val idx = order[pos]
            PreviousBid(playerNames[idx], round.playerRounds[idx].bid)
        }
        val showPrev = previousBids.isNotEmpty()
        binding.tvPreviousBidsLabel.visibility = if (showPrev) View.VISIBLE else View.GONE
        binding.rvPreviousBids.visibility = if (showPrev) View.VISIBLE else View.GONE
        binding.rvPreviousBids.apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = PreviousBidAdapter(previousBids)
        }

        val maxBid = round.cardCount
        var currentBid = if (forbidden == 0) 1 else 0
        binding.tvBidValue.text = "$currentBid"

        fun next(v: Int, dir: Int): Int? {
            var nv = v + dir
            if (nv < 0 || nv > maxBid) return null
            if (forbidden != null && nv == forbidden) nv += dir
            if (nv < 0 || nv > maxBid) return null
            return nv
        }

        fun refresh() {
            binding.btnBidMinus.isEnabled = next(currentBid, -1) != null
            binding.btnBidPlus.isEnabled = next(currentBid, +1) != null
        }
        refresh()

        binding.btnBidMinus.setOnClickListener {
            next(currentBid, -1)?.let {
                currentBid = it
                refresh()
                binding.tvBidValue.text = "$it"
            }
        }
        binding.btnBidPlus.setOnClickListener {
            next(currentBid, +1)?.let {
                currentBid = it
                refresh()
                binding.tvBidValue.text = "$it"
            }
        }
        binding.btnSubmitBid.setOnClickListener {
            viewModel.submitBid(currentBid)
        }
    }

    private fun showTricks(state: GameState, round: Round, playerNames: List<String>) {
        binding.tvPhaseTitle.text = getString(R.string.tricks_title)
        binding.ivCatMood.setImageResource(R.drawable.ic_cat_normal)
        binding.viewFlipper.displayedChild = 1

        tricksAdapter = TricksAdapter(round, playerNames) { total ->
            updateTricksValidation(total, round.cardCount)
        }
        binding.rvTricks.apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = tricksAdapter
        }
        updateTricksValidation(0, round.cardCount)

        binding.btnConfirmTricks.setOnClickListener {
            val adapter = tricksAdapter ?: return@setOnClickListener
            if (adapter.getTotal() != round.cardCount) {
                Toast.makeText(
                    this,
                    "Totalul levatelor trebuie să fie ${round.cardCount}!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.submitTricksForAllPlayers(adapter.getTaken())
        }
    }

    private fun updateTricksValidation(total: Int, cardCount: Int) {
        binding.tvTricksValidation.text = "Total levate: $total / $cardCount"
        binding.tvTricksValidation.setTextColor(
            if (total == cardCount) getColor(R.color.success) else getColor(R.color.error_red)
        )
    }

    private fun showRoundResult(state: GameState, round: Round, playerNames: List<String>) {
        binding.layoutRoundResult.visibility = View.VISIBLE

        val hits = round.playerRounds.count { it.bid == it.taken }
        binding.ivCatMood.setImageResource(
            if (hits > playerNames.size / 2) R.drawable.ic_cat_happy else R.drawable.ic_cat_sad
        )

        val entries = ScoreboardAdapter.buildEntries(state.players, round)
        roundResultAdapter.update(entries)

        // Premiere announcement
        if (state.premiuEnabled && round.cardCount != 1) {
            val winners = state.players.filter { it.consecutiveHits > 0 && it.consecutiveHits % 5 == 0 }
            if (winners.isNotEmpty()) {
                val names = winners.joinToString(", ") { it.name }
                val verb = if (winners.size == 1) "a" else "au"
                binding.tvPremiuAnnouncement.text = "🎉 $names $verb câștigat premiu!"
                binding.tvPremiuAnnouncement.visibility = View.VISIBLE
            } else {
                binding.tvPremiuAnnouncement.visibility = View.GONE
            }
        } else {
            binding.tvPremiuAnnouncement.visibility = View.GONE
        }

        binding.btnNextRound.text = if (state.isLastRound) "Finalizează Jocul" else getString(R.string.next_round)
    }
}
