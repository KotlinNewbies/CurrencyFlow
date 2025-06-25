package com.example.currencyflow.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test ID

                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Podstawowe logowanie błędu - warto je zostawić
                        Log.e("AdmobBanner", "BŁĄD AdMob: ${loadAdError.message} (Kod: ${loadAdError.code})")
                        // Możesz dodać więcej szczegółów, jeśli chcesz, np. loadAdError.domain
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}