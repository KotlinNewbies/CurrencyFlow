package com.example.currencyflow

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.currencyflow.data.LanguageManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
private const val ADMOB_TAG = "AdMobInit"

@HiltAndroidApp
class CurrencyFlowApplication : Application() {  // created before any activity

    private val TAG = "CurrencyFlowApp"

    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application created.")

        if (::languageManager.isInitialized) {
            Log.d(TAG, "onCreate: LanguageManager has been initialized by Hilt.")
        } else {
            Log.e(TAG, "onCreate: CRITICAL - LanguageManager NOT initialized by Hilt in Application!")
        }
    }

    companion object {
        private val adSdkInitialized = AtomicBoolean(false)

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
        fun initializeMobileAdsSdk(context: Context, onComplete: ((InitializationStatus) -> Unit)? = null) {
            if (adSdkInitialized.compareAndSet(false, true)) {
                Log.d(ADMOB_TAG, "Attempting to initialize MobileAds SDK...")
                MobileAds.initialize(context.applicationContext) { initializationStatus ->
                    Log.d(ADMOB_TAG, "MobileAds.initialize() complete.")
                    val statusMap = initializationStatus.adapterStatusMap
                    for (adapterClassname in statusMap.keys) {
                        val status = statusMap[adapterClassname]
                        status?.let {
                            Log.d(
                                ADMOB_TAG, String.format(
                                    "Adapter name: %s, Description: %s, Latency: %d",
                                    adapterClassname, it.description, it.latency
                                )
                            )
                        }
                    }

                    val testDeviceIds = listOf(
                        "1587CFFF24221A16C080B47B4D691B0D", // S25
                        com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR
                    )
                    val requestConfiguration = com.google.android.gms.ads.RequestConfiguration.Builder()
                        .setTestDeviceIds(testDeviceIds)
                        .build()
                    MobileAds.setRequestConfiguration(requestConfiguration)
                    Log.d(ADMOB_TAG, "Test device IDs configured: $testDeviceIds")
                    onComplete?.invoke(initializationStatus)
                }
            } else {
                Log.d(ADMOB_TAG, "MobileAds SDK already initialized or initialization in progress.")
                // Wywołaj onComplete, jeśli został przekazany
                onComplete?.let { // Sprawdź, czy callback został przekazany
                    val currentStatus = MobileAds.getInitializationStatus()
                    if (currentStatus != null) {
                        Log.d(ADMOB_TAG, "Invoking onComplete for already initialized SDK with current status.")
                        it.invoke(currentStatus)
                    } else {
                        Log.w(ADMOB_TAG, "SDK marked as initialized, but getInitializationStatus() returned null. Callback not invoked.")
                    }
                }
            }
        }
    }
}