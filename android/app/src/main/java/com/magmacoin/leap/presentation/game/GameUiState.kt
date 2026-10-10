package com.magmacoin.leap.presentation.game

import com.magmacoin.leap.domain.model.CraterRow
import com.magmacoin.leap.domain.model.EndCause

data class GameUiState(
    val rows: List<CraterRow> = emptyList(),
    val explorerLane: Int = 1,
    val altitude: Int = 0,
    val runTokens: Int = 0,
    val leaps: Int = 0,
    val fuseFraction: Float = 1f,
    val phase: GamePhase = GamePhase.IDLE,
    val lanesEnabled: Boolean = true,
    val branchLane: Int = -1,
    val ventPulse: Boolean = true,
    val highContrast: Boolean = false,
    val finished: Boolean = false,
    val cause: EndCause = EndCause.NONE
)
