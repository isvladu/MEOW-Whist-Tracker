package com.isvladu.meowwhisttracker.model

data class Player(
    val id: Int,
    var name: String,
    var totalScore: Int = 0,
    var consecutiveHits: Int = 0
)
