package com.errymaricha.dafydiobooth.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyVoucherRequest(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("voucher_code") val voucherCode: String,
    @SerialName("voucher_type") val voucherType: String,
    @SerialName("subtotal_amount") val subtotalAmount: Long? = null,
)

@Serializable
data class VerifyVoucherResponse(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("voucher_code") val voucherCode: String? = null,
    @SerialName("voucher_type") val voucherType: String? = null,
    @SerialName("is_valid") val isValid: Boolean? = null,
    @SerialName("valid") val valid: Boolean? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("remaining_uses") val remainingUses: Int? = null,
    @SerialName("payment_required") val paymentRequired: Boolean? = null,
    @SerialName("unlock_photo") val unlockPhoto: Boolean? = null,
    @SerialName("quote") val quote: PaymentQuotePayload? = null,
)

@Serializable
data class PaymentQuoteRequest(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("voucher_code") val voucherCode: String,
    @SerialName("voucher_type") val voucherType: String,
    @SerialName("session_type") val sessionType: String,
    @SerialName("subtotal_amount") val subtotalAmount: Long? = null,
)

@Serializable
data class PaymentQuoteResponse(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("quote_id") val quoteId: String? = null,
    @SerialName("amount") val amount: Long? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("payment_required") val paymentRequired: Boolean? = null,
    @SerialName("unlock_photo") val unlockPhoto: Boolean? = null,
    @SerialName("payment_url") val paymentUrl: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("voucher_code") val voucherCode: String? = null,
    @SerialName("voucher_type") val voucherType: String? = null,
    @SerialName("quote") val quote: PaymentQuotePayload? = null,
)

@Serializable
data class PaymentQuotePayload(
    @SerialName("quote_id") val quoteId: String? = null,
    @SerialName("subtotal_amount") val subtotalAmount: Long? = null,
    @SerialName("discount_amount") val discountAmount: Long? = null,
    @SerialName("total_due") val totalDue: Long? = null,
    @SerialName("amount") val amount: Long? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("payment_required") val paymentRequired: Boolean? = null,
    @SerialName("unlock_photo") val unlockPhoto: Boolean? = null,
    @SerialName("discount_reason") val discountReason: String? = null,
    @SerialName("payment_url") val paymentUrl: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)

@Serializable
data class CreateSessionRequest(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("voucher_code") val voucherCode: String,
    @SerialName("voucher_type") val voucherType: String,
    @SerialName("quote_id") val quoteId: String,
    @SerialName("session_type") val sessionType: String,
)

@Serializable
data class CreateSessionResponse(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("session_code") val sessionCode: String? = null,
    @SerialName("upload_url") val uploadUrl: String? = null,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("payment_required") val paymentRequired: Boolean? = null,
    @SerialName("unlock_photo") val unlockPhoto: Boolean? = null,
    @SerialName("voucher_applied") val voucherApplied: Boolean? = null,
    @SerialName("voucher_code") val voucherCode: String? = null,
    @SerialName("voucher_type") val voucherType: String? = null,
)

@Serializable
data class PaymentCheckResponse(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("session_code") val sessionCode: String? = null,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("can_upload") val canUpload: Boolean? = null,
    @SerialName("payment_required") val paymentRequired: Boolean? = null,
    @SerialName("payment_unlocked") val paymentUnlocked: Boolean? = null,
    @SerialName("unlock_photo") val unlockPhoto: Boolean? = null,
    @SerialName("skip_reason") val skipReason: String? = null,
    @SerialName("voucher_code") val voucherCode: String? = null,
    @SerialName("voucher_type") val voucherType: String? = null,
)

@Serializable
data class ConfirmPaymentRequest(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("payment_ref") val paymentRef: String,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("amount") val amount: Long,
    @SerialName("currency") val currency: String,
)

@Serializable
data class ConfirmPaymentResponse(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("session_code") val sessionCode: String? = null,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("can_upload") val canUpload: Boolean? = null,
    @SerialName("payment_required") val paymentRequired: Boolean? = null,
    @SerialName("unlock_photo") val unlockPhoto: Boolean? = null,
    @SerialName("payment_ref") val paymentRef: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("paid_at") val paidAt: String? = null,
)

@Serializable
data class ApiErrorBody(
    @SerialName("code") val code: String? = null,
    @SerialName("message") val message: String? = null,
)
