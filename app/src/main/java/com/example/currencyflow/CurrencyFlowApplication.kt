package com.example.currencyflow

import android.app.Application
import android.content.Context
import android.util.Log // Dodaj import dla Log
import com.example.currencyflow.data.LanguageManager // Upewnij się, że ścieżka do LanguageManager jest poprawna
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CurrencyFlowApplication : Application() {

    private val TAG = "CurrencyFlowApp" // Tag dla logów

    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application created.")
        // W tym momencie, Hilt powinien już wstrzyknąć `languageManager`.
        // Blok `init` w LanguageManager (z runBlocking) powinien zostać wykonany,
        // inicjalizując `currentPersistedLanguageTag`.
        if (::languageManager.isInitialized) {
            Log.d(TAG, "onCreate: LanguageManager has been initialized by Hilt.")
        } else {
            // To nie powinno się zdarzyć, jeśli Hilt jest poprawnie skonfigurowany.
            Log.e(TAG, "onCreate: CRITICAL - LanguageManager NOT initialized by Hilt in Application!")
        }
    }

    companion object {
        fun getLanguageManager(context: Context): LanguageManager {
            val application = context.applicationContext as? CurrencyFlowApplication
            if (application != null) {
                // Sprawdzamy, czy pole lateinit zostało zainicjalizowane
                if (application::languageManager.isInitialized) {
                    return application.languageManager
                } else {
                    // Ten scenariusz wskazuje na problem z konfiguracją Hilta lub cyklem życia aplikacji.
                    // Hilt powinien wstrzyknąć zależności do Application przed tym,
                    // jak Activity będzie próbowało uzyskać do nich dostęp.
                    Log.e(
                        "CurrencyFlowApp_Companion",
                        "CRITICAL: LanguageManager accessed before Hilt initialization in Application!"
                    )
                    // Rzucenie wyjątku jest tutaj właściwe, ponieważ jest to stan błędu.
                    throw IllegalStateException(
                        "LanguageManager has not been initialized in CurrencyFlowApplication. " +
                                "Check Hilt setup and application lifecycle."
                    )
                }
            }
            // Ten fallback również wskazuje na poważny problem.
            Log.e(
                "CurrencyFlowApp_Companion",
                "CRITICAL: Application context could not be cast to CurrencyFlowApplication."
            )
            throw IllegalStateException(
                "Application context is not an instance of CurrencyFlowApplication or LanguageManager is unavailable."
            )
        }
    }
}