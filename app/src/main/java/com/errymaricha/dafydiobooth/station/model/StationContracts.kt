package com.errymaricha.dafydiobooth.station.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
)

@Serializable
data class HeartbeatRequest(
    @SerialName("device_type") val deviceType: String,
    @SerialName("local_ip") val localIp: String,
    @SerialName("battery_percent") val batteryPercent: Int,
    @SerialName("network_strength") val networkStrength: Int,
    @SerialName("app_version") val appVersion: String,
    @SerialName("os_name") val osName: String,
    @SerialName("os_version") val osVersion: String,
    @SerialName("capabilities") val capabilities: HeartbeatCapabilities,
    @SerialName("metrics") val metrics: Map<String, String> = emptyMap(),
    @SerialName("last_sync_at") val lastSyncAt: String,
)

@Serializable
data class HeartbeatCapabilities(
    @SerialName("camera") val camera: Boolean,
    @SerialName("printer") val printer: Boolean,
    @SerialName("offline_queue") val offlineQueue: Boolean,
    @SerialName("local_render") val localRender: Boolean,
)

@Serializable
data class HeartbeatResponse(
    @SerialName("status") val status: String? = null,
    @SerialName("server_time") val serverTime: String? = null,
)

@Serializable
data class TemplateDto(
    @SerialName("id") val id: String,
    @SerialName("template_name") val templateName: String,
    @SerialName("template_code") val templateCode: String,
    @SerialName("paper_size") val paperSize: String? = null,
    @SerialName("preview_url") val previewUrl: String? = null,
)

@Serializable
data class VerifyVoucherRequest(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("voucher_code") val voucherCode: String,
    @SerialName("voucher_type") val voucherType: String,
)

@Serializable
data class VerifyVoucherResponse(
    @SerialName("is_valid") val isValid: Boolean? = null,
    @SerialName("message") val message: String? = null,
)

@Serializable
data class PaymentQuoteRequest(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("voucher_code") val voucherCode: String,
    @SerialName("voucher_type") val voucherType: String,
    @SerialName("session_type") val sessionType: String,
)

@Serializable
data class PaymentQuoteResponse(
    @SerialName("quote_id") val quoteId: String? = null,
    @SerialName("amount") val amount: Long? = null,
    @SerialName("payment_required") val paymentRequired: Boolean? = null,
)

@Serializable
data class CreateSessionRequest(
    @SerialName("contract_version") val contractVersion: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("quote_id") val quoteId: String,
    @SerialName("session_type") val sessionType: String,
)

@Serializable
data class CreateSessionResponse(
    @SerialName("session_id") val sessionId: String,
    @SerialName("session_code") val sessionCode: String? = null,
    @SerialName("payment_status") val paymentStatus: String,
)

@Serializable
data class PaymentCheckResponse(
    @SerialName("session_id") val sessionId: String,
    @SerialName("payment_status") val paymentStatus: String,
)

@Serializable
data class SessionCompleteResponse(
    @SerialName("status") val status: String? = null,
)

@Serializable
data class ApiErrorBody(
    @SerialName("message") val message: String? = null,
    @SerialName("errors") val errors: Map<String, List<String>>? = null,
)
