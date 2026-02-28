package com.isvladu.meowwhisttracker.model

data class Round(
    val index: Int,
    val cardCount: Int,
    val trump: Suit,
    val dealerIndex: Int,
    val playerRounds: List<PlayerRound>
) {
    val isBiddingDone: Boolean
        get() = playerRounds.all { it.bid >= 0 }

    val isComplete: Boolean
        get() = playerRounds.all { it.taken >= 0 }
}
