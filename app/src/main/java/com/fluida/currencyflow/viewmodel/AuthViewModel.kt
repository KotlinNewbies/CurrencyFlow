package com.fluida.currencyflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluida.currencyflow.data.AuthManager
import com.fluida.currencyflow.data.repository.AuthRepository
import com.fluida.currencyflow.data.repository.UserDataRepository
import com.fluida.currencyflow.util.UiText
import com.fluida.currencyflow.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val deviceId = userDataRepository.getUserDataModel().id
            val result = authRepository.register(username, password, deviceId)
            
            result.onSuccess { response ->
                if (response.rcSuccess && response.api_key != null) {
                    authManager.saveAuthData(username, response.api_key)
                    _uiState.value = AuthUiState.Success(UiText.DynamicString(response.message))
                } else {
                    _uiState.value = AuthUiState.Error(UiText.DynamicString(response.message))
                }
            }.onFailure {
                _uiState.value = AuthUiState.Error(UiText.DynamicString(it.message ?: "Unknown error"))
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(username, password)
            
            result.onSuccess { response ->
                if (response.rcSuccess && response.api_key != null) {
                    authManager.saveAuthData(username, response.api_key)
                    _uiState.value = AuthUiState.Success(UiText.DynamicString(response.message))
                } else {
                    // Ujednolicony komunikat o błędnych danych logowania
                    _uiState.value = AuthUiState.Error(UiText.StringResource(R.string.login_error_invalid_credentials))
                }
            }.onFailure {
                _uiState.value = AuthUiState.Error(UiText.DynamicString(it.message ?: "Unknown error"))
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: UiText) : AuthUiState()
    data class Error(val message: UiText) : AuthUiState()
}
