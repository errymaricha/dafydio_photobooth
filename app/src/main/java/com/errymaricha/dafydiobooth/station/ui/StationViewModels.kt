package com.errymaricha.dafydiobooth.station.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.errymaricha.dafydiobooth.station.local.TemplateEntity
import com.errymaricha.dafydiobooth.station.network.AppError
import com.errymaricha.dafydiobooth.station.network.AppResult
import com.errymaricha.dafydiobooth.station.repository.AuthRepository
import com.errymaricha.dafydiobooth.station.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(deviceCode: String, apiKey: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.login(deviceCode, apiKey)) {
                is AppResult.Success -> _state.value = LoginUiState(isLoggedIn = true)
                is AppResult.Failure -> _state.value = LoginUiState(errorMessage = result.error.toUiMessage())
            }
        }
    }
}

data class TemplateListUiState(
    val isLoading: Boolean = false,
    val templates: List<TemplateEntity> = emptyList(),
    val errorMessage: String? = null,
)

class TemplateListViewModel(
    private val templateRepository: TemplateRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TemplateListUiState())
    val state: StateFlow<TemplateListUiState> = _state.asStateFlow()

    init {
        templateRepository.observeTemplates()
            .onEach { items -> _state.value = _state.value.copy(templates = items) }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = templateRepository.refreshTemplates()) {
                is AppResult.Success -> _state.value = _state.value.copy(isLoading = false)
                is AppResult.Failure -> _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = result.error.toUiMessage(),
                )
            }
        }
    }
}

private fun AppError.toUiMessage(): String {
    return when (this) {
        AppError.Unauthorized -> "Unauthorized (401): silakan login ulang"
        AppError.Forbidden -> "Forbidden (403): device tidak diizinkan"
        is AppError.Validation -> "Validation (422): $message"
        is AppError.Server -> "Server error (5xx): $message"
        is AppError.Network -> "Network error: $message"
        is AppError.Unknown -> "Unknown error: $message"
    }
}
