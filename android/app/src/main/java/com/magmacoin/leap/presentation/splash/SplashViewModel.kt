package com.magmacoin.leap.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magmacoin.leap.core.config.GameConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private var timer: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _ready.value = true
        }
    }

    fun consume() {
        _ready.value = false
    }

    override fun onCleared() {
        timer?.cancel()
        timer = null
        super.onCleared()
    }
}
