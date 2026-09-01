package com.fluida.currencyflow.viewmodel.ads

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluida.currencyflow.data.PremiumManager
import com.fluida.currencyflow.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

private const val TAG_AD_VM = "AdBannerViewModel"
private const val MAX_AD_RETRIES_VM = 3
private const val INITIAL_BACKOFF_MS_VM = 10000L
private const val MAX_BACKOFF_MS_VM = 60000L
private const val BACKOFF_FACTOR_VM = 2.0
private const val AD_REFRESH_INTERVAL_MS = 60000L // Interwał, zmienić na 0L jeśli admob ma zarządzać odświeżaniem, w przeciwnym razie 60000

@HiltViewModel
class AdBannerViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _adBannerState = MutableStateFlow<AdBannerState>(AdBannerState.Idle)
    val adBannerState: StateFlow<AdBannerState> = _adBannerState.asStateFlow()

    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.observe()
        .map { status ->
            Log.d(TAG_AD_VM, "Network status changed in VM: $status")
            status == ConnectivityObserver.Status.Available
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = connectivityObserver.getCurrentStatus() == ConnectivityObserver.Status.Available
        )

    private val _uiEvent = Channel<AdBannerUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var loadAdJob: Job? = null
    private var refreshAdJob: Job? = null
    private var adRetryAttempt = 0

    init {
        Log.d(TAG_AD_VM, "AdBannerViewModel initialized. Initial network status: ${isNetworkAvailable.value}")

        // Obserwuj globalny stan reklam (Premium)
        viewModelScope.launch {
            premiumManager.adsEnabled.collect { enabled ->
                Log.d(TAG_AD_VM, "Ads enabled state changed: $enabled. Current state: ${_adBannerState.value}")
                if (!enabled) {
                    loadAdJob?.cancel()
                    refreshAdJob?.cancel()
                    _adBannerState.value = AdBannerState.Disabled
                } else if (_adBannerState.value == AdBannerState.Disabled || _adBannerState.value == AdBannerState.Idle) {
                    // Jeśli reklamy zostały włączone, natychmiast zacznij ładować
                    _adBannerState.value = AdBannerState.Idle
                    attemptLoadAd()
                }
            }
        }

        // Obserwuj zmiany sieci, aby potencjalnie ponowić próbę ładowania reklamy
        viewModelScope.launch {
            isNetworkAvailable.collect { networkAvailable ->
                val currentState = _adBannerState.value
                Log.d(TAG_AD_VM, "Network status collected in init: $networkAvailable, Current ad state: $currentState")
                if (networkAvailable) {
                    // Jeśli sieć wróciła, a byliśmy w stanie błędu LUB jesteśmy w Idle, spróbuj załadować
                    if (currentState is AdBannerState.Error || currentState == AdBannerState.Idle) {
                        Log.i(TAG_AD_VM, "Network became available. Current ad state: $currentState. Attempting to load ad.")
                        // Sprawdź, czy poprzedni błąd był związany z siecią (np. kod błędu AdMob == 2)
                        // lub po prostu ponów próbę, jeśli był jakikolwiek błąd
                        attemptLoadAd()
                    }
                } else {
                    // Sieć utracona
                    if (currentState is AdBannerState.Loading) {
                        Log.w(TAG_AD_VM, "Network lost while ad was in Loading state. Ad request will likely fail.")
                        // Możemy pozwolić, aby AdListener obsłużył błąd sieci,
                        // lub proaktywnie anulować i ustawić stan błędu.
                        // Dla prostoty, polegajmy na AdListener, ale można to rozbudować.
                        // loadAdJob?.cancel()
                        // _adBannerState.value = AdBannerState.Error("Network unavailable", ERROR_CODE_NETWORK_UNAVAILABLE_CUSTOM)
                    }
                }
            }
        }
    }

    fun onBannerReady() {
        Log.d(TAG_AD_VM, "onBannerReady called. Current state: ${_adBannerState.value}, Network: ${isNetworkAvailable.value}")
        
        if (_adBannerState.value == AdBannerState.Disabled) {
            Log.d(TAG_AD_VM, "onBannerReady: Ads are disabled. Skipping.")
            return
        }

        if (!isNetworkAvailable.value) {
            Log.w(TAG_AD_VM, "onBannerReady: Network is unavailable. Not attempting to load ad now.")
            // Ustaw stan błędu, jeśli jeszcze nie jest, aby UI mogło odpowiednio zareagować
            if (_adBannerState.value != AdBannerState.Error("Network unavailable", -1)) { // Użyj jakiegoś kodu dla braku sieci
                _adBannerState.value = AdBannerState.Error("Network unavailable", -1) // Przykładowy kod błędu
            }
            return // Nie próbuj ładować, jeśli nie ma sieci
        }

        if (_adBannerState.value == AdBannerState.Idle || _adBannerState.value is AdBannerState.Error) {
            attemptLoadAd()
        } else if (_adBannerState.value == AdBannerState.Loaded) {
            viewModelScope.launch { _uiEvent.send(AdBannerUiEvent.ShowAd) }
            scheduleAdRefresh()
        }
    }

    fun onAdActuallyLoadedInView() {
        Log.d(TAG_AD_VM, "onAdActuallyLoadedInView confirmed by UI.")
        if (_adBannerState.value == AdBannerState.Loading) {
            _adBannerState.value = AdBannerState.Loaded
            adRetryAttempt = 0
            scheduleAdRefresh()
        } else {
            Log.w(TAG_AD_VM, "onAdActuallyLoadedInView called but state was not Loading: ${_adBannerState.value}")
        }
    }

    fun onAdFailedToLoadInView(errorMessage: String, errorCode: Int) {
        Log.e(TAG_AD_VM, "onAdFailedToLoadInView by UI: $errorMessage (Code: $errorCode), Network: ${isNetworkAvailable.value}")
        adRetryAttempt++
        refreshAdJob?.cancel()

        // Jeśli błąd jest spowodowany brakiem sieci (np. kod 2 od AdMob), a sieć jest niedostępna,
        // nie ponawiaj od razu, poczekaj na powrót sieci (obsłużone w init.collect)
        val isNetworkErrorByAdMob = errorCode == 2 // ERROR_CODE_NETWORK_ERROR
        if (!isNetworkAvailable.value && isNetworkErrorByAdMob) {
            Log.w(TAG_AD_VM, "Ad failed due to network error and network is still unavailable. Waiting for network to return.")
            _adBannerState.value = AdBannerState.Error(errorMessage, errorCode) // Ustaw stan błędu
            // Nie wywołuj scheduleLoadWithBackoff()
            return
        }

        if (adRetryAttempt <= MAX_AD_RETRIES_VM) {
            Log.d(TAG_AD_VM, "Scheduling retry for ad load (attempt ${adRetryAttempt}).")
            _adBannerState.value = AdBannerState.Loading
            scheduleLoadWithBackoff()
        } else {
            Log.w(TAG_AD_VM, "Failed to load ad after $adRetryAttempt attempts.")
            _adBannerState.value = AdBannerState.Error(errorMessage, errorCode)
        }
    }

    private fun attemptLoadAd() {
        if (_adBannerState.value == AdBannerState.Disabled) {
            Log.d(TAG_AD_VM, "AttemptLoadAd: Ads are disabled. Aborting.")
            return
        }

        if (!isNetworkAvailable.value) {
            Log.w(TAG_AD_VM, "AttemptLoadAd: Network is unavailable. Aborting.")
            if (_adBannerState.value != AdBannerState.Error("Network unavailable", -1)) {
                _adBannerState.value = AdBannerState.Error("Network unavailable", -1)
            }
            return
        }

        loadAdJob?.cancel()
        refreshAdJob?.cancel()
        _adBannerState.value = AdBannerState.Loading
        // Resetuj licznik prób tylko jeśli to nowa, świadoma próba (np. z onBannerReady lub refreshAd)
        // Jeśli to ponowienie z onAdFailedToLoadInView, adRetryAttempt jest już inkrementowany.
        // Dla uproszczenia, jeśli attemptLoadAd jest wywoływane z zewnątrz (onBannerReady, refreshAd),
        // zawsze resetuj licznik.
        // adRetryAttempt = 0 // Usunięte, bo onAdFailedToLoadInView zarządza tym dla ponowień
        scheduleLoadWithBackoff() // adRetryAttempt będzie użyte przez scheduleLoadWithBackoff
    }

    private fun scheduleLoadWithBackoff() {
        // Jeśli adRetryAttempt = 0, to pierwsza próba zainicjowana przez attemptLoadAd
        // Jeśli > 0, to ponowienie po błędzie
        Log.d(TAG_AD_VM, "scheduleLoadWithBackoff called. Retry attempt (before delay): $adRetryAttempt")

        loadAdJob = viewModelScope.launch {
            if (!isNetworkAvailable.value) { // Sprawdź sieć również tutaj, przed opóźnieniem
                Log.w(TAG_AD_VM, "scheduleLoadWithBackoff: Network unavailable before delay. Setting error state.")
                _adBannerState.value = AdBannerState.Error("Network unavailable", -1)
                return@launch
            }

            val currentDelay: Long = when {
                // Dla pierwszej próby (adRetryAttempt == 0), opóźnienie jest inicjalne.
                // Dla ponowień (adRetryAttempt > 0), używamy backoff.
                adRetryAttempt == 0 -> 500L
                else -> {
                    var backoffDelay = INITIAL_BACKOFF_MS_VM * (BACKOFF_FACTOR_VM.pow(adRetryAttempt - 1)).toLong()
                    val jitter = (backoffDelay * 0.2 * (Random.nextDouble() * 2 - 1)).toLong()
                    backoffDelay = (backoffDelay + jitter).coerceAtLeast(0)
                    min(backoffDelay, MAX_BACKOFF_MS_VM)
                }
            }

            if (currentDelay > 0) {
                Log.d(TAG_AD_VM, "Delaying ${currentDelay}ms for ad load (retry attempt num ${adRetryAttempt +1})")
                delay(currentDelay.milliseconds)
            }

            if (!isActive) {
                Log.w(TAG_AD_VM, "Coroutine inactive after delay, aborting ad load command.")
                return@launch
            }

            if (!isNetworkAvailable.value) { // Sprawdź sieć ponownie po opóźnieniu
                Log.w(TAG_AD_VM, "scheduleLoadWithBackoff: Network unavailable after delay. Setting error state.")
                _adBannerState.value = AdBannerState.Error("Network unavailable", -1)
                return@launch
            }

            Log.d(TAG_AD_VM, "Sending LoadAd event to UI (for retry attempt num ${adRetryAttempt +1}).")
            _uiEvent.send(AdBannerUiEvent.LoadAd)
        }
    }

    fun refreshAd() {
        Log.i(TAG_AD_VM, "refreshAd called. Network: ${isNetworkAvailable.value}")
        adRetryAttempt = 0 // Resetuj licznik prób dla ręcznego odświeżenia
        attemptLoadAd()
    }

    private fun scheduleAdRefresh() {
        refreshAdJob?.cancel()

        refreshAdJob = viewModelScope.launch {
            Log.d(TAG_AD_VM, "Scheduling ad refresh in ${AD_REFRESH_INTERVAL_MS / 1000} seconds.")
            delay(AD_REFRESH_INTERVAL_MS.milliseconds)
            if (isActive && _adBannerState.value == AdBannerState.Loaded && isNetworkAvailable.value) {
                Log.i(TAG_AD_VM, "Automatic ad refresh interval reached.")
                adRetryAttempt = 0 // Odświeżenie to nowa, "świeża" próba
                refreshAd() // Wywołaj metodę, która już resetuje licznik i próbuje
            } else {
                Log.d(TAG_AD_VM, "Ad refresh cancelled or not applicable. State: ${_adBannerState.value}, Network: ${isNetworkAvailable.value}, isActive: $isActive")
            }
        }
    }

    fun onBannerPaused() {
        Log.d(TAG_AD_VM, "onBannerPaused: Cancelling scheduled refresh if any.")
        refreshAdJob?.cancel()
    }

    fun onBannerResumed() {
        Log.d(TAG_AD_VM, "onBannerResumed. Current state: ${_adBannerState.value}, Network: ${isNetworkAvailable.value}")
        
        if (_adBannerState.value == AdBannerState.Disabled) {
            return
        }

        if (!isNetworkAvailable.value) {
            Log.w(TAG_AD_VM, "onBannerResumed: Network is unavailable. Not attempting to load or refresh.")
            if (_adBannerState.value != AdBannerState.Error("Network unavailable", -1)) {
                _adBannerState.value = AdBannerState.Error("Network unavailable", -1)
            }
            return
        }

        if (_adBannerState.value == AdBannerState.Loaded) {
            scheduleAdRefresh()
        } else if (_adBannerState.value == AdBannerState.Idle || _adBannerState.value is AdBannerState.Error) {
            // Jeśli poprzedni błąd nie był związany z siecią lub sieć wróciła
            adRetryAttempt = 0 // Nowa próba po wznowieniu, jeśli był błąd
            attemptLoadAd()
        }
    }

    override fun onCleared() {
        Log.d(TAG_AD_VM, "AdBannerViewModel onCleared.")
        loadAdJob?.cancel()
        refreshAdJob?.cancel()
    }
}

