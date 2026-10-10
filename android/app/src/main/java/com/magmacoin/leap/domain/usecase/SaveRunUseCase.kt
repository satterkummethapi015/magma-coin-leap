package com.magmacoin.leap.domain.usecase

import com.magmacoin.leap.domain.model.PlayerProgress
import com.magmacoin.leap.domain.model.RunResult
import com.magmacoin.leap.domain.repository.ProgressRepository

class SaveRunUseCase(private val repository: ProgressRepository) {

    operator fun invoke(result: RunResult): PlayerProgress = repository.commitRun(result)
}
