package com.fluida.currencyflow.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.fluida.currencyflow.data.LanguageManager
import com.fluida.currencyflow.data.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
        appSettingsDataStore: DataStore<Preferences>
    ): LanguageManager {
        return LanguageManager(appSettingsDataStore)
    }

    @Singleton
    @Provides
    fun provideSettingsManager(
        appSettingsDataStore: DataStore<Preferences>
    ): SettingsManager {
        return SettingsManager(appSettingsDataStore)
    }
}