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
    val currentLanguageTag: StateFlow<String> = languageManager.currentLanguageTagFlow
        .map { nullableLanguageTag ->
            nullableLanguageTag ?: "" // Zamień null na pusty string
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = languageManager.currentLanguageTagFlow.value ?: ""
        )

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
            Log.d("SettingsViewModel", "Immediately applying persisted language to system via LanguageManager before recreate.")
            languageManager.applyPersistedLanguageToSystem()

            Log.d("SettingsViewModel", "Calling activity.recreate() to apply language change to UI.")
            activity.recreate()
        }
    }
}