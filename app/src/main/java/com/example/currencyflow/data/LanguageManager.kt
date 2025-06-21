// LanguageManager.kt
package com.example.currencyflow.data // Or the appropriate package, e.g., com.example.currencyflow.data.repository

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

// Preference keys object to avoid typos.
object PreferenceKeys { // This object can remain if the key is also used elsewhere.
    val APP_LANGUAGE_TAG = stringPreferencesKey("app_language_tag")
}

data class LanguageOption(
    val tag: String,
    val displayNameResId: Int
)

@Singleton
class LanguageManager @Inject constructor(
    private val appSettingsDataStore: DataStore<Preferences> // DataStore injected by Hilt
) {
    private val TAG = "LanguageManager"

    // Stores the last persisted language tag.
    // Initialized in the init block.
    private var currentPersistedLanguageTag: String = "" // Default to empty string (for "System Default")
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
            preferences[PreferenceKeys.APP_LANGUAGE_TAG] ?: "" // Default to empty string
        }

    init {
        // Synchronously initialize the language state on creation.
        // runBlocking is used intentionally here for synchronous state initialization,
        // which can be necessary for scenarios like attachBaseContext.
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
                    // Log error and default to system language if DataStore read fails.
                    Log.e(TAG, "Error initializing language from DataStore, defaulting to system language.", exception)
                    emit("")
                }
                .first()
            Log.d(TAG, "Initialized currentPersistedLanguageTag: '$currentPersistedLanguageTag'")
            isInitialized = true
        }
    }

    // Synchronously retrieves the initialized persisted language tag.
    private fun getInitializedPersistedLanguageTag(): String {
        // isInitialized should ideally always be true after the init block.
        // This log serves as a safeguard.
        if (!isInitialized) {
            Log.w(TAG, "getInitializedPersistedLanguageTag called when not initialized! This indicates a potential issue.")
            // Attempting re-initialization here might hide a deeper problem.
        }
        return currentPersistedLanguageTag
    }

    suspend fun setApplicationLanguage(languageTag: String) {
        Log.d(TAG, "Attempting to set application language to: '$languageTag'")
        val oldLang = currentPersistedLanguageTag
        currentPersistedLanguageTag = languageTag // Update internal state immediately

        try {
            appSettingsDataStore.edit { settings ->
                settings[PreferenceKeys.APP_LANGUAGE_TAG] = languageTag
            }
            Log.d(TAG, "Successfully saved language tag '$languageTag' to DataStore")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving language tag '$languageTag' to DataStore", e)
            // Optionally, revert currentPersistedLanguageTag or handle the error more gracefully.
        }

        applyLocaleToSystem(languageTag)

        if (oldLang != currentPersistedLanguageTag) {
            Log.i(TAG, "Language changed from '$oldLang' to '$currentPersistedLanguageTag'. Activity recreate is likely needed.")
        }
    }

    private fun applyLocaleToSystem(languageTag: String) {
        val localeList = if (languageTag.isNotEmpty()) {  // creates a new list of
            LocaleListCompat.forLanguageTags(languageTag) // locales containing the specified tag

        } else {
            // An empty tag means "System Default".
            LocaleListCompat.getEmptyLocaleList()
        }
        Log.d(TAG, "Setting application locales with: ${localeList.toLanguageTags()} (input tag: '$languageTag')")
        AppCompatDelegate.setApplicationLocales(localeList)  // Informs OS to read the appropriate language resource IMPORTANT
        Log.d(TAG, "AppCompatDelegate.setApplicationLocales called.")
    }

    /**
     * Creates and returns a new Context with the application's locale set
     * based on the persisted language tag.
     * If no language is persisted (or the tag is empty), it uses the system language.
     * This is typically used in `Activity.attachBaseContext()`.
     */
    fun getContextWithLocale(baseContext: Context): Context {
        val languageTagToApply = getInitializedPersistedLanguageTag()

        val targetLocale: Locale = if (languageTagToApply.isNotEmpty()) {
            Locale.forLanguageTag(languageTagToApply)
        } else {
            // Use the primary system locale.
            // Fallback to Locale.getDefault() if system configuration is somehow unavailable,
            // though getLocales(Configuration)[0] should generally work.
            ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
                ?: Locale.getDefault()
        }

        Log.d(TAG, "getContextWithLocale: Applying Locale: '${targetLocale.toLanguageTag()}' (from tag: '$languageTagToApply')")

        val configuration = Configuration(baseContext.resources.configuration)
        configuration.setLocale(targetLocale) // More direct way to set a single locale
        // configuration.setLocales(LocaleList(targetLocale)) // Also correct

        // For supporting RTL languages, uncomment and use:
        // configuration.setLayoutDirection(targetLocale)

        return baseContext.createConfigurationContext(configuration)
    }

    fun getAvailableLanguages(): List<LanguageOption> {
        return listOf(
            LanguageOption(tag = "", displayNameResId = R.string.language_system_default), // Zazwyczaj na górze
            LanguageOption(tag = "de", displayNameResId = R.string.lang_display_de),     // Niemiecki
            LanguageOption(tag = "en", displayNameResId = R.string.lang_display_en),     // Angielski
            LanguageOption(tag = "es", displayNameResId = R.string.lang_display_es),     // Hiszpański
            LanguageOption(tag = "fr", displayNameResId = R.string.lang_display_fr),     // Francuski
            LanguageOption(tag = "it", displayNameResId = R.string.lang_display_it),     // Włoski
            LanguageOption(tag = "pl", displayNameResId = R.string.lang_display_pl)      // Polski
            // Add other supported languages here
        )
    }
}