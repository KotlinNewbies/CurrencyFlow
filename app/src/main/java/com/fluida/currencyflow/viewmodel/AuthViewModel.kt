package com.fluida.currencyflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluida.currencyflow.data.AuthManager
import com.fluida.currencyflow.data.PremiumManager
import com.fluida.currencyflow.data.SyncManager
import com.fluida.currencyflow.data.repository.AuthRepository
import com.fluida.currencyflow.data.repository.UserDataRepository
import com.fluida.currencyflow.util.UiText
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
    private val userDataRepository: UserDataRepository,
    private val premiumManager: PremiumManager,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(firstName: String, lastName: String, email: String, phone: String, password: String, isPremium: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val deviceId = userDataRepository.getUserDataModel().id
            val result = authRepository.register(firstName, lastName, email, phone, password, deviceId, isPremium)
            
            result.onSuccess { response ->
                if (response.rcSuccess) {
                    _uiState.value = AuthUiState.Success(UiText.DynamicString(response.message))
                } else {
                    _uiState.value = AuthUiState.Error(UiText.DynamicString(response.message))
                }
            }.onFailure {
                _uiState.value = AuthUiState.Error(UiText.DynamicString(it.message ?: "Unknown error"))
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(email, password)
            
            result.onSuccess { response ->
                if (response.rcSuccess && response.api_key != null) {
                    // UWAGA: Nie nadpisujemy już UUID urządzenia UUID z serwera, 
                    // aby urządzenia pozostały unikalne.
                    
                    authManager.saveAuthData(email, response.first_name, response.api_key, response.is_premium)
                    premiumManager.setAdsEnabled(!response.is_premium)
                    
                    // Jeśli użytkownik ma Premium, spróbuj pobrać i zaaplikować backup
                    if (response.is_premium) {
                        syncManager.downloadAndApplyBackup()
                    }
                    
                    _uiState.value = AuthUiState.Success(UiText.DynamicString(response.message))
                } else {
                    _uiState.value = AuthUiState.Error(UiText.DynamicString(response.message))
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
