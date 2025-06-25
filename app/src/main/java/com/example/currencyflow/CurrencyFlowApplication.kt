package com.example.currencyflow

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.currencyflow.data.LanguageManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration // <--- DODAJ TEN IMPORT
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CurrencyFlowApplication : Application() {  // created before any activity

    private val TAG = "CurrencyFlowApp"
    private val ADMOB_TAG = "AdMobInit"

    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application created.")

        MobileAds.initialize(this) { initializationStatus ->
            Log.d(ADMOB_TAG, "MobileAds.initialize() complete.")
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClassname in statusMap.keys) {
                val status = statusMap[adapterClassname]
                // Używamy ?.let dla bezpieczeństwa, na wypadek gdyby status był null (choć nie powinien)
                status?.let {
                    Log.d(ADMOB_TAG, String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClassname, it.description, it.latency))
                }
            }

//            val testDeviceIds = listOf(
//                "B45DA32EF5474BDF8B3DD1BF018B0F09",
//                AdRequest.DEVICE_ID_EMULATOR // Dobrze jest to mieć, jeśli używasz standardowych emulatorów
//            )
//            val requestConfiguration = RequestConfiguration.Builder()
//                .setTestDeviceIds(testDeviceIds)
//                .build()
//            MobileAds.setRequestConfiguration(requestConfiguration)
//            Log.d(ADMOB_TAG, "Test device IDs configured: $testDeviceIds")
        }

        if (::languageManager.isInitialized) {
            Log.d(TAG, "onCreate: LanguageManager has been initialized by Hilt.")
        } else {
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