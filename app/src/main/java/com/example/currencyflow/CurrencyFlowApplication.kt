package com.example.currencyflow

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.currencyflow.data.LanguageManager
import com.google.android.gms.ads.MobileAds // <--- DODAJ TEN IMPORT
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CurrencyFlowApplication : Application() {  // created before any activity

    private val TAG = "CurrencyFlowApp"
    private val ADMOB_TAG = "AdMobInit" // Opcjonalny tag dla logów AdMob

    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application created.")

        // Inicjalizacja Google Mobile Ads SDK
        // Można to zrobić w głównym wątku, ale przykład Google pokazuje w tle,
        // co jest dobrą praktyką, aby nie blokować UI, chociaż initialize() jest lekkie.
        // Dla prostoty, można też bezpośrednio:
        MobileAds.initialize(this) { initializationStatus ->
            // Ten callback jest opcjonalny, ale przydatny do logowania
            // lub jeśli używasz AdMob Mediation i chcesz wiedzieć, kiedy adaptery są gotowe.
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClassname in statusMap.keys) {
                val status = statusMap[adapterClassname]
                Log.d(ADMOB_TAG, String.format(
                    "Adapter name: %s, Description: %s, Latency: %d",
                    adapterClassname, status!!.description, status.latency))
            }
            // Możesz też po prostu zalogować ogólny status:
            Log.d(ADMOB_TAG, "MobileAds.initialize() complete.")
        }
        // Lub jeśli nie potrzebujesz szczegółowego statusu:
        // MobileAds.initialize(this) {}


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
                if (application::languageManager.isInitialized) {
                    return application.languageManager
                } else {
                    Log.e(
                        "CurrencyFlowApp_Companion",
                        "CRITICAL: LanguageManager accessed before Hilt initialization in Application!"
                    )
                    throw IllegalStateException(
                        "LanguageManager has not been initialized in CurrencyFlowApplication. " +
                                "Check Hilt setup and application lifecycle."
                    )
                }
            }
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