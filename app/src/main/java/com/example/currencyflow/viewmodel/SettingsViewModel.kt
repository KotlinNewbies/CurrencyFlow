package com.example.currencyflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyflow.data.LanguageManager // Zaimportuj swój LanguageManager
import com.example.currencyflow.data.LanguageOption // Zaimportuj LanguageOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // Adnotacja Hilt dla ViewModeli
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager // Wstrzyknięcie LanguageManager przez Hilt
) : ViewModel() {

    // Lista dostępnych opcji językowych, pobierana bezpośrednio z LanguageManager.
    // UI użyje tej listy do wyświetlenia opcji wyboru.
    val availableLanguages: List<LanguageOption> = languageManager.getAvailableLanguages()

    // StateFlow reprezentujący aktualnie wybrany/zapisany tag języka.
    // Jest on obserwowany z currentLanguageTagFlow w LanguageManager.
    // UI będzie obserwować ten stan, aby np. zaznaczyć aktywny język na liście.
    val currentLanguageTag: StateFlow<String> = languageManager.currentLanguageTagFlow
        .stateIn(
            scope = viewModelScope, // Zakres korutyny powiązany z cyklem życia ViewModelu
            started = SharingStarted.WhileSubscribed(5000L), // Rozpoczyna i zatrzymuje zbieranie danych, gdy są subskrybenci
            initialValue = "" // Wartość początkowa, pusty string dla "System Default"
        )

    /**
     * Funkcja wywoływana z UI (np. po kliknięciu opcji języka przez użytkownika)
     * w celu zmiany języka aplikacji.
     *
     * @param languageTag Tag języka do ustawienia (np. "en", "pl", lub "" dla "System Default").
     */
    fun changeLanguage(languageTag: String) {
        // Uruchomienie operacji zmiany języka w korutynie w zakresie ViewModelu.
        viewModelScope.launch {
            languageManager.setApplicationLanguage(languageTag)
            // Po wywołaniu setApplicationLanguage, LanguageManager zapisze preferencję
            // w DataStore i użyje AppCompatDelegate do zmiany języka aplikacji.
            // AppCompatDelegate zazwyczaj odtworzy Aktywność, co spowoduje
            // odświeżenie UI z nowymi zasobami językowymi.
        }
    }
}