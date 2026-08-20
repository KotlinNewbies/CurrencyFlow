package com.fluida.currencyflow.data.repository

import android.util.Log
import com.fluida.currencyflow.data.PremiumManager
import com.fluida.currencyflow.data.model.CurrencyType
import com.fluida.currencyflow.data.model.Konwersja
import com.fluida.currencyflow.data.model.ModelDanychUzytkownika
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
    private val premiumManager: PremiumManager
) {
    private val jsonParser = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    // Cache walut dla błyskawicznego wyszukiwania O(1)
    private val walutyMap by lazy { Waluta.entries.associateBy { it.symbol } }

    fun pobierzAktualneKursy(identyfikatorUzytkownika: String): Flow<Map<String, Double>> = flow {
        // 1. Czekanie na sieć (bez zmian, to dobra praktyka)
        val status = withTimeoutOrNull(15.seconds) {
            connectivityObserver.observe().first { it == ConnectivityObserver.Status.Available }
        }

        if (status != ConnectivityObserver.Status.Available) {
            Log.w(TAG_REPO, "Brak połączenia z siecią.")
            emit(emptyMap())
            return@flow
        }

        // 2. Przygotowanie żądania
        val daneJson = jsonParser.encodeToString(
            ModelDanychUzytkownika(id = identyfikatorUzytkownika, app = "curConv", v = "1.0.0")
        )
        val url = URL("https://android.propages.pl")

        // 3. Pobieranie danych
        val wynik = withTimeoutOrNull(10.seconds) {
            try {
                wykonajZapytanieSieciowe(url, daneJson)
            } catch (e: Exception) {
                Log.e(TAG_REPO, "Błąd podczas pobierania kursów", e)
                emptyMap()
            }
        } ?: emptyMap()

        emit(wynik)
    }.flowOn(Dispatchers.IO)

    private fun wykonajZapytanieSieciowe(url: URL, body: String): Map<String, Double> {
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            connectTimeout = 5000
            readTimeout = 5000
        }

        conn.outputStream.use { it.write(body.toByteArray()) }

        if (conn.responseCode != HttpURLConnection.HTTP_OK) {
            Log.e(TAG_REPO, "Błąd HTTP: ${conn.responseCode}")
            return emptyMap()
        }

        // ZABEZPIECZENIE: Sprawdzamy czy to na pewno JSON, a nie HTML
        val contentType = conn.contentType ?: ""
        if (!contentType.contains("application/json")) {
            val fragment = conn.inputStream.bufferedReader().use { it.readText().take(100) }
            Log.e(TAG_REPO, "Serwer zwrócił niepoprawny format: $contentType. Fragment: $fragment")
            return emptyMap()
        }

        val odpowiedzRaw = conn.inputStream.bufferedReader().use { it.readText() }

        return try {
            // WYDAJNOŚĆ: Parsujemy wszystko na raz
            val response = jsonParser.decodeFromString<KursyResponse>(odpowiedzRaw)

            // Aktualizuj stan reklam na podstawie odpowiedzi serwera
            premiumManager.setAdsEnabled(response.showAds)

            if (response.rcSuccess && response.c != null) {
                przetworzListeKursow(response.c)
            } else {
                Log.w(TAG_REPO, "Serwer zwrócił sukces=false lub brak kursów.")
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e(TAG_REPO, "Błąd parsowania JSON. Dane: ${odpowiedzRaw.take(200)}", e)
            emptyMap()
        }
    }

    private fun przetworzListeKursow(lista: List<Konwersja>): Map<String, Double> {
        val przetworzone = mutableMapOf<String, Double>()

        for (item in lista) {
            val wartosc = item.value.toDouble()
            val walutaFrom = walutyMap[item.from]
            val walutaTo = walutyMap[item.to]

            // Optymalizacja logiki krypto (unikamy redundantnych find)
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