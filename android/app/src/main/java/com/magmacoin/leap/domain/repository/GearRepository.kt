package com.magmacoin.leap.domain.repository

import com.magmacoin.leap.domain.model.GearItem

interface GearRepository {

    fun catalogue(): List<GearItem>

    fun owned(): Set<String>

    fun balance(): Int

    fun purchase(id: String): Boolean
}
