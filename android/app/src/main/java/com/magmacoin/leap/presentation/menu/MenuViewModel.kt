package com.magmacoin.leap.presentation.menu

import androidx.lifecycle.ViewModel
import com.magmacoin.leap.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel(private val progressRepository: ProgressRepository) : ViewModel() {

    private val _state = MutableStateFlow(MenuUiState())
    val state: StateFlow<MenuUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val progress = progressRepository.load()
        _state.value = MenuUiState(
            bestAltitude = progress.bestAltitude,
            runs = progress.runs,
            tokens = progress.tokens
        )
    }
}
