package com.magmacoin.leap.data.repository

import com.magmacoin.leap.core.config.GameConfig
import com.magmacoin.leap.data.local.PreferencesStorage
import com.magmacoin.leap.data.sample.SampleData
import com.magmacoin.leap.domain.model.GearItem
import com.magmacoin.leap.domain.repository.GearRepository

class GearRepositoryImpl(private val storage: PreferencesStorage) : GearRepository {

    override fun catalogue(): List<GearItem> {
        val ownedIds = owned()
        return SampleData.GEAR.map { item -> item.copy(owned = ownedIds.contains(item.id)) }
    }

    override fun owned(): Set<String> = storage.getStringSet(PreferencesStorage.KEY_OWNED)

    override fun balance(): Int =
        storage.getInt(PreferencesStorage.KEY_TOKENS, GameConfig.STARTING_TOKENS)

    override fun purchase(id: String): Boolean {
        val item = SampleData.GEAR.firstOrNull { entry -> entry.id == id } ?: return false
        val ownedIds = owned()
        if (ownedIds.contains(id)) {
            return false
        }
        val current = balance()
        if (current < item.cost) {
            return false
        }
        storage.putInt(PreferencesStorage.KEY_TOKENS, current - item.cost)
        storage.putStringSet(PreferencesStorage.KEY_OWNED, ownedIds + id)
        return true
    }
}
