package com.magmacoin.leap.presentation.gameover

data class GameOverUiState(
    val altitude: Int = 0,
    val tokens: Int = 0,
    val leaps: Int = 0,
    val bestAltitude: Int = 0,
    val balance: Int = 0,
    val newBest: Boolean = false,
    val summit: Boolean = false
)
