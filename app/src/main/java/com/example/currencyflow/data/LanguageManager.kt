// LanguageManager.kt
package com.example.currencyflow.data // Lub odpowiedni pakiet, np. com.example.currencyflow.data.repository

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.currencyflow.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// Obiekt do przechowywania kluczy preferencji, aby uniknąć literówek.
object PreferenceKeys { // Ten obiekt może pozostać, jeśli klucz jest używany też w innych miejscach
    val APP_LANGUAGE_TAG = stringPreferencesKey("app_language_tag")
}

data class LanguageOption(
    val tag: String,
    val displayNameResId: Int
)

@Singleton
class LanguageManager @Inject constructor(
    private val appSettingsDataStore: DataStore<Preferences> // DataStore wstrzykiwany przez Hilt
) {
    private val TAG = "LanguageManager"

    // Zmienna do przechowywania ostatnio ustawionego/odczytanego języka (tagu)
    // Inicjalizowana leniwie przy pierwszym dostępie lub w init
    private var currentPersistedLanguageTag: String = "" // Inicjalizuj pustym stringiem (dla "System Default")
    private var isInitialized = false

    val currentLanguageTagFlow: Flow<String> = appSettingsDataStore.data
        .catch { exception ->
            Log.e(TAG, "Error reading language tag from DataStore", exception)
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferenceKeys.APP_LANGUAGE_TAG] ?: "" // Domyślnie pusty string
        }

    init {
        runBlocking {
            initializeCurrentLanguageFromDataStore()
        }
    }

    private suspend fun initializeCurrentLanguageFromDataStore() {
        if (!isInitialized) {
            currentPersistedLanguageTag = appSettingsDataStore.data
                .map { preferences ->
                    preferences[PreferenceKeys.APP_LANGUAGE_TAG] ?: ""
                }
                .catch { exception ->
                    Log.e(TAG, "Error initializing language from DataStore", exception)
                    emit("") // W razie błędu, użyj domyślnego (pusty string)
                }
                .first() // Pobierz pierwszą (i jedyną) wartość przy inicjalizacji
            Log.d(TAG, "Initialized currentPersistedLanguageTag: '$currentPersistedLanguageTag'")
            isInitialized = true
        }
    }

    // Publiczna metoda do synchronicznego pobrania zapisanego języka
    private fun getInitializedPersistedLanguageTag(): String {
        // Teoretycznie `isInitialized` powinno być zawsze true po bloku init,
        // ale można dodać dodatkowe zabezpieczenie/logowanie, jeśli potrzebne.
        if (!isInitialized) {
            Log.w(TAG, "getInitializedPersistedLanguageTag called when not initialized! This shouldn't happen.")
            // Można by tu spróbować ponownej synchronicznej inicjalizacji, ale to wskazuje na problem.
        }
        return currentPersistedLanguageTag
    }

    suspend fun setApplicationLanguage(languageTag: String) {
        Log.d(TAG, "Attempting to set application language to: '$languageTag'")
        val oldLang = currentPersistedLanguageTag
        currentPersistedLanguageTag = languageTag // Zaktualizuj wewnętrzny stan natychmiast

        try {
            appSettingsDataStore.edit { settings ->
                settings[PreferenceKeys.APP_LANGUAGE_TAG] = languageTag
            }
            Log.d(TAG, "Successfully saved language tag '$languageTag' to DataStore")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving language tag '$languageTag' to DataStore", e)
        }

        applyLocaleToSystem(languageTag)

        if (oldLang != currentPersistedLanguageTag) {
            Log.i(TAG, "Language changed from '$oldLang' to '$currentPersistedLanguageTag'. Activity recreate is needed.")
        }
    }

    private fun applyLocaleToSystem(languageTag: String) {
        val localeList = if (languageTag.isNotEmpty()) {
            LocaleListCompat.forLanguageTags(languageTag)
        } else {
            // Pusty tag oznacza "System Default"
            LocaleListCompat.getEmptyLocaleList()
            // Alternatywnie, jeśli chcesz jawnie ustawić locale systemu:
            // LocaleListCompat.create(ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0])
        }
        Log.d(TAG, "Setting application locales with: ${localeList.toLanguageTags()} (input tag: '$languageTag')")
        AppCompatDelegate.setApplicationLocales(localeList)
        Log.d(TAG, "AppCompatDelegate.setApplicationLocales called.")
    }

    /**
     * Tworzy i zwraca nowy kontekst z językiem ustawionym na podstawie zapisanego tagu.
     * Jeśli żaden język nie jest zapisany (lub tag jest pusty), używa języka systemowego.
     */
    fun getContextWithLocale(baseContext: Context): Context {
        val languageTagToApply = getInitializedPersistedLanguageTag()

        val targetLocale: Locale = if (languageTagToApply.isNotEmpty()) {
            Locale.forLanguageTag(languageTagToApply)
        } else {
            // Użyj głównego języka systemowego
            ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
                ?: Locale.getDefault() // Fallback, chociaż getLocales[0] powinno działać
        }

        Log.d(TAG, "getContextWithLocale: Applying Locale: '${targetLocale.toLanguageTag()}' (from tag: '$languageTagToApply')")

        val configuration = Configuration(baseContext.resources.configuration)

        val localeList = LocaleList(targetLocale)
        // Nie wywołuj LocaleList.setDefault(localeList) tutaj,
        // setDefault zmienia globalny stan JVM, co może mieć niepożądane efekty uboczne.
        // Lepiej jest skonfigurować tylko kontekst.
        configuration.setLocales(localeList)
        // Opcjonalnie: Ustaw kierunek layoutu, jeśli wspierasz języki RTL
        // configuration.setLayoutDirection(targetLocale)

        return baseContext.createConfigurationContext(configuration)
    }

    fun getAvailableLanguages(): List<LanguageOption> {
        return listOf(
            LanguageOption(tag = "", displayNameResId = R.string.language_system_default), // Dla "System Default"
            LanguageOption(tag = "en", displayNameResId = R.string.lang_display_en),      // Użyj R.string.lang_display_en
            LanguageOption(tag = "pl", displayNameResId = R.string.lang_display_pl),       // Użyj R.string.lang_display_pl
            LanguageOption(tag = "de", displayNameResId = R.string.lang_display_de),

        )
    }
}