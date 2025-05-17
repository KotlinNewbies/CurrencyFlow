package com.example.currencyflow.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose // Kluczowy import
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged // Kluczowy import
import javax.inject.Inject
import javax.inject.Singleton


interface ConnectivityObserver {
    fun observe(): Flow<Status>
    fun getCurrentStatus(): Status // Dodajemy metodę do natychmiastowego sprawdzenia

    enum class Status {
        Available, Unavailable, Losing, Lost
    }
}

@Singleton
class NetworkConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectivityObserver.Status> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    // Używamy trySend, który nie jest funkcją zawieszającą
                    trySend(ConnectivityObserver.Status.Available)
                    // Możesz opcjonalnie sprawdzić wynik:
                    // val result = trySend(ConnectivityObserver.Status.Available)
                    // if (result.isFailure) { /* obsłuż błąd wysyłania, np. jeśli flow jest już zamknięty */ }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    trySend(ConnectivityObserver.Status.Losing)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    trySend(ConnectivityObserver.Status.Lost)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    trySend(ConnectivityObserver.Status.Unavailable)
                }
            }

            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(networkRequest, callback)

            // Dla początkowego statusu, można użyć trySendBlocking lub launch,
            // ponieważ jesteśmy w bloku callbackFlow, który ma scope.
            // trySend jest tutaj nadal bezpieczne.
            trySend(getCurrentStatus())
            // LUB jeśli chcesz launch dla spójności z wcześniejszym podejściem,
            // i jeśli getCurrentStatus() byłoby operacją długotrwałą (choć nie jest):
            // launch { send(getCurrentStatus()) } // Wymagałoby importu kotlinx.coroutines.launch

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }

    override fun getCurrentStatus(): ConnectivityObserver.Status {
        // ... (bez zmian)
        val activeNetwork = connectivityManager.activeNetwork
            ?: return ConnectivityObserver.Status.Unavailable
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return ConnectivityObserver.Status.Unavailable
        return if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            ConnectivityObserver.Status.Available
        } else {
            ConnectivityObserver.Status.Unavailable
        }
    }
}