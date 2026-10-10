package com.magmacoin.leap.core.di

import android.content.Context
import com.magmacoin.leap.data.local.PreferencesStorage
import com.magmacoin.leap.data.repository.GearRepositoryImpl
import com.magmacoin.leap.data.repository.ProgressRepositoryImpl
import com.magmacoin.leap.domain.repository.GearRepository
import com.magmacoin.leap.domain.repository.ProgressRepository
import com.magmacoin.leap.domain.usecase.GenerateRowUseCase
import com.magmacoin.leap.domain.usecase.PurchaseGearUseCase
import com.magmacoin.leap.domain.usecase.ResolveLeapUseCase
import com.magmacoin.leap.domain.usecase.SaveRunUseCase

object ServiceLocator {

    private var storage: PreferencesStorage? = null
    private var progress: ProgressRepository? = null
    private var gear: GearRepository? = null

    fun init(context: Context) {
        if (storage == null) {
            val created = PreferencesStorage(context.applicationContext)
            storage = created
            progress = ProgressRepositoryImpl(created)
            gear = GearRepositoryImpl(created)
        }
    }

    fun progressRepository(context: Context): ProgressRepository {
        init(context)
        val existing = progress
        if (existing != null) {
            return existing
        }
        return ProgressRepositoryImpl(PreferencesStorage(context.applicationContext))
    }

    fun gearRepository(context: Context): GearRepository {
        init(context)
        val existing = gear
        if (existing != null) {
            return existing
        }
        return GearRepositoryImpl(PreferencesStorage(context.applicationContext))
    }

    fun generateRowUseCase(): GenerateRowUseCase = GenerateRowUseCase()

    fun resolveLeapUseCase(): ResolveLeapUseCase = ResolveLeapUseCase()

    fun saveRunUseCase(context: Context): SaveRunUseCase =
        SaveRunUseCase(progressRepository(context))

    fun purchaseGearUseCase(context: Context): PurchaseGearUseCase =
        PurchaseGearUseCase(gearRepository(context))
}
