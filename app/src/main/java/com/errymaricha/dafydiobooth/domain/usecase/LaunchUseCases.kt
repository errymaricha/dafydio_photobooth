package com.errymaricha.dafydiobooth.domain.usecase

import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchPaymentStatus
import com.errymaricha.dafydiobooth.domain.model.LaunchEvent
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import com.errymaricha.dafydiobooth.domain.repository.LaunchRepository
import kotlinx.coroutines.flow.Flow

class CalculateFinalAmountUseCase {
    operator fun invoke(
        photoboothPrice: Double,
        additionalPrintPrice: Double,
        additionalPrintCount: Int,
    ): Double {
        return calculateFinalAmount(
            photoboothPrice = photoboothPrice,
            additionalPrintPrice = additionalPrintPrice,
            additionalPrintCount = additionalPrintCount,
        )
    }
}

class PrepareLaunchUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        deviceCode: String,
        apiKey: String,
    ): Pair<String, LaunchPricing> {
        require(deviceCode.isNotBlank()) { "Device code wajib diisi" }
        require(apiKey.isNotBlank()) { "API key wajib diisi" }

        val token = repository.login(deviceCode.trim(), apiKey.trim())
        val pricing = repository.syncPricing(token)
        return token to pricing
    }
}

class SyncLaunchPricingUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(token: String): LaunchPricing {
        require(token.isNotBlank()) { "Token station belum tersedia" }
        return repository.syncPricing(token)
    }
}

class OpenManualSessionUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        token: String,
        eventId: String,
        customerWhatsapp: String,
        voucherCode: String,
        additionalPrintCount: Int,
    ): LaunchSession {
        require(token.isNotBlank()) { "Token station belum tersedia" }

        return repository.openSessionManual(
            token = token,
            eventId = eventId.trim(),
            customerWhatsapp = customerWhatsapp.trim(),
            voucherCode = voucherCode.trim(),
            additionalPrintCount = additionalPrintCount.coerceAtLeast(0),
        )
    }
}

class ListLaunchEventsUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(token: String): List<LaunchEvent> {
        require(token.isNotBlank()) { "Token station belum tersedia" }
        return repository.listEvents(token)
    }
}

class CreateLaunchEventUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        token: String,
        eventCode: String,
        eventName: String,
        cloudEnabled: Boolean = true,
        cloudUploadMode: String = "originals_and_framed",
        cloudSyncTiming: String = "after_render",
        cloudTemplateMarketplaceEnabled: Boolean = true,
    ): LaunchEvent {
        require(token.isNotBlank()) { "Token station belum tersedia" }
        require(eventCode.isNotBlank()) { "Event code wajib diisi" }
        require(eventName.isNotBlank()) { "Event name wajib diisi" }
        return repository.createEvent(
            token = token,
            eventCode = eventCode.trim(),
            eventName = eventName.trim(),
            cloudEnabled = cloudEnabled,
            cloudUploadMode = cloudUploadMode,
            cloudSyncTiming = cloudSyncTiming,
            cloudTemplateMarketplaceEnabled = cloudTemplateMarketplaceEnabled,
        )
    }
}

class UpdateLaunchEventUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        token: String,
        eventId: String,
        eventCode: String,
        eventName: String,
        cloudEnabled: Boolean = true,
        cloudUploadMode: String = "originals_and_framed",
        cloudSyncTiming: String = "after_render",
        cloudTemplateMarketplaceEnabled: Boolean = true,
    ): LaunchEvent {
        require(token.isNotBlank()) { "Token station belum tersedia" }
        require(eventId.isNotBlank()) { "Event ID wajib diisi" }
        require(eventCode.isNotBlank()) { "Event code wajib diisi" }
        require(eventName.isNotBlank()) { "Event name wajib diisi" }
        return repository.updateEvent(
            token = token,
            eventId = eventId.trim(),
            eventCode = eventCode.trim(),
            eventName = eventName.trim(),
            cloudEnabled = cloudEnabled,
            cloudUploadMode = cloudUploadMode,
            cloudSyncTiming = cloudSyncTiming,
            cloudTemplateMarketplaceEnabled = cloudTemplateMarketplaceEnabled,
        )
    }
}

class VerifyLaunchVoucherUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): VoucherVerification {
        require(token.isNotBlank()) { "Token station belum tersedia" }
        require(voucherCode.isNotBlank()) { "Kode voucher wajib diisi" }

        return repository.verifyVoucher(
            token = token,
            voucherCode = voucherCode.trim(),
            subtotalAmount = subtotalAmount.coerceAtLeast(0),
        )
    }
}

class RequestLaunchPaymentQuoteUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        token: String,
        voucherCode: String,
        subtotalAmount: Long,
    ): PaymentQuote {
        require(token.isNotBlank()) { "Token station belum tersedia" }

        return repository.requestPaymentQuote(
            token = token,
            voucherCode = voucherCode.trim(),
            subtotalAmount = subtotalAmount.coerceAtLeast(0),
        )
    }
}

class CheckLaunchPaymentUseCase(
    private val repository: LaunchRepository,
) {
    suspend operator fun invoke(
        token: String,
        sessionId: String,
    ): LaunchPaymentStatus {
        require(token.isNotBlank()) { "Token station belum tersedia" }
        require(sessionId.isNotBlank()) { "Session manual belum dibuat" }

        return repository.checkPayment(
            token = token,
            sessionId = sessionId,
        )
    }

    fun checkPaymentSse(
        token: String,
        sessionId: String,
    ): Flow<LaunchPaymentStatus> {
        require(token.isNotBlank()) { "Token station belum tersedia" }
        require(sessionId.isNotBlank()) { "Session manual belum dibuat" }
        return repository.checkPaymentSse(token, sessionId)
    }
}

data class LaunchUseCases(
    val prepareLaunch: PrepareLaunchUseCase,
    val syncLaunchPricing: SyncLaunchPricingUseCase,
    val listLaunchEvents: ListLaunchEventsUseCase,
    val createLaunchEvent: CreateLaunchEventUseCase,
    val updateLaunchEvent: UpdateLaunchEventUseCase,
    val openManualSession: OpenManualSessionUseCase,
    val verifyLaunchVoucher: VerifyLaunchVoucherUseCase,
    val requestLaunchPaymentQuote: RequestLaunchPaymentQuoteUseCase,
    val checkLaunchPayment: CheckLaunchPaymentUseCase,
    val calculateFinalAmount: CalculateFinalAmountUseCase,
)
