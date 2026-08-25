package com.fluida.currencyflow.viewmodel

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluida.currencyflow.data.LanguageManager
import com.fluida.currencyflow.data.LanguageOption
import com.fluida.currencyflow.data.SettingsManager
import com.fluida.currencyflow.data.TutorialManager
import com.fluida.currencyflow.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val userDataRepository: UserDataRepository,
    private val tutorialManager: TutorialManager,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    init {
        viewModelScope.launch {
            _userId.value = userDataRepository.getUserDataModel().id
        }
    }

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

    val decimalPlaces: StateFlow<Int> = settingsManager.decimalPlaces

    fun setDecimalPlaces(places: Int) {
        settingsManager.setDecimalPlaces(places)
    }

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

    fun resetTutorial() {
        tutorialManager.setTutorialSeen(false)
    }
}
