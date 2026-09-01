package com.fluida.currencyflow.data.repository

import android.util.Log
import com.fluida.currencyflow.data.AuthManager
import com.fluida.currencyflow.data.PremiumManager
import com.fluida.currencyflow.data.model.CurrencyType
import com.fluida.currencyflow.data.model.Konwersja
import com.fluida.currencyflow.data.model.Waluta
import com.fluida.currencyflow.util.ConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

private const val TAG_REPO = "WalutyRepository"

// Model pomocniczy dla wydajnego parsowania całej odpowiedzi na raz
@Serializable
private data class KursyResponse(
    val rcSuccess: Boolean,
    val dbSuccess: Boolean = false,
    val showAds: Boolean = true,
    val c: List<Konwersja>? = null
)

@Singleton
class WalutyRepository @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
    private val premiumManager: PremiumManager,
    private val authManager: AuthManager
) {
    private val jsonParser = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // Cache walut dla błyskawicznego wyszukiwania O(1)
    private val walutyMap by lazy { Waluta.entries.associateBy { it.symbol } }

    fun pobierzAktualneKursy(identyfikatorUzytkownika: String): Flow<Map<String, Double>> = flow {
        // 1. Check sieci
        if (connectivityObserver.getCurrentStatus() != ConnectivityObserver.Status.Available) {
            val status = withTimeoutOrNull(8.seconds) {
                connectivityObserver.observe().first { it == ConnectivityObserver.Status.Available }
            }
            if (status != ConnectivityObserver.Status.Available) {
                Log.w(TAG_REPO, "Brak połączenia z siecią.")
                emit(emptyMap())
                return@flow
            }
        }

        // 2. Budowanie żądania (ręczna mapa, aby mieć 100% kontroli nad JSONem)
        val requestMap = mutableMapOf<String, String>(
            "id" to identyfikatorUzytkownika,
            "app" to "curConv",
            "v" to "1.0.0"
        )
        authManager.apiKey.value?.let { if (it.isNotBlank()) requestMap["api_key"] = it }

        val bodyJson = jsonParser.encodeToString(requestMap)
        val url = URL("https://android.propages.pl")

        // 3. Strzał do serwera
        val wynik = try {
            wykonajZapytanieSieciowe(url, bodyJson)
        } catch (e: Exception) {
            Log.e(TAG_REPO, "Błąd krytyczny: ${e.message}")
            emptyMap<String, Double>()
        }

        emit(wynik)
    }.flowOn(Dispatchers.IO)

    private fun wykonajZapytanieSieciowe(url: URL, body: String): Map<String, Double> {
        Log.d(TAG_REPO, "POST -> $url. Body: $body")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            connectTimeout = 10000
            readTimeout = 10000
        }

        try {
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG_REPO, "Błąd HTTP: ${conn.responseCode}")
                return emptyMap()
            }

            val rawResponse = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            Log.d(TAG_REPO, "RAW Response: ${rawResponse.take(200)}...")

            val response = jsonParser.decodeFromString<KursyResponse>(rawResponse)

            // Aktualizuj stan reklam
            premiumManager.setAdsEnabled(response.showAds)
            if (response.showAds && authManager.apiKey.value != null) {
                authManager.updatePremiumStatus(false)
            }

            return if (response.rcSuccess && response.c != null) {
                przetworzListeKursow(response.c)
            } else {
                Log.w(TAG_REPO, "rcSuccess=false lub c=null")
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e(TAG_REPO, "Błąd parsowania/sieci: ${e.message}")
            return emptyMap()
        } finally {
            conn.disconnect()
        }
    }

    private fun przetworzListeKursow(lista: List<Konwersja>): Map<String, Double> {
        val przetworzone = mutableMapOf<String, Double>()

        for (item in lista) {
            val wartosc = item.value.toDouble()
            val walutaFrom = walutyMap[item.from]
            val walutaTo = walutyMap[item.to]

            when {
                walutaTo?.type == CurrencyType.CRYPTO && item.from == "EUR" -> {
                    przetworzone["EUR-${item.to}"] = 1.0 / wartosc
                    przetworzone["${item.to}-EUR"] = wartosc
                }
                walutaFrom?.type == CurrencyType.CRYPTO && item.to == "EUR" -> {
                    przetworzone["${item.from}-EUR"] = 1.0 / wartosc
                    przetworzone["EUR-${item.from}"] = wartosc
                }
                else -> {
                    przetworzone["${item.from}-${item.to}"] = wartosc
                }
            }
        }
        return przetworzone
    }
}
