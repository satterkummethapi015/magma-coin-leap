package com.magmacoin.leap.data.repository

import com.magmacoin.leap.core.config.GameConfig
import com.magmacoin.leap.data.local.PreferencesStorage
import com.magmacoin.leap.domain.model.PlayerProgress
import com.magmacoin.leap.domain.model.RunResult
import com.magmacoin.leap.domain.repository.ProgressRepository

class ProgressRepositoryImpl(private val storage: PreferencesStorage) : ProgressRepository {

    override fun load(): PlayerProgress = PlayerProgress(
        bestAltitude = storage.getInt(PreferencesStorage.KEY_BEST, 0),
        runs = storage.getInt(PreferencesStorage.KEY_RUNS, 0),
        tokens = storage.getInt(PreferencesStorage.KEY_TOKENS, GameConfig.STARTING_TOKENS)
    )

    override fun save(progress: PlayerProgress) {
        storage.putInt(PreferencesStorage.KEY_BEST, progress.bestAltitude)
        storage.putInt(PreferencesStorage.KEY_RUNS, progress.runs)
        storage.putInt(PreferencesStorage.KEY_TOKENS, progress.tokens)
    }

    override fun commitRun(result: RunResult): PlayerProgress {
        val current = load()
        val best = if (result.altitude > current.bestAltitude) result.altitude else current.bestAltitude
        val updated = PlayerProgress(
            bestAltitude = best,
            runs = current.runs + 1,
            tokens = current.tokens + result.tokens
        )
        save(updated)
        return updated
    }

    override fun isHapticEnabled(): Boolean =
        storage.getBoolean(PreferencesStorage.KEY_HAPTIC, true)

    override fun setHapticEnabled(enabled: Boolean) {
        storage.putBoolean(PreferencesStorage.KEY_HAPTIC, enabled)
    }

    override fun isVentPulseEnabled(): Boolean =
        storage.getBoolean(PreferencesStorage.KEY_VENT_PULSE, true)

    override fun setVentPulseEnabled(enabled: Boolean) {
        storage.putBoolean(PreferencesStorage.KEY_VENT_PULSE, enabled)
    }

    override fun isHighContrastEnabled(): Boolean =
        storage.getBoolean(PreferencesStorage.KEY_HIGH_CONTRAST, false)

    override fun setHighContrastEnabled(enabled: Boolean) {
        storage.putBoolean(PreferencesStorage.KEY_HIGH_CONTRAST, enabled)
    }

    override fun resetProgress() {
        storage.clearAll()
    }
}
