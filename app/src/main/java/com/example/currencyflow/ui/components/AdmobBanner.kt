package com.example.currencyflow.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) { // Na razie nie przekazujemy adUnitId jako parametr
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                // Ustawienie rozmiaru banera (możesz wybrać inny, np. AdSize.LARGE_BANNER)
                setAdSize(AdSize.BANNER)

                // WAŻNE: Użyj TESTOWEGO ID JEDNOSTKI REKLAMOWEJ DLA BANERA
                adUnitId = "ca-app-pub-3940256099942544/6300978111"

                // Załaduj żądanie reklamy
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}