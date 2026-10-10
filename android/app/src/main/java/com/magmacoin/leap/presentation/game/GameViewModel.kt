package com.magmacoin.leap.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magmacoin.leap.core.config.GameConfig
import com.magmacoin.leap.data.sample.SampleData
import com.magmacoin.leap.domain.model.Cell
import com.magmacoin.leap.domain.model.CraterRow
import com.magmacoin.leap.domain.model.EndCause
import com.magmacoin.leap.domain.model.RunResult
import com.magmacoin.leap.domain.repository.GearRepository
import com.magmacoin.leap.domain.repository.ProgressRepository
import com.magmacoin.leap.domain.usecase.GenerateRowUseCase
import com.magmacoin.leap.domain.usecase.ResolveLeapUseCase
import com.magmacoin.leap.domain.usecase.SaveRunUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val progressRepository: ProgressRepository,
    gearRepository: GearRepository,
    private val generateRow: GenerateRowUseCase,
    private val resolveLeap: ResolveLeapUseCase,
    private val saveRun: SaveRunUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private val _result = MutableStateFlow<RunResult?>(null)
    val result: StateFlow<RunResult?> = _result.asStateFlow()

    val hapticEnabled: Boolean = progressRepository.isHapticEnabled()

    private val ownedGear: Set<String> = gearRepository.owned()
    private val coinBonus =
        if (ownedGear.contains(SampleData.ID_POUCH)) GameConfig.POUCH_COIN_BONUS else 0
    private val fuseRate =
        if (ownedGear.contains(SampleData.ID_CLOAK)) GameConfig.CLOAK_FUSE_RATE else 1f
    private val beacon = ownedGear.contains(SampleData.ID_BEACON)
    private val harness = ownedGear.contains(SampleData.ID_HARNESS)

    private var shieldAvailable = ownedGear.contains(SampleData.ID_BOOTS)
    private var rowCounter = 0
    private var floorRemaining = GameConfig.IDLE_FLOOR_MS
    private var fuseRemaining = GameConfig.ROW_FUSE_MS.toFloat()
    private var ticker: Job? = null
    private var driftJob: Job? = null

    init {
        startRun()
    }

    fun canLeap(): Boolean {
        val current = _state.value
        return !current.finished && current.lanesEnabled && current.rows.size > 1
    }

    fun beginLeap() {
        val current = _state.value
        if (current.finished) {
            return
        }
        _state.value = current.copy(phase = GamePhase.LEAPING, lanesEnabled = false)
    }

    fun completeLeap(lane: Int) {
        val current = _state.value
        if (current.finished) {
            return
        }
        val targetRow = current.rows.getOrNull(1)
        if (targetRow == null) {
            _state.value = current.copy(phase = GamePhase.IDLE, lanesEnabled = true)
            return
        }
        val outcome = resolveLeap(targetRow, lane, shieldAvailable)
        if (!outcome.survived) {
            finish(EndCause.VENT, current.copy(explorerLane = lane))
            return
        }
        if (outcome.shielded) {
            shieldAvailable = false
        }
        val altitude = current.altitude + 1
        val rows = ArrayList(current.rows.drop(1))
        rowCounter += 1
        rows.add(buildRow(rowCounter, altitude, lane))
        fuseRemaining = GameConfig.ROW_FUSE_MS.toFloat()
        _state.value = current.copy(
            rows = rows,
            explorerLane = lane,
            altitude = altitude,
            runTokens = current.runTokens + outcome.tokens,
            leaps = current.leaps + 1,
            fuseFraction = 1f,
            phase = GamePhase.IDLE,
            lanesEnabled = true,
            branchLane = branchLaneFor(rows)
        )
        scheduleDrift()
    }

    private fun startRun() {
        val lane = 1
        val rows = ArrayList<CraterRow>()
        rows.add(landingRow())
        for (index in 1 until GameConfig.BUFFER_ROWS) {
            rowCounter = index
            rows.add(buildRow(index, index, lane))
        }
        _state.value = GameUiState(
            rows = rows,
            explorerLane = lane,
            branchLane = branchLaneFor(rows),
            ventPulse = progressRepository.isVentPulseEnabled(),
            highContrast = progressRepository.isHighContrastEnabled()
        )
        startTicker()
    }

    private fun landingRow(): CraterRow = CraterRow(
        index = 0,
        cells = listOf(Cell.Rock, Cell.Rock, Cell.Rock),
        ventLane = -1,
        drifting = false,
        branch = false,
        driftTarget = -1
    )

    private fun buildRow(index: Int, altitude: Int, lane: Int): CraterRow {
        val driftAllowed = !(harness && altitude < GameConfig.HARNESS_SAFE_ALTITUDE)
        return generateRow(index, altitude, lane, driftAllowed, coinBonus)
    }

    private fun branchLaneFor(rows: List<CraterRow>): Int {
        if (!beacon) {
            return -1
        }
        val next = rows.getOrNull(1) ?: return -1
        if (!next.branch) {
            return -1
        }
        var bestLane = -1
        var bestValue = 0
        for (lane in next.cells.indices) {
            val cell = next.cells[lane]
            if (cell is Cell.Coin && cell.value > bestValue) {
                bestValue = cell.value
                bestLane = lane
            }
        }
        return bestLane
    }

    private fun scheduleDrift() {
        driftJob?.cancel()
        val next = _state.value.rows.getOrNull(1) ?: return
        if (!next.drifting) {
            return
        }
        driftJob = viewModelScope.launch {
            delay(GameConfig.DRIFT_TELEGRAPH_MS)
            val current = _state.value
            if (current.finished) {
                return@launch
            }
            val target = current.rows.getOrNull(1) ?: return@launch
            if (!target.drifting) {
                return@launch
            }
            val rows = ArrayList(current.rows)
            rows[1] = shiftVent(target)
            _state.value = current.copy(rows = rows)
        }
    }

    private fun shiftVent(row: CraterRow): CraterRow {
        val cells = ArrayList<Cell>(row.cells.size)
        for (lane in row.cells.indices) {
            cells.add(
                when (lane) {
                    row.driftTarget -> Cell.Vent
                    row.ventLane -> Cell.Rock
                    else -> row.cells[lane]
                }
            )
        }
        return row.copy(cells = cells, ventLane = row.driftTarget, drifting = false)
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (isActive) {
                delay(GameConfig.FUSE_TICK_MS)
                val current = _state.value
                if (current.finished) {
                    return@launch
                }
                if (floorRemaining > 0L) {
                    floorRemaining -= GameConfig.FUSE_TICK_MS
                    continue
                }
                fuseRemaining -= GameConfig.FUSE_TICK_MS.toFloat() * fuseRate
                if (fuseRemaining <= 0f) {
                    fuseRemaining = 0f
                    finish(EndCause.FUSE, _state.value.copy(fuseFraction = 0f))
                    return@launch
                }
                _state.value = _state.value.copy(
                    fuseFraction = fuseRemaining / GameConfig.ROW_FUSE_MS.toFloat()
                )
            }
        }
    }

    private fun finish(cause: EndCause, base: GameUiState) {
        ticker?.cancel()
        driftJob?.cancel()
        val before = progressRepository.load()
        val newBest = base.altitude > before.bestAltitude
        val runResult = RunResult(
            altitude = base.altitude,
            tokens = base.runTokens,
            leaps = base.leaps,
            newBest = newBest,
            cause = cause
        )
        saveRun(runResult)
        _state.value = base.copy(
            phase = GamePhase.FINISHED,
            lanesEnabled = false,
            finished = true,
            cause = cause
        )
        _result.value = runResult
    }

    override fun onCleared() {
        ticker?.cancel()
        driftJob?.cancel()
        ticker = null
        driftJob = null
        super.onCleared()
    }
}
