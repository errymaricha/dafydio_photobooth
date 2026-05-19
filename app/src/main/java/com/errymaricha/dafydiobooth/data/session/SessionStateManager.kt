package com.errymaricha.dafydiobooth.data.session

import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SessionState(
    val stationIp: String = "",
    val deviceId: String = "",
    val apiKey: String = "",
    val authToken: String = "",
    val isStationConnected: Boolean = false,
    val customerId: String = "",
    val customerWhatsapp: String = "",
    val selectedEventId: String = "",
    val sessionId: String? = null,
    val sessionCode: String? = null,
    val uploadUrl: String? = null,
    val paymentStatus: String? = null,
    val paymentRequired: Boolean? = null,
    val unlockPhoto: Boolean? = null,
)

class SessionStateManager {
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    fun snapshot(): SessionState = _state.value

    fun updateConnection(
        stationIp: String,
        deviceId: String,
        apiKey: String,
        authToken: String,
    ) {
        _state.update {
            it.copy(
                stationIp = stationIp,
                deviceId = deviceId,
                apiKey = apiKey,
                authToken = authToken,
                isStationConnected = authToken.isNotBlank(),
            )
        }
    }

    fun updateFromBoothSession(session: BoothSession?, customerId: String) {
        _state.update {
            val resolvedCustomerId = session?.customerId?.takeIf { it.isNotBlank() }
                ?: customerId.takeIf { it.isNotBlank() }
                ?: it.customerId.ifBlank { buildFallbackCustomerId() }
            it.copy(
                customerId = resolvedCustomerId,
                sessionId = session?.sessionId,
                sessionCode = session?.sessionCode,
                uploadUrl = session?.uploadUrl,
                paymentStatus = session?.paymentStatus,
                paymentRequired = session?.paymentRequired,
                unlockPhoto = session?.unlockPhoto,
            )
        }
    }

    fun updateFromLaunchSession(
        session: LaunchSession?,
        customerWhatsapp: String,
        authToken: String?,
        selectedEventId: String = "",
    ) {
        _state.update {
            val resolvedToken = authToken?.ifBlank { it.authToken } ?: it.authToken
            val resolvedCustomerId = session?.customerId?.takeIf { it.isNotBlank() }
                ?: it.customerId.ifBlank { buildFallbackCustomerId(customerWhatsapp) }
            it.copy(
                authToken = resolvedToken,
                isStationConnected = resolvedToken.isNotBlank(),
                customerId = resolvedCustomerId,
                customerWhatsapp = customerWhatsapp,
                selectedEventId = selectedEventId.ifBlank { it.selectedEventId },
                sessionId = session?.sessionId,
                sessionCode = session?.sessionCode,
                uploadUrl = session?.uploadUrl,
                paymentStatus = session?.paymentStatus,
                paymentRequired = session?.paymentRequired,
                unlockPhoto = session?.unlockPhoto,
            )
        }
    }

    fun clearSession() {
        _state.update {
            it.copy(
                customerId = "",
                customerWhatsapp = "",
                sessionId = null,
                sessionCode = null,
                uploadUrl = null,
                paymentStatus = null,
                paymentRequired = null,
                unlockPhoto = null,
            )
        }
    }

    private fun buildFallbackCustomerId(customerWhatsapp: String = ""): String {
        val digits = customerWhatsapp.filter(Char::isDigit)
        return when {
            digits.isNotBlank() -> "CUST-$digits"
            else -> "CUST-DEFAULT"
        }
    }
}
