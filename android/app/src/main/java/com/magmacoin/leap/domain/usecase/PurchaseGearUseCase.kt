package com.magmacoin.leap.domain.usecase

import com.magmacoin.leap.domain.repository.GearRepository

class PurchaseGearUseCase(private val repository: GearRepository) {

    operator fun invoke(id: String): Boolean = repository.purchase(id)
}
