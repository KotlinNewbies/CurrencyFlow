package com.example.currencyflow.di // Lub com.example.currencyflow, jeśli wybrałeś główny pakiet

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.currencyflow.data.LanguageManager // Upewnij się, że importujesz swój LanguageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Nazwa pliku z DataStore, musi być taka sama jak używana w LanguageManager, jeśli tam ją definiowałeś
// lub jeśli LanguageManager polega na tej konkretnej nazwie przy użyciu rozszerzenia context.
// Jeśli DataStore jest tworzony tylko tutaj i wstrzykiwany, to ta stała jest ważna tutaj.
private const val USER_PREFERENCES_NAME = "app_settings_preferences"

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile(USER_PREFERENCES_NAME) }
        )
    }
}

@InstallIn(SingletonComponent::class)
@Module
object ManagerModule {

    @Singleton
    @Provides
    fun provideLanguageManager(
        @ApplicationContext applicationContext: Context,
        appSettingsDataStore: DataStore<Preferences> // Hilt wstrzyknie DataStore z DataStoreModule
    ): LanguageManager {
        return LanguageManager(appSettingsDataStore)
    }
}

