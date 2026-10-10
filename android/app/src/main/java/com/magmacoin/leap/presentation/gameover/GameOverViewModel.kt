package com.magmacoin.leap.presentation.gameover

import androidx.lifecycle.ViewModel
import com.magmacoin.leap.core.config.GameConfig
import com.magmacoin.leap.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameOverViewModel(
    private val progressRepository: ProgressRepository,
    private val altitude: Int,
    private val tokens: Int,
    private val leaps: Int,
    private val newBest: Boolean
) : ViewModel() {

    private val _state = MutableStateFlow(GameOverUiState())
    val state: StateFlow<GameOverUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val progress = progressRepository.load()
        _state.value = GameOverUiState(
            altitude = altitude,
            tokens = tokens,
            leaps = leaps,
            bestAltitude = progress.bestAltitude,
            balance = progress.tokens,
            newBest = newBest,
            summit = altitude >= GameConfig.SUMMIT_ALTITUDE
        )
    }
}
