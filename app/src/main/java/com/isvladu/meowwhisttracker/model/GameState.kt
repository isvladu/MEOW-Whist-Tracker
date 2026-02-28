package com.isvladu.meowwhisttracker.model

enum class GamePhase {
    BIDDING, TRICKS, ROUND_RESULT, DONE
}

data class GameState(
    val players: List<Player>,
    val rounds: List<Round>,
    var currentRoundIndex: Int = 0,
    var phase: GamePhase = GamePhase.BIDDING,
    val currentBidderPosition: Int = 0,
    val premiuEnabled: Boolean = false
) {
    val currentRound: Round get() = rounds[currentRoundIndex]
    val isLastRound: Boolean get() = currentRoundIndex >= rounds.size - 1
}

fun generateRoundSequence(playerCount: Int): List<Int> = buildList {
    repeat(playerCount) { add(1) }
    (2..7).forEach { add(it) }
    repeat(playerCount) { add(8) }
    (7 downTo 2).forEach { add(it) }
    repeat(playerCount) { add(1) }
}
