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
class TutorialManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TUTORIAL_SEEN_KEY = booleanPreferencesKey("tutorial_seen_key")

    private val _isTutorialSeen = MutableStateFlow(true) // Domyślnie true, dopóki nie wczytamy
    val isTutorialSeen: StateFlow<Boolean> = _isTutorialSeen.asStateFlow()

    init {
        scope.launch {
            dataStore.data
                .map { it[TUTORIAL_SEEN_KEY] ?: false }
                .collect { savedState ->
                    _isTutorialSeen.value = savedState
                }
        }
    }

    fun setTutorialSeen(seen: Boolean) {
        _isTutorialSeen.value = seen
        scope.launch {
            dataStore.edit { it[TUTORIAL_SEEN_KEY] = seen }
        }
    }
}
