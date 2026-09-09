package com.fluida.currencyflow.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
class SettingsManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val DECIMAL_PLACES_KEY = intPreferencesKey("decimal_places_key")

    private val _decimalPlaces = MutableStateFlow(4)
    val decimalPlaces: StateFlow<Int> = _decimalPlaces.asStateFlow()

    init {
        scope.launch {
            dataStore.data
                .map { it[DECIMAL_PLACES_KEY] ?: 4 }
                .collect { savedValue ->
                    _decimalPlaces.value = savedValue
                }
        }
    }

    fun setDecimalPlaces(places: Int) {
        _decimalPlaces.value = places
        scope.launch {
            dataStore.edit { it[DECIMAL_PLACES_KEY] = places }
        }
    }
}
