package com.magmacoin.leap.domain.usecase

import com.magmacoin.leap.core.config.GameConfig
import com.magmacoin.leap.domain.model.Cell
import com.magmacoin.leap.domain.model.CraterRow
import kotlin.math.abs
import kotlin.random.Random

class GenerateRowUseCase(private val random: Random = Random.Default) {

    operator fun invoke(
        index: Int,
        altitude: Int,
        currentLane: Int,
        driftAllowed: Boolean,
        coinBonus: Int
    ): CraterRow {
        val ventLane = random.nextInt(GameConfig.LANES)
        val branch = index > 0 && index % GameConfig.BRANCH_EVERY_ROWS == 0
        val cells = ArrayList<Cell>(GameConfig.LANES)
        for (lane in 0 until GameConfig.LANES) {
            cells.add(buildCell(lane, ventLane, branch, currentLane, coinBonus))
        }
        val drifting = driftAllowed &&
            altitude >= GameConfig.DRIFT_START_ALTITUDE &&
            random.nextFloat() < driftChance(altitude)
        val driftTarget = if (drifting) adjacentLane(ventLane) else ventLane
        return CraterRow(
            index = index,
            cells = cells,
            ventLane = ventLane,
            drifting = drifting && driftTarget != ventLane,
            branch = branch,
            driftTarget = driftTarget
        )
    }

    private fun buildCell(
        lane: Int,
        ventLane: Int,
        branch: Boolean,
        currentLane: Int,
        coinBonus: Int
    ): Cell {
        if (lane == ventLane) {
            return Cell.Vent
        }
        val base = GameConfig.COIN_BASE + coinBonus
        if (branch) {
            val far = abs(lane - currentLane) >= 2
            return Cell.Coin(if (far) base * GameConfig.BRANCH_MULTIPLIER else base)
        }
        if (random.nextInt(100) < GameConfig.COIN_CHANCE_PERCENT) {
            return Cell.Coin(base)
        }
        return Cell.Rock
    }

    private fun driftChance(altitude: Int): Float {
        val raw = GameConfig.DRIFT_CHANCE_BASE +
            GameConfig.DRIFT_CHANCE_STEP * (altitude / GameConfig.DRIFT_ALTITUDE_STEP)
        return if (raw > GameConfig.DRIFT_CHANCE_MAX) GameConfig.DRIFT_CHANCE_MAX else raw
    }

    private fun adjacentLane(ventLane: Int): Int {
        val options = ArrayList<Int>(2)
        if (ventLane - 1 >= 0) {
            options.add(ventLane - 1)
        }
        if (ventLane + 1 < GameConfig.LANES) {
            options.add(ventLane + 1)
        }
        if (options.isEmpty()) {
            return ventLane
        }
        return options[random.nextInt(options.size)]
    }
}
