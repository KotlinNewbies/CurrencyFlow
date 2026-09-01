package com.fluida.currencyflow.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val API_KEY = stringPreferencesKey("api_key")
    private val USERNAME = stringPreferencesKey("username")
    private val IS_PREMIUM = booleanPreferencesKey("is_premium")

    private val _apiKey = MutableStateFlow<String?>(null)
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[API_KEY] != null }

    init {
        scope.launch {
            val prefs = dataStore.data.first()
            _apiKey.value = prefs[API_KEY]
            _username.value = prefs[USERNAME]
            _isPremium.value = prefs[IS_PREMIUM] ?: false
        }
    }

    fun saveAuthData(username: String, apiKey: String, isPremium: Boolean) {
        _username.value = username
        _apiKey.value = apiKey
        _isPremium.value = isPremium
        scope.launch {
            dataStore.edit {
                it[USERNAME] = username
                it[API_KEY] = apiKey
                it[IS_PREMIUM] = isPremium
            }
        }
    }

    fun updatePremiumStatus(isPremium: Boolean) {
        if (_isPremium.value != isPremium) {
            _isPremium.value = isPremium
            scope.launch {
                dataStore.edit {
                    it[IS_PREMIUM] = isPremium
                }
            }
        }
    }

    fun clearAuthData() {
        _username.value = null
        _apiKey.value = null
        _isPremium.value = false
        scope.launch {
            dataStore.edit {
                it.remove(USERNAME)
                it.remove(API_KEY)
                it.remove(IS_PREMIUM)
            }
        }
    }
}
