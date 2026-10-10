package com.magmacoin.leap.domain.usecase

import com.magmacoin.leap.domain.model.Cell
import com.magmacoin.leap.domain.model.CraterRow
import com.magmacoin.leap.domain.model.LeapOutcome

class ResolveLeapUseCase {

    operator fun invoke(row: CraterRow, lane: Int, shieldAvailable: Boolean): LeapOutcome {
        val cell = row.cells.getOrNull(lane) ?: return LeapOutcome(false, 0, false)
        return when (cell) {
            is Cell.Vent -> if (shieldAvailable) {
                LeapOutcome(true, 0, true)
            } else {
                LeapOutcome(false, 0, false)
            }
            is Cell.Coin -> LeapOutcome(true, cell.value, false)
            is Cell.Rock -> LeapOutcome(true, 0, false)
        }
    }
}
