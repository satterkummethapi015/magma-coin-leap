package com.magmacoin.leap.domain.model

sealed class Cell {

    object Rock : Cell()

    data class Coin(val value: Int) : Cell()

    object Vent : Cell()
}
