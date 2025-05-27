package com.example.currencyflow.di

import android.content.Context // Potrzebny dla @Provides
import android.util.Log
import com.example.currencyflow.util.ConnectivityObserver
import com.example.currencyflow.util.NetworkConnectivityObserver
// import dagger.Binds // Już niepotrzebne
import dagger.Module
import dagger.Provides // Zmieniamy na Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext // Potrzebny dla @Provides
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConnectivityModule { // Można też użyć 'class ConnectivityModule'

    @Provides
    @Singleton
    fun provideConnectivityObserver(
        @ApplicationContext context: Context // Wstrzykujemy Context tutaj
    ): ConnectivityObserver {
        Log.d("HiltModuleDebug", "ConnectivityModule: provideConnectivityObserver WYWOŁANE")
        return NetworkConnectivityObserver(context)
    }
}