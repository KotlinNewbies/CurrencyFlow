package com.fluida.currencyflow.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val ADS_ENABLED_KEY = booleanPreferencesKey("ads_enabled_key")

    private val _adsEnabled = MutableStateFlow(true)
    val adsEnabled: StateFlow<Boolean> = _adsEnabled.asStateFlow()

    init {
        // Przy starcie natychmiast wczytaj ostatni znany stan z pamięci
        scope.launch {
            val savedState = dataStore.data
                .map { it[ADS_ENABLED_KEY] ?: true }
                .first()
            _adsEnabled.value = savedState
        }
    }

    fun setAdsEnabled(enabled: Boolean) {
        if (_adsEnabled.value != enabled) {
            _adsEnabled.value = enabled
            // Zapisz stan na przyszłość
            scope.launch {
                dataStore.edit { it[ADS_ENABLED_KEY] = enabled }
            }
        }
    }
}
