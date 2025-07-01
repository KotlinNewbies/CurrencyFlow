package com.example.currencyflow.ui.components

import android.util.Log
import android.widget.FrameLayout // Użyjemy FrameLayout jako kontenera dla AdView
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AdmobBanner(modifier: Modifier = Modifier, initialDelayMillis: Long = 1000L) {
    val adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test ID
    val context = LocalContext.current

    val adView = remember {
        Log.d("AdmobBanner", "Tworzenie NOWEJ instancji AdView (remember)")
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId // Pamiętaj, aby ustawić ID jednostki reklamowej
        }
    }

    var isAdActuallyLoaded by remember { mutableStateOf(adView.parent != null) } // Prosta heurystyka, czy reklama mogła być załadowana
    var isAdLoadingAttemptedOrInProgress by remember { mutableStateOf(false) }
    var adLoadFailedPreviously by remember { mutableStateOf(false) }


    LaunchedEffect(key1 = adView, key2 = adLoadFailedPreviously, key3 = isAdActuallyLoaded) {
        // Jeśli reklama nie jest załadowana i nie było poprzedniej próby zakończonej błędem (lub chcemy ponowić)
        // ORAZ adView nie ma jeszcze rodzica (co oznacza, że nie jest wyświetlany)
        // LUB jeśli chcemy agresywnie próbować załadować, jeśli nie jest załadowana.
        if (!isAdActuallyLoaded && !isAdLoadingAttemptedOrInProgress && !adLoadFailedPreviously) {
            Log.d("AdmobBanner", "LaunchedEffect: Warunki spełnione, przygotowanie do ładowania reklamy.")
            isAdLoadingAttemptedOrInProgress = true // Oznaczamy próbę ładowania

            try {
                if (initialDelayMillis > 0) {
                    Log.d("AdmobBanner", "LaunchedEffect: Rozpoczynanie opóźnienia ${initialDelayMillis}ms przed loadAd()")
                    delay(initialDelayMillis)
                    Log.d("AdmobBanner", "LaunchedEffect: Opóźnienie zakończone.")
                }

                if (!isActive) {
                    Log.w("AdmobBanner", "Korutyna nieaktywna po opóźnieniu, przerywanie ładowania reklamy.")
                    isAdLoadingAttemptedOrInProgress = false
                    return@LaunchedEffect
                }

                Log.d("AdmobBanner", "LaunchedEffect: Ustawianie adListener i rozpoczynanie adView.loadAd()")
                adView.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.d("AdmobBanner", "Reklama załadowana pomyślnie.")
                        isAdActuallyLoaded = true
                        isAdLoadingAttemptedOrInProgress = false
                        adLoadFailedPreviously = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        Log.e("AdmobBanner", "BŁĄD AdMob: ${loadAdError.message} (Kod: ${loadAdError.code})")
                        isAdActuallyLoaded = false
                        isAdLoadingAttemptedOrInProgress = false
                        adLoadFailedPreviously = true
                    }
                    // ... inne callbacki AdListener ...
                }
                adView.loadAd(AdRequest.Builder().build())

            } catch (e: CancellationException) {
                Log.w("AdmobBanner", "LaunchedEffect anulowany.", e)
                isAdLoadingAttemptedOrInProgress = false
            } catch (e: Exception) {
                Log.e("AdmobBanner", "Nieoczekiwany błąd w LaunchedEffect.", e)
                isAdLoadingAttemptedOrInProgress = false
                adLoadFailedPreviously = true
            }
        } else {
            Log.d(
                "AdmobBanner",
                "LaunchedEffect: Warunki do ładowania reklamy niespełnione (isAdActuallyLoaded=$isAdActuallyLoaded, isAdLoadingAttemptedOrInProgress=$isAdLoadingAttemptedOrInProgress, adLoadFailedPreviously=$adLoadFailedPreviously, parent=${adView.parent})"
            )
            // Jeśli reklama jest już załadowana, ale adView straciło rodzica (np. po nawigacji wstecz),
            // a my chcemy ją ponownie pokazać bez przeładowywania, to AndroidView w factory sobie z tym poradzi,
            // o ile isAdActuallyLoaded jest true.
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, key2 = adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_DESTROY -> {
                    // To jest moment, gdy Activity/Fragment jest niszczony.
                    // Wtedy AdView MUSI być zniszczony.
                    Log.d("AdmobBanner", "ON_DESTROY cyklu życia Activity/Fragmentu: Niszczenie AdView ($adView)")
                    adView.destroy()
                    // Tutaj moglibyśmy zresetować stany, aby przy następnym utworzeniu Activity
                    // reklama ładowała się od nowa.
                    isAdActuallyLoaded = false
                    isAdLoadingAttemptedOrInProgress = false
                    adLoadFailedPreviously = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            // Ten onDispose jest wywoływany, gdy AdmobBanner jest usuwany z kompozycji.
            // Jeśli Activity/Fragment nie jest niszczony (np. tylko nawigacja)
            Log.d("AdmobBanner", "onDispose komponentu AdmobBanner: Usuwanie obserwatora cyklu życia dla AdView ($adView). NIE niszczymy AdView tutaj, chyba że cały ekran jest niszczony.")
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AdSize.BANNER.getHeightInPixels(context).pixelsToDp())
            .background(
                if (isAdActuallyLoaded) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surface
            ),
        contentAlignment = Alignment.Center
    ) {
        // Zawsze próbuj wyświetlić AdView, jeśli myślimy, że jest załadowane,
        // lub jeśli jest w trakcie ładowania (AndroidView poradzi sobie z pustym AdView,
        // a potem zaktualizuje się, gdy reklama się załaduje).
        // Kluczowe jest, że AndroidView factory jest wywoływane, gdy AdView ma być dodane.
        if (isAdActuallyLoaded || isAdLoadingAttemptedOrInProgress) {
            AndroidView(
                factory = { factoryCtx ->
                    Log.d("AdmobBanner", "AndroidView factory: Używanie AdView ($adView). Parent przed: ${adView.parent}")
                    // Usuń adView z poprzedniego rodzica, jeśli istnieje, aby uniknąć crasha
                    (adView.parent as? FrameLayout)?.removeView(adView)
                    FrameLayout(factoryCtx).apply {
                        addView(adView)
                    }
                },
                update = { frameLayout ->
                    Log.d("AdmobBanner", "AndroidView update: FrameLayout ($frameLayout) z AdView ($adView). Parent: ${adView.parent}")
                    // Upewnij się, że AdView jest dzieckiem tego FrameLayout
                    if (adView.parent != frameLayout) {
                        Log.w("AdmobBanner", "AdView nie jest dzieckiem oczekiwanego FrameLayout w update. Próba naprawy.")
                        (adView.parent as? FrameLayout)?.removeView(adView)
                        frameLayout.removeAllViews() // Usuń wszystko, co mogło tam być
                        frameLayout.addView(adView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            // Jeśli reklama się ładuje, a nie jest jeszcze załadowana, pokaż progress bar NAD reklamą (lub zamiast niej)
            if (isAdLoadingAttemptedOrInProgress && !isAdActuallyLoaded) {
                CircularProgressIndicator()
            }
        } else if (adLoadFailedPreviously) {
            Text("Ad failed", color = MaterialTheme.colorScheme.error)
        } else {
            Log.d("AdmobBanner", "Stan oczekiwania na załadowanie lub błąd.")
            CircularProgressIndicator()
        }
    }
}
@Composable
private fun Int.pixelsToDp() = with(LocalContext.current.resources.displayMetrics) {
    (this@pixelsToDp / density).dp
}