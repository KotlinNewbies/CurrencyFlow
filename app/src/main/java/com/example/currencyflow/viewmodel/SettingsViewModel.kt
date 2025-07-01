package com.example.currencyflow.viewmodel

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyflow.data.LanguageManager // Zaimportuj swój LanguageManager
import com.example.currencyflow.data.LanguageOption // Zaimportuj LanguageOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager
) : ViewModel() {

    val availableLanguages: List<LanguageOption> = languageManager.getAvailableLanguages()

    // currentLanguageTagFlow z LanguageManager jest teraz StateFlow<String?>
    // Musimy dostosować, jak inicjalizujemy currentLanguageTag w ViewModel.
    // Chcemy, aby UI nadal widziało String (np. pusty string dla języka systemowego/niezaładowanego).
    val currentLanguageTag: StateFlow<String> = languageManager.currentLanguageTagFlow
        .map { nullableLanguageTag ->
            nullableLanguageTag ?: "" // Zamień null na pusty string
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            // Wartość początkowa: odczytaj z languageManager.currentLanguageTagFlow i zamień null na ""
            // Jeśli currentLanguageTagFlow.value jest null na starcie, to initialValue będzie "".
            initialValue = languageManager.currentLanguageTagFlow.value ?: ""
        )

    // Możesz również chcieć udostępnić oryginalny nullowalny StateFlow, jeśli jakaś logika
    // specyficznie potrzebuje rozróżnienia między "niezaładowany" (null) a "systemowy" ("").
    // val rawCurrentLanguageTag: StateFlow<String?> = languageManager.currentLanguageTagFlow

    fun changeLanguage(languageTag: String, activity: ComponentActivity) {
        Log.d("SettingsViewModel", "UI wants to change language to: '$languageTag'")
        // Porównujemy z wartością z naszego zmapowanego currentLanguageTag, który jest już String
        if (currentLanguageTag.value == languageTag) {
            Log.d("SettingsViewModel", "Language is already '$languageTag'. No action taken.")
            return
        }

        viewModelScope.launch {
            Log.d("SettingsViewModel", "Calling languageManager.setApplicationLanguage with '$languageTag'")
            languageManager.setApplicationLanguage(languageTag) // To zapisze do DataStore

            // Po zapisie, LanguageManager wewnętrznie zaktualizuje swój _currentLanguageTag.value.
            // Nasz `currentLanguageTag` (StateFlow<String>) w ViewModelu automatycznie
            // otrzyma tę aktualizację dzięki `map` i `stateIn` z `languageManager.currentLanguageTagFlow`.

            // `applyPersistedLanguageToSystem` użyje najnowszej wartości z LanguageManager.
            Log.d("SettingsViewModel", "Immediately applying persisted language to system via LanguageManager before recreate.")
            languageManager.applyPersistedLanguageToSystem()

            Log.d("SettingsViewModel", "Calling activity.recreate() to apply language change to UI.")
            activity.recreate()
        }
    }
}