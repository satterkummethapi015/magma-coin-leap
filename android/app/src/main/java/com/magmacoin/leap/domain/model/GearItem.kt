package com.magmacoin.leap.domain.model

data class GearItem(
    val id: String,
    val nameRes: Int,
    val effectRes: Int,
    val cost: Int,
    val owned: Boolean = false
)
