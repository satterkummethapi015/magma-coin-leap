package com.magmacoin.leap.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun getInt(key: String, fallback: Int): Int = prefs.getInt(key, fallback)

    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getBoolean(key: String, fallback: Boolean): Boolean = prefs.getBoolean(key, fallback)

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getStringSet(key: String): Set<String> =
        prefs.getStringSet(key, emptySet()) ?: emptySet()

    fun putStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        const val FILE_NAME = "magma_coin_leap_prefs"
        const val KEY_BEST = "best_altitude"
        const val KEY_RUNS = "total_runs"
        const val KEY_TOKENS = "token_balance"
        const val KEY_OWNED = "owned_gear"
        const val KEY_HAPTIC = "setting_haptic"
        const val KEY_VENT_PULSE = "setting_vent_pulse"
        const val KEY_HIGH_CONTRAST = "setting_high_contrast"
    }
}
