// LanguageManager.kt
// Lokalizacja: np. app/src/main/java/com/example/currencyflow/data/LanguageManager.kt
// Pamiętaj, aby dostosować deklarację pakietu poniżej do rzeczywistej lokalizacji pliku.
package com.example.currencyflow.data // Lub np. com.example.currencyflow.common

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.currencyflow.R // Upewnij się, że R jest poprawnie zaimportowane z Twojego głównego pakietu
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Definicja DataStore na poziomie pliku (rozszerzenie dla Context).
// Użyj unikalnej nazwy dla swojego DataStore, aby uniknąć konfliktów, jeśli masz inne.
private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings_preferences")

// Obiekt do przechowywania kluczy preferencji, aby uniknąć literówek.
object PreferenceKeys {
    val APP_LANGUAGE_TAG = stringPreferencesKey("app_language_tag")
}

/**
 * Data class reprezentująca opcję językową w UI.
 * @property tag Kod języka (np. "en", "pl") lub pusty string dla opcji "System Default".
 * @property displayNameResId ID zasobu string dla nazwy języka wyświetlanej użytkownikowi.
 */
data class LanguageOption(
    val tag: String,
    val displayNameResId: Int
)

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "LanguageManager" // Tag dla logów

    val currentLanguageTagFlow: Flow<String> = context.appSettingsDataStore.data
        .catch { exception ->
            Log.e(TAG, "Error reading language tag from DataStore", exception) // Log błędu
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val langTag = preferences[PreferenceKeys.APP_LANGUAGE_TAG] ?: ""
            Log.d(TAG, "Read language tag from DataStore: '$langTag'") // Log odczytu
            langTag
        }

    suspend fun setApplicationLanguage(languageTag: String) {
        Log.d(TAG, "Attempting to set application language to: '$languageTag'") // Log na początku
        try {
            context.appSettingsDataStore.edit { settings ->
                settings[PreferenceKeys.APP_LANGUAGE_TAG] = languageTag
            }
            Log.d(TAG, "Successfully saved language tag '$languageTag' to DataStore") // Log po zapisie
        } catch (e: Exception) {
            Log.e(TAG, "Error saving language tag '$languageTag' to DataStore", e) // Log błędu zapisu
            // Możesz chcieć rzucić wyjątek dalej lub obsłużyć go inaczej
        }

        val localeList = if (languageTag.isNotEmpty()) {
            LocaleListCompat.forLanguageTags(languageTag)
        } else {
            LocaleListCompat.getEmptyLocaleList()
        }
        Log.d(TAG, "Setting application locales with: ${localeList.toLanguageTags()} (input tag: '$languageTag')") // Log przed setApplicationLocales
        AppCompatDelegate.setApplicationLocales(localeList)
        Log.d(TAG, "AppCompatDelegate.setApplicationLocales called.") // Log po wywołaniu
    }

    /**
     * Zwraca listę dostępnych opcji językowych, które użytkownik może wybrać.
     * Każda opcja zawiera tag języka i ID zasobu string dla jego nazwy.
     */
    fun getAvailableLanguages(): List<LanguageOption> {
        return listOf(
            LanguageOption("", R.string.language_system_default), // Opcja "System Default"
            LanguageOption("en", R.string.language_english),      // Angielski
            LanguageOption("pl", R.string.language_polish)       // Polski
            // Możesz dodać więcej języków tutaj, jeśli je wspierasz, np.:
            // LanguageOption("de", R.string.language_german)
        )
    }
}