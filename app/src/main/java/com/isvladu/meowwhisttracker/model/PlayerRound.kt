package com.isvladu.meowwhisttracker.model

data class PlayerRound(
    val playerId: Int,
    var bid: Int = -1,
    var taken: Int = -1,
    var score: Int = 0
)
