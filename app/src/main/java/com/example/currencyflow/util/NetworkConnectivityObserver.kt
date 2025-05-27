package com.example.currencyflow.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
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
            // 1. Natychmiast wyślij aktualny stan
            val initialStatus = getCurrentStatus()
            Log.d("NetworkConnectivityObserver", "Initial network status: $initialStatus") // Dodaj log
            trySend(initialStatus).isSuccess // Lub .getOrThrow() jeśli chcesz być pewny, że się udało

            // 2. Następnie zarejestruj callback do obserwacji zmian
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d("NetworkConnectivityObserver", "NetworkCallback: onAvailable") // Dodaj log
                    trySend(ConnectivityObserver.Status.Available)
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    Log.d("NetworkConnectivityObserver", "NetworkCallback: onLosing") // Dodaj log
                    trySend(ConnectivityObserver.Status.Losing)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d("NetworkConnectivityObserver", "NetworkCallback: onLost") // Dodaj log
                    trySend(ConnectivityObserver.Status.Lost)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Log.d("NetworkConnectivityObserver", "NetworkCallback: onUnavailable") // Dodaj log
                    trySend(ConnectivityObserver.Status.Unavailable)
                }
            }

            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            Log.d("NetworkConnectivityObserver", "Registering network callback") // Dodaj log
            connectivityManager.registerNetworkCallback(networkRequest, callback)

            awaitClose {
                Log.d("NetworkConnectivityObserver", "Unregistering network callback") // Dodaj log
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }

    override fun getCurrentStatus(): ConnectivityObserver.Status {
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