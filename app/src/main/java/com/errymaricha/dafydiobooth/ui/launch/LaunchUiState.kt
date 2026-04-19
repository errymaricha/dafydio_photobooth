package com.errymaricha.dafydiobooth.ui.launch

import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import com.errymaricha.dafydiobooth.domain.model.isRejectedPaymentStatus

data class LaunchUiState(
    val loading: Boolean = false,
    val token: String? = null,
    val session: LaunchSession? = null,
    val customerWhatsapp: String = "",
    val voucherCode: String = "",
    val voucher: VoucherVerification? = null,
    val additionalPrintCount: Int = 0,
    val pricing: LaunchPricing? = null,
    val quote: PaymentQuote? = null,
    val finalAmount: Double = 0.0,
    val approvalStatus: String? = null,
    val shouldNavigateToTemplates: Boolean = false,
    val message: String? = null,
    val error: String? = null,
) {
    val isManualPaymentRejected: Boolean
        get() = approvalStatus.isRejectedPaymentStatus()

    val isManualPaymentWaiting: Boolean
        get() = session != null && !isManualPaymentRejected && shouldNavigateToTemplates.not()

    val canSubmitManualPayment: Boolean
        get() = !loading && !isManualPaymentWaiting
}
