package com.fluida.currencyflow.ui.components

import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.fluida.currencyflow.viewmodel.ads.AdBannerState
import com.fluida.currencyflow.viewmodel.ads.AdBannerUiEvent
import com.fluida.currencyflow.viewmodel.ads.AdBannerViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest.Builder
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.flow.collectLatest


private const val TAG_BANNER_COMP = "AdmobBannerComponent"

@Composable
fun AdmobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111", // Test ID
    viewModel: AdBannerViewModel = hiltViewModel() // Hilt do wstrzyknięcia ViewModelu
) {
    val context = LocalContext.current
    val adBannerState by viewModel.adBannerState.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState() // Obserwuj stan sieci

    // Tworzymy i zapamiętujemy AdView LOKALNIE w komponencie
    val adViewInstance = remember {
        Log.d(TAG_BANNER_COMP, "Creating NEW AdView instance")
        AdView(context).apply {
            this.adUnitId = adUnitId
            setAdSize(AdSize.BANNER)
            // Listener jest ustawiany dynamicznie w odpowiedzi na UiEvent.LoadAd
        }
    }

    // Efekt do obsługi cyklu życia AdView i komunikacji z ViewModel
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, adViewInstance, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG_BANNER_COMP, "Lifecycle.Event.ON_RESUME")
                    adViewInstance.resume()
                    viewModel.onBannerResumed() // Poinformuj ViewModel
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG_BANNER_COMP, "Lifecycle.Event.ON_PAUSE")
                    adViewInstance.pause()
                    viewModel.onBannerPaused() // Poinformuj ViewModel
                }
                Lifecycle.Event.ON_DESTROY -> {
                    Log.d(TAG_BANNER_COMP, "Lifecycle.Event.ON_DESTROY - Destroying AdView")
                    adViewInstance.destroy()
                    // ViewModel zostanie wyczyszczony przez Hilt/system, jeśli jego scope się zakończy
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Poinformuj ViewModel, że baner jest gotowy (np. po pierwszej kompozycji)
        viewModel.onBannerReady()

        onDispose {
            Log.d(TAG_BANNER_COMP, "AdmobBanner onDispose - AdView will be destroyed by Lifecycle.Event.ON_DESTROY if scope matches.")
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Nie niszczymy adViewInstance tutaj bezpośrednio, jeśli ON_DESTROY observera ma to zrobić.
            // Ale jeśli komponent jest usuwany z drzewa przed ON_DESTROY cyklu życia, to warto:
            // adViewInstance.destroy() // Rozważ to, jeśli onDispose może być wywołane wcześniej
        }
    }

    // Nasłuchuj na zdarzenia UI z ViewModelu
    LaunchedEffect(key1 = viewModel, key2 = adViewInstance) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AdBannerUiEvent.LoadAd, is AdBannerUiEvent.ShowAd -> {
                    if (!isNetworkAvailable) {
                        Log.w(TAG_BANNER_COMP, "Event $event received, but network is unavailable. Skipping adView.loadAd().")
                        return@collectLatest
                    }
                    Log.d(TAG_BANNER_COMP, "Received $event event from ViewModel. Loading ad into AdView.")
                    adViewInstance.adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            Log.i(TAG_BANNER_COMP, "AdView: Ad loaded successfully ($event).")
                            viewModel.onAdActuallyLoadedInView()
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            Log.e(TAG_BANNER_COMP, "AdView: Ad failed to load ($event): ${loadAdError.message} (Code: ${loadAdError.code})")
                            viewModel.onAdFailedToLoadInView(loadAdError.message, loadAdError.code)
                        }
                        override fun onAdOpened() {
                            super.onAdOpened()
                            Log.d(TAG_BANNER_COMP, "AdView: Ad opened (clicked).")
                        }
                    }
                    adViewInstance.loadAd(Builder().build())
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AdSize.BANNER.getHeightInPixels(context).pixelsToDp())
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        when (adBannerState) {
            is AdBannerState.Loading -> {
                if (isNetworkAvailable) { // WYŚWIETLAJ KÓŁKO TYLKO GDY JEST SIEĆ
                    CircularProgressIndicator()
                    Log.d(TAG_BANNER_COMP, "UI State: Loading (Network available)")
                } else {
                    // Jeśli nie ma sieci, a stan to Loading, możesz pokazać placeholder lub nic
                    // Można też od razu pokazać komunikat o braku sieci, jeśli ViewModel nie zdążył
                    // przejść w stan Error.
                    Text("Loading ad... (No network)", color = MaterialTheme.colorScheme.outline)
                    Log.d(TAG_BANNER_COMP, "UI State: Loading (Network unavailable) - No spinner")
                }
            }
            is AdBannerState.Loaded -> {
                AndroidView(
                    factory = { Log.d(TAG_BANNER_COMP, "AndroidView factory. Parent before: ${adViewInstance.parent}")
                        (adViewInstance.parent as? FrameLayout)?.removeView(adViewInstance)
                        FrameLayout(it).apply { addView(adViewInstance) } },
                    update = { Log.d(TAG_BANNER_COMP, "AndroidView update. AdView parent: ${adViewInstance.parent}")
                        if (adViewInstance.parent != it) {
                            (adViewInstance.parent as? FrameLayout)?.removeView(adViewInstance)
                            it.removeAllViews()
                            it.addView(adViewInstance)
                        } },
                    modifier = Modifier.fillMaxSize()
                )
                Log.d(TAG_BANNER_COMP, "UI State: Loaded")
            }
            is AdBannerState.Error -> {
                val errorState = adBannerState as AdBannerState.Error
                // Możesz dostosować komunikat błędu, jeśli wynika on z braku sieci
                val displayMessage = if (!isNetworkAvailable && errorState.errorCode != 0 /* np. kod błędu sieci AdMob */) {
                    "Ad failed: No network connection"
                } else {
                    "Ad failed: ${errorState.message}"
                }
                Text(displayMessage, color = MaterialTheme.colorScheme.error)
                Log.d(TAG_BANNER_COMP, "UI State: Error (${errorState.message}), Network: $isNetworkAvailable")
            }
            AdBannerState.Idle -> {
                if (isNetworkAvailable) {
                    CircularProgressIndicator()
                    Log.d(TAG_BANNER_COMP, "UI State: Idle (Network available)")
                } else {
                    Text("Waiting for ad... (No network)", color = MaterialTheme.colorScheme.outline)
                    Log.d(TAG_BANNER_COMP, "UI State: Idle (Network unavailable) - No spinner")
                }
            }
        }
    }
}

// Helper używający LocalDensity zamiast DisplayMetrics
@Composable
private fun Int.pixelsToDp() = with(LocalDensity.current) { 
    this@pixelsToDp.toDp() 
}

