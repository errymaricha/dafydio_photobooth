package com.errymaricha.dafydiobooth.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DeviceAuthRequest(
    @SerialName("device_code") val deviceCode: String,
    @SerialName("api_key") val apiKey: String,
)

@Serializable
data class DeviceAuthResponse(
    @SerialName("token") val token: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("station_id") val stationId: String,
    @SerialName("device_code") val deviceCode: String,
    @SerialName("station_code") val stationCode: String? = null,
    @SerialName("message") val message: String? = null,
) {
    val bearerToken: String
        get() = token
}

@Serializable
data class DeviceMasterDataResponse(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("station") val station: StationDto,
    @SerialName("pricing") val pricing: PricingDto,
    @SerialName("templates") val templates: List<TemplateDto>,
)

@Serializable
data class StationDto(
    @SerialName("id") val id: String,
    @SerialName("station_code") val stationCode: String,
    @SerialName("station_name") val stationName: String,
    @SerialName("location_name") val locationName: String? = null,
    @SerialName("timezone") val timezone: String? = null,
    @SerialName("local_ip") val localIp: String? = null,
    @SerialName("status") val status: String? = null,
)

@Serializable
data class PricingDto(
    @SerialName("photobooth_price") val photoboothPrice: Double,
    @SerialName("additional_print_price") val additionalPrintPrice: Double,
    @SerialName("currency_code") val currencyCode: String,
)

@Serializable
data class TemplateDto(
    @SerialName("id") val id: String,
    @SerialName("template_code") val templateCode: String,
    @SerialName("template_name") val templateName: String,
    @SerialName("category") val category: String? = null,
    @SerialName("paper_size") val paperSize: String? = null,
    @SerialName("canvas_width") val canvasWidth: Int,
    @SerialName("canvas_height") val canvasHeight: Int,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("thumbnail_signed_url") val thumbnailSignedUrl: String? = null,
    @SerialName("signed_thumbnail_url") val signedThumbnailUrl: String? = null,
    @SerialName("thumbnail_url_signed") val thumbnailUrlSigned: String? = null,
    @SerialName("preview_url") val previewUrl: String? = null,
    @SerialName("preview_signed_url") val previewSignedUrl: String? = null,
    @SerialName("signed_preview_url") val signedPreviewUrl: String? = null,
    @SerialName("preview_url_signed") val previewUrlSigned: String? = null,
    @SerialName("overlay_url") val overlayUrl: String? = null,
    @SerialName("overlay_signed_url") val overlaySignedUrl: String? = null,
    @SerialName("signed_overlay_url") val signedOverlayUrl: String? = null,
    @SerialName("overlay_url_signed") val overlayUrlSigned: String? = null,
    @SerialName("asset_urls_expires_at") val assetUrlsExpiresAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("config") val config: JsonObject? = null,
    @SerialName("slots") val slots: List<TemplateSlotDto>,
)

@Serializable
data class TemplateSlotDto(
    @SerialName("slot_index") val slotIndex: Int,
    @SerialName("source_slot_index") val sourceSlotIndex: Int? = null,
    @SerialName("x") val x: Int,
    @SerialName("y") val y: Int,
    @SerialName("width") val width: Int,
    @SerialName("height") val height: Int,
    @SerialName("rotation") val rotation: Double,
    @SerialName("border_radius") val borderRadius: Int,
    @SerialName("metadata") val metadata: JsonObject? = null,
)

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
    @SerialName("customer_id") val customerId: String? = null,
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
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("voucher_code") val voucherCode: String,
    @SerialName("voucher_type") val voucherType: String,
    @SerialName("quote_id") val quoteId: String,
    @SerialName("session_type") val sessionType: String,
    @SerialName("customer_id") val customerId: String? = null,
)

@Serializable
data class OpenManualSessionRequest(
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("customer_whatsapp") val customerWhatsapp: String? = null,
    @SerialName("voucher_code") val voucherCode: String? = null,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("additional_print_count") val additionalPrintCount: Int = 0,
)

