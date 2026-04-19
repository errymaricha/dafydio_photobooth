package com.errymaricha.dafydiobooth.data.repository

import com.errymaricha.dafydiobooth.data.api.CreateSessionResponse
import com.errymaricha.dafydiobooth.data.api.PaymentCheckResponse
import com.errymaricha.dafydiobooth.data.api.PaymentQuoteResponse
import com.errymaricha.dafydiobooth.data.api.VerifyVoucherResponse
import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification

fun VerifyVoucherResponse.toDomain() = VoucherVerification(
    code = voucherCode.orEmpty(),
    type = voucherType.orEmpty(),
    isValid = isValid ?: valid ?: false,
    status = status ?: if (isValid == true || valid == true) "valid" else "invalid",
    message = message,
    customerName = customerName,
    remainingUses = remainingUses,
    paymentRequired = paymentRequired ?: quote?.paymentRequired,
    unlockPhoto = unlockPhoto ?: quote?.unlockPhoto,
)

fun PaymentQuoteResponse.toDomain() = PaymentQuote(
    quoteId = quote?.quoteId ?: quoteId.orEmpty(),
    amount = quote?.totalDue ?: quote?.amount ?: amount ?: 0L,
    currency = quote?.currency ?: currency ?: "IDR",
    paymentRequired = quote?.paymentRequired ?: paymentRequired ?: true,
    paymentUrl = quote?.paymentUrl ?: paymentUrl,
    expiresAt = quote?.expiresAt ?: expiresAt,
    subtotalAmount = quote?.subtotalAmount,
    discountAmount = quote?.discountAmount,
    unlockPhoto = quote?.unlockPhoto ?: unlockPhoto ?: false,
    discountReason = quote?.discountReason,
)

fun CreateSessionResponse.toDomain() = BoothSession(
    sessionId = sessionId,
    sessionCode = sessionCode,
    uploadUrl = uploadUrl,
    paymentStatus = paymentStatus,
    paymentRequired = paymentRequired ?: paymentStatus != "paid",
    unlockPhoto = unlockPhoto ?: paymentStatus == "paid",
)

fun PaymentCheckResponse.toDomain() = PaymentStatus(
    sessionId = sessionId,
    sessionCode = sessionCode,
    paymentStatus = paymentStatus,
    canUpload = canUpload == true || paymentUnlocked == true || unlockPhoto == true,
    paymentRequired = paymentRequired ?: paymentStatus != "paid",
    unlockPhoto = unlockPhoto == true || paymentUnlocked == true,
)
