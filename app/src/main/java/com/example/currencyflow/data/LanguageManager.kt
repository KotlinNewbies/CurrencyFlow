// LanguageManager.kt
package com.example.currencyflow.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

object PreferenceKeys {
    val APP_LANGUAGE_TAG_DS = stringPreferencesKey("app_language_tag_ds") // DS for DataStore
    const val APP_LANGUAGE_TAG_SP = "app_language_tag_sp" // SP for SharedPreferences
    const val SHARED_PREFS_NAME = "language_prefs"
}

data class LanguageOption(
    val tag: String,
    val displayNameResId: Int
)

@Singleton
class LanguageManager @Inject constructor(
    context: Context, // Potrzebujemy kontekstu dla SharedPreferences
    private val appSettingsDataStore: DataStore<Preferences>
) {
    private val TAG = "LanguageManager"
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // SharedPreferences do szybkiego synchronicznego odczytu przy starcie
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PreferenceKeys.SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    // Przechowuje ostatni odczytany/ustawiony tag języka.
    // Inicjalizowany synchronicznie z SharedPreferences.
    private var currentPersistedLanguageTag: String

    init {
        // Synchronous initialization from SharedPreferences for immediate use
        currentPersistedLanguageTag = sharedPreferences.getString(PreferenceKeys.APP_LANGUAGE_TAG_SP, "") ?: ""
        Log.d(TAG, "Initialized currentPersistedLanguageTag from SharedPreferences: '$currentPersistedLanguageTag'")

        // Upewnij się, że DataStore jest zsynchronizowany z SharedPreferences, jeśli to pierwsze uruchomienie
        // lub jeśli SharedPreferences ma wartość, a DataStore nie.
        // To jest bardziej dla spójności, jeśli aplikacja była używana przed tą zmianą.
        coroutineScope.launch {
            val dataStoreLang = appSettingsDataStore.data
                .map { preferences -> preferences[PreferenceKeys.APP_LANGUAGE_TAG_DS] ?: "" }
                .first()
            if (currentPersistedLanguageTag.isNotEmpty() && dataStoreLang != currentPersistedLanguageTag) {
                appSettingsDataStore.edit { settings ->
                    settings[PreferenceKeys.APP_LANGUAGE_TAG_DS] = currentPersistedLanguageTag
                }
                Log.d(TAG, "Synchronized DataStore with SharedPreferences language: '$currentPersistedLanguageTag'")
            } else if (currentPersistedLanguageTag.isEmpty() && dataStoreLang.isNotEmpty()) {
                // If SP is empty but DS has a value (e.g. after clear data and restore from DS backup)
                // This scenario is less likely with the new approach but good for robustness
                currentPersistedLanguageTag = dataStoreLang
                sharedPreferences.edit().putString(PreferenceKeys.APP_LANGUAGE_TAG_SP, dataStoreLang).apply()
                Log.d(TAG, "Synchronized SharedPreferences from DataStore language: '$dataStoreLang'")
            }
        }
    }

    // Flow z DataStore do obserwacji zmian w reszcie aplikacji (np. w UI)
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
            preferences[PreferenceKeys.APP_LANGUAGE_TAG_DS] ?: "" // Odczytuj z DataStore
        }
        // Możesz użyć stateIn, aby Flow miał zawsze najnowszą wartość i nie wymagał ponownego odczytu
        // dla każdego nowego kolektora, jeśli jest to potrzebne.
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), currentPersistedLanguageTag)


    // Ta metoda jest teraz bardzo lekka, bo odczytuje już zainicjalizowaną wartość
    private fun getInitializedPersistedLanguageTag(): String {
        return currentPersistedLanguageTag
    }

    suspend fun setApplicationLanguage(languageTag: String) {
        Log.d(TAG, "Attempting to set application language to: '$languageTag'")
        val oldLang = currentPersistedLanguageTag
        currentPersistedLanguageTag = languageTag // Update internal state immediately

        // Zapisz do SharedPreferences (synchronicznie, ale apply() jest asynchroniczne na dysku)
        sharedPreferences.edit().putString(PreferenceKeys.APP_LANGUAGE_TAG_SP, languageTag).apply()
        Log.d(TAG, "Successfully saved language tag '$languageTag' to SharedPreferences")

        // Zapisz do DataStore (asynchronicznie)
        try {
            appSettingsDataStore.edit { settings ->
                settings[PreferenceKeys.APP_LANGUAGE_TAG_DS] = languageTag
            }
            Log.d(TAG, "Successfully saved language tag '$languageTag' to DataStore")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving language tag '$languageTag' to DataStore", e)
            // Rozważ logikę błędu, np. przywrócenie currentPersistedLanguageTag
        }

        applyLocaleToSystem(languageTag)

        if (oldLang != currentPersistedLanguageTag) {
            Log.i(TAG, "Language changed from '$oldLang' to '$currentPersistedLanguageTag'. Activity recreate is likely needed.")
        }
    }

    private fun applyLocaleToSystem(languageTag: String) {
        val localeList = if (languageTag.isNotEmpty()) {
            LocaleListCompat.forLanguageTags(languageTag)
        } else {
            LocaleListCompat.getEmptyLocaleList()
        }
        Log.d(TAG, "Setting application locales with: ${localeList.toLanguageTags()} (input tag: '$languageTag')")
        AppCompatDelegate.setApplicationLocales(localeList)
        Log.d(TAG, "AppCompatDelegate.setApplicationLocales called.")
    }

    fun getContextWithLocale(baseContext: Context): Context {
        val languageTagToApply = getInitializedPersistedLanguageTag() // Teraz odczytuje pole, a nie z DataStore

        val targetLocale: Locale = if (languageTagToApply.isNotEmpty()) {
            Locale.forLanguageTag(languageTagToApply)
        } else {
            ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
                ?: Locale.getDefault()
        }

        Log.d(TAG, "getContextWithLocale: Applying Locale: '${targetLocale.toLanguageTag()}' (from tag: '$languageTagToApply')")

        val configuration = Configuration(baseContext.resources.configuration)
        configuration.setLocale(targetLocale)
        // configuration.setLayoutDirection(targetLocale) // Dla RTL

        return baseContext.createConfigurationContext(configuration)
    }

    fun getAvailableLanguages(): List<LanguageOption> {
        return listOf(
            LanguageOption(tag = "", displayNameResId = R.string.language_system_default),
            LanguageOption(tag = "de", displayNameResId = R.string.lang_display_de),
            LanguageOption(tag = "en", displayNameResId = R.string.lang_display_en),
            LanguageOption(tag = "es", displayNameResId = R.string.lang_display_es),
            LanguageOption(tag = "fr", displayNameResId = R.string.lang_display_fr),
            LanguageOption(tag = "it", displayNameResId = R.string.lang_display_it),
            LanguageOption(tag = "pl", displayNameResId = R.string.lang_display_pl)
        )
    }
}