@Serializable
data class DeviceEventDto(
    @SerialName("id") val id: String = "",
    @SerialName("event_id")
    val eventIdAlias: String? = null,
    @SerialName("event_code") val eventCode: String,
    @SerialName("event_name") val eventName: String,
    @SerialName("cloud_enabled") val cloudEnabled: Boolean = false,
    @SerialName("cloud_upload_mode") val cloudUploadMode: String? = null,
    @SerialName("cloud_sync_timing") val cloudSyncTiming: String? = null,
    @SerialName("cloud_template_marketplace_enabled") val cloudTemplateMarketplaceEnabled: Boolean = false,
)

@Serializable
data class CreateDeviceEventRequest(
    @SerialName("event_code") val eventCode: String,
    @SerialName("event_name") val eventName: String,
    @SerialName("cloud_enabled") val cloudEnabled: Boolean,
    @SerialName("cloud_upload_mode") val cloudUploadMode: String,
    @SerialName("cloud_sync_timing") val cloudSyncTiming: String,
    @SerialName("cloud_template_marketplace_enabled") val cloudTemplateMarketplaceEnabled: Boolean,
)

@Serializable
data class UpdateDeviceEventRequest(
    @SerialName("event_code") val eventCode: String? = null,
    @SerialName("event_name") val eventName: String? = null,
    @SerialName("cloud_enabled") val cloudEnabled: Boolean? = null,
    @SerialName("cloud_upload_mode") val cloudUploadMode: String? = null,
    @SerialName("cloud_sync_timing") val cloudSyncTiming: String? = null,
    @SerialName("cloud_template_marketplace_enabled") val cloudTemplateMarketplaceEnabled: Boolean? = null,
)

@Serializable
data class CreateSessionResponse(
    @SerialName("contract_version") val contractVersion: String = "",
    @SerialName("session_id") val sessionId: String,
    @SerialName("session_code") val sessionCode: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("upload_url") val uploadUrl: String? = null,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("payment_required") val paymentRequired: Boolean? = null,
    @SerialName("unlock_photo") val unlockPhoto: Boolean? = null,
    @SerialName("voucher_applied") val voucherApplied: Boolean? = null,
    @SerialName("voucher_code") val voucherCode: String? = null,
    @SerialName("voucher_type") val voucherType: String? = null,
)

typealias SessionCreateResponse = CreateSessionResponse

@Serializable
data class PaymentCheckResponse(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("session_code") val sessionCode: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("status") val status: String? = null,
    @SerialName("approval_status") val approvalStatus: String? = null,
    @SerialName("review_status") val reviewStatus: String? = null,
    @SerialName("manual_review_status") val manualReviewStatus: String? = null,
    @SerialName("manual_payment_status") val manualPaymentStatus: String? = null,
    @SerialName("payment_approval_status") val paymentApprovalStatus: String? = null,
    @SerialName("can_upload") val canUpload: Boolean? = null,
    @SerialName("payment_required") val paymentRequired: Boolean? = null,
    @SerialName("payment_unlocked") val paymentUnlocked: Boolean? = null,
    @SerialName("unlock_photo") val unlockPhoto: Boolean? = null,
    @SerialName("skip_reason") val skipReason: String? = null,
    @SerialName("reject_reason") val rejectReason: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("reason") val reason: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("review_notes") val reviewNotes: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    @SerialName("reviewer") val reviewer: String? = null,
    @SerialName("reviewer_name") val reviewerName: String? = null,
    @SerialName("reviewed_by") val reviewedBy: String? = null,
    @SerialName("reviewed_by_name") val reviewedByName: String? = null,
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
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("can_upload") val canUpload: Boolean? = null,
    @SerialName("payment_required") val paymentRequired: Boolean? = null,
    @SerialName("unlock_photo") val unlockPhoto: Boolean? = null,
    @SerialName("payment_ref") val paymentRef: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("paid_at") val paidAt: String? = null,
)

@Serializable
data class CreateEditJobRequest(
    @SerialName("template_id") val templateId: String,
    @SerialName("items") val items: List<EditJobItemRequest>,
)

@Serializable
data class EditJobItemRequest(
    @SerialName("session_photo_id") val sessionPhotoId: String,
    @SerialName("slot_index") val slotIndex: Int,
)

@Serializable
data class RenderEditJobRequest(
    @SerialName("force") val force: Boolean? = null,
)

@Serializable
data class ApiErrorBody(
    @SerialName("code") val code: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("errors") val errors: Map<String, List<String>>? = null,
)
