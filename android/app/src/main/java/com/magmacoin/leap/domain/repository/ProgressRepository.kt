package com.magmacoin.leap.domain.repository

import com.magmacoin.leap.domain.model.PlayerProgress
import com.magmacoin.leap.domain.model.RunResult

interface ProgressRepository {

    fun load(): PlayerProgress

    fun save(progress: PlayerProgress)

    fun commitRun(result: RunResult): PlayerProgress

    fun isHapticEnabled(): Boolean

    fun setHapticEnabled(enabled: Boolean)

    fun isVentPulseEnabled(): Boolean

    fun setVentPulseEnabled(enabled: Boolean)

    fun isHighContrastEnabled(): Boolean

    fun setHighContrastEnabled(enabled: Boolean)

    fun resetProgress()
}
