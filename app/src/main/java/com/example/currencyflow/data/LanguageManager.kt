// LanguageManager.kt
package com.example.currencyflow.data

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.currencyflow.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

object PreferenceKeys {
    val APP_LANGUAGE_TAG_DS = stringPreferencesKey("app_language_tag_ds")

}

data class LanguageOption(
    val tag: String,
    val displayNameResId: Int
)

@Singleton
class LanguageManager @Inject constructor(
    private val appSettingsDataStore: DataStore<Preferences>
) {
    private val TAG = "LanguageManager"
    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // StateFlow sygnalizujący, czy początkowe ładowanie języka zostało zakończone
    private val _initialLanguageLoaded = MutableStateFlow(false)
    val initialLanguageLoaded: StateFlow<Boolean> = _initialLanguageLoaded.asStateFlow()

    // StateFlow przechowujący aktualny tag języka; null oznacza, że nie został jeszcze załadowany
    // lub nie jest ustawiony. Pusty string "" będzie oznaczał "język systemowy".
    private val _currentLanguageTag = MutableStateFlow<String?>(null)
    val currentLanguageTagFlow: StateFlow<String?> = _currentLanguageTag.asStateFlow()

    init {
        Log.d(TAG, "LanguageManager INIT block started.")
        // Asynchroniczne ładowanie języka z DataStore
        managerScope.launch {
            try {
                // Odczytaj język z DataStore.
                // Jeśli klucz nie istnieje, mapowanie zwróci null, a ?: "" zapewni pusty string.
                val langFromDataStore = appSettingsDataStore.data
                    .map { preferences -> preferences[PreferenceKeys.APP_LANGUAGE_TAG_DS] ?: "" }
                    .first() // Odczytaj pierwszą wartość

                Log.d(TAG, "INIT: Language successfully loaded from DataStore: '$langFromDataStore'")
                _currentLanguageTag.value = langFromDataStore
            } catch (e: Exception) {
                Log.e(TAG, "INIT: Error loading language from DataStore", e)
                // W przypadku błędu, ustawiamy język na systemowy (pusty string)
                _currentLanguageTag.value = ""
            } finally {
                // Niezależnie od wyniku, oznaczamy, że proces ładowania się zakończył
                _initialLanguageLoaded.value = true
                Log.d(TAG, "INIT: Initial language loading process finished. Loaded: ${_initialLanguageLoaded.value}, Lang: '${_currentLanguageTag.value}'")
            }
        }
        Log.d(TAG, "LanguageManager INIT block finished. Async language load launched.")
    }

    // Publiczny dostęp do StateFlow został zmieniony na currentLanguageTagFlow typu StateFlow<String?>

    fun applyPersistedLanguageToSystem() {
        // Odczytujemy wartość. Może być null, jeśli ładowanie jeszcze trwa lub wystąpił błąd przed ustawieniem wartości.
        // Pusty string "" oznacza język systemowy.
        val currentTag = _currentLanguageTag.value ?: "" // Jeśli null (mało prawdopodobne po zakończeniu init), użyj systemowego
        Log.d(TAG, "applyPersistedLanguageToSystem called with tag: '$currentTag'")
        applyLocaleToSystem(currentTag) // applyLocaleToSystem już obsługuje pusty string
    }

    private fun applyLocaleToSystem(languageTag: String) { // Podpis metody bez zmian
        val localeList = if (languageTag.isNotEmpty()) {
            LocaleListCompat.forLanguageTags(languageTag)
        } else {
            // Pusty tag oznacza użycie języka systemowego
            LocaleListCompat.getEmptyLocaleList()
        }
        Log.d(TAG, "Setting application locales with: ${localeList.toLanguageTags()} (input languageTag: '$languageTag')")
        AppCompatDelegate.setApplicationLocales(localeList)
        Log.d(TAG, "AppCompatDelegate.setApplicationLocales called with tag: '$languageTag'.")
    }

    suspend fun setApplicationLanguage(languageTag: String) {
        val oldLang = _currentLanguageTag.value
        // Porównujemy z nowym tagiem. Jeśli oldLang jest null, a languageTag nie, to jest zmiana.
        if (oldLang == languageTag) {
            Log.d(TAG, "Language is already set to '$languageTag'. No change needed.")
            return
        }

        Log.i(TAG, "Attempting to set application language to: '$languageTag' (was '$oldLang')")
        try {
            appSettingsDataStore.edit { settings ->
                settings[PreferenceKeys.APP_LANGUAGE_TAG_DS] = languageTag
            }
            // Bezpośrednio aktualizujemy StateFlow, aby UI zareagowało natychmiast
            _currentLanguageTag.value = languageTag
            Log.i(TAG, "Successfully SAVED language tag '$languageTag' to DataStore and updated local StateFlow.")
        } catch (e: Exception) {
            Log.e(TAG, "Error SAVING language tag '$languageTag' to DataStore", e)
            // Można rozważyć logikę przywrócenia `_currentLanguageTag.value = oldLang` w przypadku błędu zapisu,
            // ale to komplikuje, jeśli strumień z DataStore miałby też aktualizować ten StateFlow.
            // Na razie zakładamy, że zapis się powiedzie lub błąd jest obsługiwany przez logowanie.
        }
    }

    fun getContextWithLocale(baseContext: Context): Context {
        // Odczytujemy aktualną wartość. Może być null, jeśli init jeszcze nie zakończył ładowania.
        // Jeśli jest null lub pusty, używamy języka systemowego.
        val languageTagToApply = _currentLanguageTag.value ?: ""
        val targetLocale: Locale

        if (languageTagToApply.isNotEmpty()) {
            targetLocale = Locale.forLanguageTag(languageTagToApply)
        } else {
            // Język systemowy, jeśli tag jest pusty (lub był null i został zamieniony na "")
            targetLocale = ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
                ?: Locale.getDefault()
        }

        Log.d(TAG, "getContextWithLocale: Applying Locale: '${targetLocale.toLanguageTag()}' (from StateFlow tag: '$languageTagToApply')")

        val configuration = Configuration(baseContext.resources.configuration)
        configuration.setLocale(targetLocale)
        // configuration.setLayoutDirection(targetLocale) // Dla RTL, jeśli potrzebne

        return baseContext.createConfigurationContext(configuration)
    }

    fun getAvailableLanguages(): List<LanguageOption> { // Bez zmian
        return listOf(
            LanguageOption(tag = "", displayNameResId = R.string.language_system_default), // Pusty tag dla języka systemowego
            LanguageOption(tag = "de", displayNameResId = R.string.lang_display_de),
            LanguageOption(tag = "en", displayNameResId = R.string.lang_display_en),
            LanguageOption(tag = "es", displayNameResId = R.string.lang_display_es),
            LanguageOption(tag = "fr", displayNameResId = R.string.lang_display_fr),
            LanguageOption(tag = "it", displayNameResId = R.string.lang_display_it),
            LanguageOption(tag = "pl", displayNameResId = R.string.lang_display_pl)
        )
    }
}