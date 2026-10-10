package com.magmacoin.leap.domain.model

data class LeapOutcome(
    val survived: Boolean,
    val tokens: Int,
    val shielded: Boolean
)
