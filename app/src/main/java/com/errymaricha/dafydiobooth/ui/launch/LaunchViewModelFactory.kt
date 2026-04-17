package com.errymaricha.dafydiobooth.ui.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.errymaricha.dafydiobooth.domain.usecase.CalculateFinalAmountUseCase
import com.errymaricha.dafydiobooth.domain.usecase.OpenManualSessionUseCase
import com.errymaricha.dafydiobooth.domain.usecase.PrepareLaunchUseCase

class LaunchViewModelFactory(
    private val prepareLaunch: PrepareLaunchUseCase,
    private val openManualSession: OpenManualSessionUseCase,
    private val calculateFinalAmount: CalculateFinalAmountUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaunchViewModel::class.java)) {
            return LaunchViewModel(
                prepareLaunch = prepareLaunch,
                openManualSession = openManualSession,
                calculateFinalAmount = calculateFinalAmount,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
