package com.errymaricha.dafydiobooth.data.repository

import com.errymaricha.dafydiobooth.data.api.CreateSessionResponse
import com.errymaricha.dafydiobooth.data.api.PaymentCheckResponse
import com.errymaricha.dafydiobooth.data.api.PaymentQuoteResponse
import com.errymaricha.dafydiobooth.data.api.TemplateDto
import com.errymaricha.dafydiobooth.data.api.TemplateSlotDto
import com.errymaricha.dafydiobooth.data.api.VerifyVoucherResponse
import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.BoothTemplate
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    customerId = customerId,
    uploadUrl = uploadUrl,
    paymentStatus = paymentStatus,
    paymentRequired = paymentRequired ?: paymentStatus != "paid",
    unlockPhoto = unlockPhoto ?: paymentStatus == "paid",
)

fun PaymentCheckResponse.toDomain() = PaymentStatus(
    sessionId = sessionId,
    sessionCode = sessionCode,
    customerId = customerId,
    paymentStatus = paymentStatus,
    canUpload = canUpload == true || paymentUnlocked == true || unlockPhoto == true,
    paymentRequired = paymentRequired ?: paymentStatus != "paid",
    unlockPhoto = unlockPhoto == true || paymentUnlocked == true,
)

private val mapperJson = Json { ignoreUnknownKeys = true }

fun TemplateDto.toDomain() = BoothTemplate(
    templateId = id,
    templateCode = templateCode,
    templateName = templateName,
    category = category,
    paperSize = paperSize,
    canvasWidth = canvasWidth,
    canvasHeight = canvasHeight,
    thumbnailUrl = firstNotBlank(
        thumbnailSignedUrl,
        signedThumbnailUrl,
        thumbnailUrlSigned,
        thumbnailUrl,
        previewSignedUrl,
        signedPreviewUrl,
        previewUrlSigned,
        previewUrl,
    ),
    previewUrl = firstNotBlank(previewSignedUrl, signedPreviewUrl, previewUrlSigned, previewUrl),
    overlayUrl = firstNotBlank(overlaySignedUrl, signedOverlayUrl, overlayUrlSigned, overlayUrl),
    configJson = config?.toString(),
    slotsJson = mapperJson.encodeToString(ListSerializer(TemplateSlotDto.serializer()), slots),
)

private fun firstNotBlank(vararg values: String?): String? {
    return values.firstOrNull { !it.isNullOrBlank() }
}
