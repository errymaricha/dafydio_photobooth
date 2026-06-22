package com.errymaricha.dafydiobooth.ui.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LaunchViewModel(
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
) : ViewModel() {
    private val _ui = MutableStateFlow(LaunchUiState())
    val ui: StateFlow<LaunchUiState> = _ui.asStateFlow()
    private var approvalPollingJob: Job? = null

    fun onWhatsappChanged(value: String) {
        _ui.update {
            it.copy(
                customerWhatsapp = value.filter(Char::isDigit),
                error = null,
            )
        }
    }

    fun onAdditionalPrintChanged(value: Int) {
        val pricing = _ui.value.pricing
        val count = value.coerceAtLeast(0)
        val total = if (pricing == null) {
            0.0
        } else {
            calculateFinalAmount(
                pricing.photoboothPrice,
                pricing.additionalPrintPrice,
                count,
            )
        }

        _ui.update {
            it.copy(
                additionalPrintCount = count,
                finalAmount = total,
                quote = null,
                error = null,
            )
        }
    }

    fun onVoucherCodeChanged(value: String) {
        _ui.update {
            it.copy(
                voucherCode = value.trim().uppercase(),
                voucher = null,
                quote = null,
                error = null,
                message = null,
            )
        }
    }

    fun onEventCodeChanged(value: String) {
        _ui.update { it.copy(eventCodeInput = value.trim().uppercase(), error = null) }
    }

    fun onEventNameChanged(value: String) {
        _ui.update { it.copy(eventNameInput = value, error = null) }
    }

    fun onSelectEvent(eventId: String) {
        val selected = _ui.value.events.firstOrNull { it.eventId == eventId }
        _ui.update {
            it.copy(
                selectedEventId = eventId,
                eventCodeInput = selected?.eventCode ?: it.eventCodeInput,
                eventNameInput = selected?.eventName ?: it.eventNameInput,
                error = null,
            )
        }
    }

    fun refreshEvents() {
        viewModelScope.launch {
            val current = _ui.value
            val token = sessionStateManager.snapshot().authToken.ifBlank { current.token.orEmpty() }
            if (token.isBlank()) {
                _ui.update { it.copy(error = "Token station belum tersedia") }
                return@launch
            }

            runCatching {
                _ui.update { it.copy(loading = true, error = null, message = null) }
                listLaunchEvents(token).sortedBy { it.eventName.lowercase() }
            }.onSuccess { events ->
                val selectedEventId = current.selectedEventId.takeIf { selected ->
                    selected.isNotBlank() && events.any { it.eventId == selected }
                } ?: events.firstOrNull()?.eventId.orEmpty()
                val selectedEvent = events.firstOrNull { it.eventId == selectedEventId }
                _ui.update {
                    it.copy(
                        loading = false,
                        events = events,
                        selectedEventId = selectedEventId,
                        eventCodeInput = selectedEvent?.eventCode ?: it.eventCodeInput,
                        eventNameInput = selectedEvent?.eventName ?: it.eventNameInput,
                        message = if (events.isEmpty()) "Belum ada event di station." else "Daftar event berhasil disinkronkan.",
                    )
                }
            }.onFailure { error ->
                _ui.update {
                    it.copy(
                        loading = false,
                        error = error.message ?: "Gagal mengambil daftar event",
                    )
                }
            }
        }
    }

    fun init(deviceCode: String, apiKey: String, isSilent: Boolean = true) {
        approvalPollingJob?.cancel()
        viewModelScope.launch {
            runCatching {
                _ui.update { it.copy(loading = !isSilent, error = null, message = null) }
                val snapshot = sessionStateManager.snapshot()
                val existingToken = snapshot.authToken.ifBlank { _ui.value.token.orEmpty() }
                val (token, pricing) = if (existingToken.isNotBlank()) {
                    existingToken to syncLaunchPricing(existingToken)
                } else {
                    prepareLaunch(deviceCode, apiKey)
                }
                sessionStateManager.updateConnection(
                    stationIp = snapshot.stationIp,
                    deviceId = deviceCode.trim(),
                    apiKey = apiKey.trim(),
                    authToken = token,
                    isStationReachable = snapshot.isStationReachable,
                )
                val total = calculateFinalAmount(
                    pricing.photoboothPrice,
                    pricing.additionalPrintPrice,
                    _ui.value.additionalPrintCount,
                )
                val events = runCatching { listLaunchEvents(token) }.getOrDefault(emptyList())
                val selectedEventId = _ui.value.selectedEventId.takeIf { selected ->
                    selected.isNotBlank() && events.any { it.eventId == selected }
                } ?: events.firstOrNull()?.eventId.orEmpty()
                val selectedEvent = events.firstOrNull { it.eventId == selectedEventId }
                _ui.update {
                    it.copy(
                        loading = false,
                        token = token,
                        session = null,
                        pricing = pricing,
                        voucher = null,
                        quote = null,
                        finalAmount = total,
                        approvalStatus = null,
                        shouldNavigateToTemplates = false,
                        events = events,
                        selectedEventId = selectedEventId,
                        eventCodeInput = selectedEvent?.eventCode ?: it.eventCodeInput,
                        eventNameInput = selectedEvent?.eventName ?: it.eventNameInput,
                    )
                }
            }.onFailure { error ->
                _ui.update {
                    it.copy(
                        loading = false,
                        error = error.message ?: "Gagal sinkronisasi master data",
                    )
                }
            }
        }
    }

    fun createOrUpdateEvent() {
        viewModelScope.launch {
            val current = _ui.value
            val token = sessionStateManager.snapshot().authToken.ifBlank { current.token.orEmpty() }
            if (token.isBlank()) {
                _ui.update { it.copy(error = "Token station belum tersedia") }
                return@launch
            }

            runCatching {
                _ui.update { it.copy(loading = true, error = null, message = null) }
                if (current.selectedEventId.isBlank()) {
                    createLaunchEvent(
                        token = token,
                        eventCode = current.eventCodeInput,
                        eventName = current.eventNameInput,
                    )
                } else {
                    updateLaunchEvent(
                        token = token,
                        eventId = current.selectedEventId,
                        eventCode = current.eventCodeInput,
                        eventName = current.eventNameInput,
                    )
                }
            }.onSuccess { saved ->
                val nextEvents = _ui.value.events
                    .filterNot { it.eventId == saved.eventId }
                    .plus(saved)
                    .sortedBy { it.eventName.lowercase() }
                _ui.update {
                    it.copy(
                        loading = false,
                        events = nextEvents,
                        selectedEventId = saved.eventId,
                        eventCodeInput = saved.eventCode,
                        eventNameInput = saved.eventName,
                        message = "Event ${saved.eventCode} tersimpan.",
                        error = null,
                    )
                }
            }.onFailure { error ->
                _ui.update {
                    it.copy(
                        loading = false,
                        error = error.message ?: "Gagal simpan event",
                    )
                }
            }
        }
    }

    fun checkVoucherAndQuote() {
        viewModelScope.launch {
            val current = _ui.value
            val token = sessionStateManager.snapshot().authToken.ifBlank { current.token.orEmpty() }
            val waValidation = validateCustomerWa(current.customerWhatsapp)
            if (waValidation != null) {
                _ui.update { it.copy(error = null, message = waValidation) }
                return@launch
            }
            if (token.isBlank()) {
                _ui.update { it.copy(error = "Token station belum tersedia") }
                return@launch
            }
            if (current.voucherCode.isBlank()) {
                _ui.update { it.copy(error = "Kode voucher wajib diisi") }
                return@launch
            }

            runCatching {
                _ui.update { it.copy(loading = true, error = null, message = null) }
                val subtotal = current.finalAmount.toLong()
                val voucher = verifyLaunchVoucher(
                    token = token,
                    voucherCode = current.voucherCode,
                    subtotalAmount = subtotal,
                )
                val quote = requestLaunchPaymentQuote(
                    token = token,
                    voucherCode = current.voucherCode,
                    subtotalAmount = subtotal,
                )
                voucher to quote
            }.onSuccess { (voucher, quote) ->
                _ui.update {
                    it.copy(
                        loading = false,
                        voucher = voucher,
                        quote = quote,
                        message = if (quote.paymentRequired) {
                            "Voucher valid. Pilih pembayaran manual atau QR Code."
                        } else {
                            "Voucher valid. Tidak perlu pembayaran."
                        },
                    )
                }
            }.onFailure { error ->
                val friendlyWaMessage = toInvalidWaMessage(error.message) ?: toInvalidVoucherMessage(error.message)
                _ui.update {
                    it.copy(
                        loading = false,
                        voucherCode = if (friendlyWaMessage == "Kode voucher tidak valid") "" else it.voucherCode,
                        voucher = if (friendlyWaMessage == "Kode voucher tidak valid") null else it.voucher,
                        quote = if (friendlyWaMessage == "Kode voucher tidak valid") null else it.quote,
                        error = if (friendlyWaMessage != null) null else (error.message ?: "Gagal cek voucher"),
                        message = friendlyWaMessage ?: it.message,
                    )
                }
            }
        }
    }

    fun quoteQrPayment() {
        viewModelScope.launch {
            val current = _ui.value
            val token = sessionStateManager.snapshot().authToken.ifBlank { current.token.orEmpty() }
            val waValidation = validateCustomerWa(current.customerWhatsapp)
            if (waValidation != null) {
                _ui.update { it.copy(error = null, message = waValidation) }
                return@launch
            }
            if (token.isBlank()) {
                _ui.update { it.copy(error = "Token station belum tersedia") }
                return@launch
            }

            runCatching {
                _ui.update { it.copy(loading = true, error = null, message = null) }
                requestLaunchPaymentQuote(
                    token = token,
                    voucherCode = current.voucherCode,
                    subtotalAmount = current.finalAmount.toLong(),
                )
            }.onSuccess { quote ->
                _ui.update {
                    it.copy(
                        loading = false,
                        quote = quote,
                        message = if (quote.paymentUrl.isNullOrBlank()) {
                            "QR Code/Xendit belum tersedia dari Photobooth Station."
                        } else {
                            "QR Code siap. Lanjutkan pembayaran dan cek status."
                        },
                    )
                }
            }.onFailure { error ->
                val friendlyWaMessage = toInvalidWaMessage(error.message) ?: toInvalidVoucherMessage(error.message)
                _ui.update {
                    it.copy(
                        loading = false,
                        error = if (friendlyWaMessage != null) null else (error.message ?: "Gagal menyiapkan QR Code"),
                        message = friendlyWaMessage ?: it.message,
                    )
                }
            }
        }
    }

    fun submitManualPaymentRequest() {
        viewModelScope.launch {
            val current = _ui.value
            val token = sessionStateManager.snapshot().authToken.ifBlank { current.token.orEmpty() }
            val waValidation = validateCustomerWa(current.customerWhatsapp)
            if (waValidation != null) {
                _ui.update { it.copy(error = null, message = waValidation) }
                return@launch
            }
            if (current.selectedEventId.isBlank()) {
                _ui.update { it.copy(error = "Pilih event dulu sebelum request manual payment") }
                return@launch
            }
            if (!current.canSubmitManualPayment) {
                _ui.update {
                    it.copy(error = "Request manual payment masih menunggu tanggapan Photobooth Station")
                }
                return@launch
            }
            if (token.isBlank()) {
                _ui.update { it.copy(error = "Token station belum tersedia") }
                return@launch
            }

            runCatching {
                _ui.update {
                    it.copy(
                        loading = true,
                        session = null,
                        approvalStatus = null,
                        error = null,
                        message = null,
                    )
                }
                openManualSession(
                    token = token,
                    eventId = current.selectedEventId,
                    customerWhatsapp = current.customerWhatsapp,
                    voucherCode = current.voucherCode,
                    additionalPrintCount = current.additionalPrintCount,
                )
            }.onSuccess { session ->
                sessionStateManager.updateFromLaunchSession(
                    session = session,
                    customerWhatsapp = current.customerWhatsapp,
                    authToken = token,
                    selectedEventId = current.selectedEventId,
                )
                _ui.update {
                    it.copy(
                        loading = false,
                        session = session,
                        approvalStatus = session.paymentStatus,
                        message = "Session ${session.sessionCode ?: session.sessionId} terkirim. Menunggu approval untuk session yang sama.",
                        error = null,
                        shouldNavigateToTemplates = false,
                    )
                }
                startApprovalPolling(token, session.sessionId)
            }.onFailure { error ->
                val friendlyWaMessage = toInvalidWaMessage(error.message)
                _ui.update {
                    it.copy(
                        loading = false,
                        error = if (friendlyWaMessage != null) null else (error.message ?: "Gagal kirim request manual"),
                        message = friendlyWaMessage ?: it.message,
                    )
                }
            }
        }
    }

    fun checkManualPaymentApproval() {
        val current = _ui.value
        val token = sessionStateManager.snapshot().authToken.ifBlank { current.token.orEmpty() }
        val sessionId = current.session?.sessionId
        if (token.isBlank() || sessionId.isNullOrBlank()) {
            _ui.update { it.copy(error = "Session manual belum tersedia") }
            return
        }

        viewModelScope.launch {
            checkApprovalOnce(token, sessionId, showWaitingMessage = true)
        }
    }

    fun consumeTemplateNavigation() {
        _ui.update { it.copy(shouldNavigateToTemplates = false) }
    }

    private fun startApprovalPolling(token: String, sessionId: String) {
        approvalPollingJob?.cancel()
        approvalPollingJob = viewModelScope.launch {
            repeat(APPROVAL_POLL_LIMIT) {
                delay(APPROVAL_POLL_INTERVAL_MS)
                val terminal = checkApprovalOnce(token, sessionId, showWaitingMessage = false)
                if (terminal) return@launch
            }
        }
    }

    private suspend fun checkApprovalOnce(
        token: String,
        sessionId: String,
        showWaitingMessage: Boolean,
    ): Boolean {
        return runCatching {
            checkLaunchPayment(token, sessionId)
        }.fold(
            onSuccess = { status ->
                val approved = status.isApproved
                val rejected = status.isRejected
                val rejectedMessage = buildRejectedMessage(status.rejectionReason, status.reviewer, status.reviewedAt)
                if (approved) {
                    sessionStateManager.updateFromLaunchSession(
                        session = _ui.value.session,
                        customerWhatsapp = _ui.value.customerWhatsapp,
                        authToken = token,
                        selectedEventId = _ui.value.selectedEventId,
                    )
                }
                _ui.update {
                    it.copy(
                        approvalStatus = status.displayStatus,
                        message = when {
                            approved -> "Manual payment approved. Membuka pilih template."
                            rejected -> rejectedMessage
                            showWaitingMessage -> "Menunggu approval untuk session ${status.sessionCode ?: status.sessionId}."
                            else -> it.message
                        },
                        shouldNavigateToTemplates = approved,
                        error = null,
                    )
                }
                if (approved || rejected) approvalPollingJob?.cancel()
                approved || rejected
            },
            onFailure = { error ->
                _ui.update {
                    it.copy(error = error.message ?: "Gagal cek approval manual payment")
                }
                false
            },
        )
    }

    override fun onCleared() {
        approvalPollingJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val APPROVAL_POLL_INTERVAL_MS = 2_000L
        const val APPROVAL_POLL_LIMIT = 150
    }

    private fun toInvalidWaMessage(raw: String?): String? {
        val message = raw?.trim().orEmpty()
        if (message.isBlank()) return null
        val lower = message.lowercase()
        val hasTargetField = lower.contains("customer") || lower.contains("whatsapp") || lower.contains("wa")
        val hasInvalidHint = lower.contains("invalid")
            || lower.contains("tidak valid")
            || lower.contains("not valid")
            || lower.contains("not found")
            || lower.contains("tidak ditemukan")
            || lower.contains("unregistered")
            || lower.contains("tidak terdaftar")
        return if (hasTargetField && hasInvalidHint) "No WA tidak valid" else null
    }

    private fun validateCustomerWa(raw: String): String? {
        val digits = raw.filter { it.isDigit() }
        if (digits.isNotEmpty() && digits.length < 10) {
            return "No WA tidak valid (minimal 10 digit)"
        }
        return null
    }

    private fun toInvalidVoucherMessage(raw: String?): String? {
        val message = raw?.trim().orEmpty()
        if (message.isBlank()) return null
        val lower = message.lowercase()
        return if (
            lower.contains("422") ||
            lower.contains("unprocessable") ||
            lower.contains("voucher tidak valid") ||
            lower.contains("kode voucher") ||
            (lower.contains("voucher") && lower.contains("invalid"))
        ) {
            "Kode voucher tidak valid"
        } else {
            null
        }
    }
}

private fun buildRejectedMessage(
    reason: String?,
    reviewer: String?,
    reviewedAt: String?,
): String {
    val details = listOfNotNull(
        reason?.takeIf { it.isNotBlank() }?.let { "Alasan: $it" },
        reviewer?.takeIf { it.isNotBlank() }?.let { "Reviewer: $it" },
        reviewedAt?.takeIf { it.isNotBlank() }?.let { "Reviewed: $it" },
    )
    return if (details.isEmpty()) {
        "Manual payment ditolak Photobooth Station."
    } else {
        "Manual payment ditolak. ${details.joinToString(" | ")}"
    }
}
