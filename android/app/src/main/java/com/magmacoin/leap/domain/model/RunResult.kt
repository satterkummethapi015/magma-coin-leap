package com.magmacoin.leap.domain.model

data class RunResult(
    val altitude: Int,
    val tokens: Int,
    val leaps: Int,
    val newBest: Boolean,
    val cause: EndCause
)
