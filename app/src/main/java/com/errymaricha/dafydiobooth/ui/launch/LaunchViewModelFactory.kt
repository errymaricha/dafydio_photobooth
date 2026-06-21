package com.errymaricha.dafydiobooth.ui.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.errymaricha.dafydiobooth.data.session.SessionStateManager
import com.errymaricha.dafydiobooth.domain.usecase.CalculateFinalAmountUseCase
import com.errymaricha.dafydiobooth.domain.usecase.CheckLaunchPaymentUseCase
import com.errymaricha.dafydiobooth.domain.usecase.CreateLaunchEventUseCase
import com.errymaricha.dafydiobooth.domain.usecase.ListLaunchEventsUseCase
import com.errymaricha.dafydiobooth.domain.usecase.OpenManualSessionUseCase
import com.errymaricha.dafydiobooth.domain.usecase.PrepareLaunchUseCase
import com.errymaricha.dafydiobooth.domain.usecase.RequestLaunchPaymentQuoteUseCase
import com.errymaricha.dafydiobooth.domain.usecase.SyncLaunchPricingUseCase
import com.errymaricha.dafydiobooth.domain.usecase.UpdateLaunchEventUseCase
import com.errymaricha.dafydiobooth.domain.usecase.VerifyLaunchVoucherUseCase

class LaunchViewModelFactory(
    private val prepareLaunch: PrepareLaunchUseCase,
    private val syncLaunchPricing: SyncLaunchPricingUseCase,
    private val listLaunchEvents: ListLaunchEventsUseCase,
    private val createLaunchEvent: CreateLaunchEventUseCase,
    private val updateLaunchEvent: UpdateLaunchEventUseCase,
    private val openManualSession: OpenManualSessionUseCase,
    private val verifyLaunchVoucher: VerifyLaunchVoucherUseCase,
    private val requestLaunchPaymentQuote: RequestLaunchPaymentQuoteUseCase,
    private val checkLaunchPayment: CheckLaunchPaymentUseCase,
    private val calculateFinalAmount: CalculateFinalAmountUseCase,
    private val sessionStateManager: SessionStateManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaunchViewModel::class.java)) {
            return LaunchViewModel(
                prepareLaunch = prepareLaunch,
                syncLaunchPricing = syncLaunchPricing,
                listLaunchEvents = listLaunchEvents,
                createLaunchEvent = createLaunchEvent,
                updateLaunchEvent = updateLaunchEvent,
                openManualSession = openManualSession,
                verifyLaunchVoucher = verifyLaunchVoucher,
                requestLaunchPaymentQuote = requestLaunchPaymentQuote,
                checkLaunchPayment = checkLaunchPayment,
                calculateFinalAmount = calculateFinalAmount,
                sessionStateManager = sessionStateManager,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
