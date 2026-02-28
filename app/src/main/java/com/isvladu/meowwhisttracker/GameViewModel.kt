package com.isvladu.meowwhisttracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.isvladu.meowwhisttracker.model.*
import kotlin.math.abs
import kotlin.random.Random

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = GameRepository(app)
    val gameState = MutableLiveData<GameState>()

    fun startGame(players: List<String>, premiuEnabled: Boolean = false) {
        val playerList = players.mapIndexed { i, name -> Player(id = i, name = name) }
        val sequence = generateRoundSequence(players.size)
        val rounds = sequence.mapIndexed { idx, cardCount ->
            val dealerIndex = idx % players.size
            Round(
                index = idx,
                cardCount = cardCount,
                trump = Suit.entries.random(Random),
                dealerIndex = dealerIndex,
                playerRounds = playerList.map { PlayerRound(playerId = it.id) }
            )
        }
        val state = GameState(players = playerList, rounds = rounds, premiuEnabled = premiuEnabled)
        gameState.value = state
        repo.saveGame(state)
    }

    fun getBiddingOrder(round: Round, playerCount: Int): List<Int> =
        (1..playerCount).map { (round.dealerIndex + it) % playerCount }

    fun getCurrentBidderPlayerIndex(state: GameState): Int =
        getBiddingOrder(state.currentRound, state.players.size)[state.currentBidderPosition]

    fun getForbiddenBid(state: GameState): Int? {
        val isDealer = state.currentBidderPosition == state.players.size - 1
        if (!isDealer) return null
        val sumSoFar = state.currentRound.playerRounds.sumOf { if (it.bid >= 0) it.bid else 0 }
        val forbidden = state.currentRound.cardCount - sumSoFar
        return if (forbidden in 0..state.currentRound.cardCount) forbidden else null
    }

    fun submitBid(bid: Int) {
        val state = gameState.value ?: return
        val round = state.currentRound
        val currentPlayerIndex = getCurrentBidderPlayerIndex(state)
        val playerRounds = round.playerRounds.toMutableList()
        playerRounds[currentPlayerIndex] = playerRounds[currentPlayerIndex].copy(bid = bid)
        val updatedRound = round.copy(playerRounds = playerRounds)
        val updatedRounds = state.rounds.toMutableList()
        updatedRounds[state.currentRoundIndex] = updatedRound

        val nextPos = state.currentBidderPosition + 1
        val allDone = nextPos >= state.players.size
        val newState = state.copy(
            rounds = updatedRounds,
            phase = if (allDone) GamePhase.TRICKS else GamePhase.BIDDING,
            currentBidderPosition = if (allDone) 0 else nextPos
        )
        gameState.value = newState
        repo.saveGame(newState)
    }

    fun submitTricksForAllPlayers(takenList: List<Int>) {
        val state = gameState.value ?: return
        val round = state.currentRound
        val isOneCardRound = round.cardCount == 1

        val updatedPlayerRounds = round.playerRounds.mapIndexed { i, pr ->
            val taken = takenList[i]
            val score = calculateScore(pr.bid, taken)
            pr.copy(taken = taken, score = score)
        }
        val updatedRound = round.copy(playerRounds = updatedPlayerRounds)
        val updatedRounds = state.rounds.toMutableList()
        updatedRounds[state.currentRoundIndex] = updatedRound

        val updatedPlayers = state.players.map { player ->
            val pr = updatedPlayerRounds.first { it.playerId == player.id }
            val newConsecHits = if (state.premiuEnabled && !isOneCardRound) {
                if (pr.bid == pr.taken) player.consecutiveHits + 1 else 0
            } else player.consecutiveHits
            player.copy(totalScore = player.totalScore + pr.score, consecutiveHits = newConsecHits)
        }

        val newState = state.copy(
            players = updatedPlayers,
            rounds = updatedRounds,
            phase = GamePhase.ROUND_RESULT
        )
        gameState.value = newState
        repo.saveGame(newState)
    }

    fun nextRound() {
        val state = gameState.value ?: return
        if (state.isLastRound) {
            val newState = state.copy(phase = GamePhase.DONE)
            gameState.value = newState
            repo.saveToHistory(newState)
            repo.saveGame(newState)
        } else {
            val newState = state.copy(
                currentRoundIndex = state.currentRoundIndex + 1,
                phase = GamePhase.BIDDING,
                currentBidderPosition = 0
            )
            gameState.value = newState
            repo.saveGame(newState)
        }
    }

    fun restoreGame(): Boolean {
        val saved = repo.loadGame() ?: return false
        gameState.value = saved
        return true
    }

    fun clearGame() {
        repo.clearGame()
    }

    private fun calculateScore(bid: Int, taken: Int): Int {
        return if (bid == taken) 5 + bid else -(abs(bid - taken))
    }
}
