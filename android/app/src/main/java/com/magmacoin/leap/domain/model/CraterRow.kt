package com.magmacoin.leap.domain.model

data class CraterRow(
    val index: Int,
    val cells: List<Cell>,
    val ventLane: Int,
    val drifting: Boolean,
    val branch: Boolean,
    val driftTarget: Int
)
