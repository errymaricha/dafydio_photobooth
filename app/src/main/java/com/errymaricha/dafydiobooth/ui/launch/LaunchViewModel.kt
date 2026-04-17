package com.errymaricha.dafydiobooth.ui.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.errymaricha.dafydiobooth.domain.usecase.CalculateFinalAmountUseCase
import com.errymaricha.dafydiobooth.domain.usecase.OpenManualSessionUseCase
import com.errymaricha.dafydiobooth.domain.usecase.PrepareLaunchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LaunchViewModel(
    private val prepareLaunch: PrepareLaunchUseCase,
    private val openManualSession: OpenManualSessionUseCase,
    private val calculateFinalAmount: CalculateFinalAmountUseCase,
) : ViewModel() {
    private val _ui = MutableStateFlow(LaunchUiState())
    val ui: StateFlow<LaunchUiState> = _ui.asStateFlow()

    fun onWhatsappChanged(value: String) {
        _ui.update {
            it.copy(
                customerWhatsapp = value.filter { char -> char.isDigit() || char == '+' },
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
                error = null,
            )
        }
    }

    fun init(deviceCode: String, apiKey: String) {
        viewModelScope.launch {
            runCatching {
                _ui.update { it.copy(loading = true, error = null, message = null) }
                val (token, pricing) = prepareLaunch(deviceCode, apiKey)
                val total = calculateFinalAmount(
                    pricing.photoboothPrice,
                    pricing.additionalPrintPrice,
                    _ui.value.additionalPrintCount,
                )
                _ui.update {
                    it.copy(
                        loading = false,
                        token = token,
                        pricing = pricing,
                        finalAmount = total,
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

    fun submitManualPaymentRequest() {
        viewModelScope.launch {
            val current = _ui.value
            val token = current.token
            if (token.isNullOrBlank()) {
                _ui.update { it.copy(error = "Token station belum tersedia") }
                return@launch
            }
            if (current.customerWhatsapp.isBlank()) {
                _ui.update { it.copy(error = "No WA wajib diisi") }
                return@launch
            }

            runCatching {
                _ui.update { it.copy(loading = true, error = null, message = null) }
                openManualSession(
                    token = token,
                    customerWhatsapp = current.customerWhatsapp,
                    additionalPrintCount = current.additionalPrintCount,
                )
            }.onSuccess { session ->
                _ui.update {
                    it.copy(
                        loading = false,
                        message = "Open session terkirim (${session.sessionCode}). Menunggu approve station.",
                    )
                }
            }.onFailure { error ->
                _ui.update {
                    it.copy(
                        loading = false,
                        error = error.message ?: "Gagal kirim request manual",
                    )
                }
            }
        }
    }
}
