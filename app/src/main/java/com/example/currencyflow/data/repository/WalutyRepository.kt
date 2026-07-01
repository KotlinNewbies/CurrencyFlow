package com.example.currencyflow.data.repository

import android.util.Log
import com.example.currencyflow.data.model.CurrencyType
import com.example.currencyflow.data.model.Konwersja
import com.example.currencyflow.data.model.ModelDanychUzytkownika
import com.example.currencyflow.data.model.Waluta
import com.example.currencyflow.util.ConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.toBoolean
import kotlin.time.Duration.Companion.seconds

private const val TAG_REPO = "WalutyRepository"

@Singleton
class WalutyRepository @Inject constructor(
    private val connectivityObserver: ConnectivityObserver // DODAJEMY
) {

    private val jsonParser = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    fun pobierzAktualneKursy(identyfikatorUzytkownika: String): Flow<Map<String, Double>> {
        return flow {
            val statusDostepnyNaCzas: ConnectivityObserver.Status? =
                withTimeoutOrNull(15.seconds) { // Np. 15 sekund
                Log.d(TAG_REPO, "Rozpoczynam obserwację statusu sieci w withTimeoutOrNull...")
                connectivityObserver.observe().first { it == ConnectivityObserver.Status.Available }
            }

            if (statusDostepnyNaCzas != ConnectivityObserver.Status.Available) {
                if (statusDostepnyNaCzas == null) {
                    Log.w(TAG_REPO, "Przekroczono czas oczekiwania na dostępne połączenie sieciowe (wynik withTimeoutOrNull to null).")
                } else {
                    Log.w(TAG_REPO, "Połączenie sieciowe nie stało się 'Available' w wymaganym czasie. Status: $statusDostepnyNaCzas")
                }
                emit(emptyMap())
                return@flow
            }

            var rcSuccess: Boolean
            // var dbSuccess = false // Zastanów się, czy dbSuccess jest częścią wyniku pobierania KURSÓW

            val daneDoWyslaniaJson = jsonParser.encodeToString(
                ModelDanychUzytkownika(
                    id = identyfikatorUzytkownika,
                    app = "curConv",
                    v = "1.0.0",
                )
            )
            val punktKoncowy = URL("https://android.propages.pl") // Przenieś do stałej lub konfiguracji

            Log.d(TAG_REPO, "Wysyłanie żądania kursów do: $punktKoncowy z danymi: $daneDoWyslaniaJson")

            val mapaKursowWynik: Map<String, Double> = withTimeoutOrNull(10000) { // 10 sekund timeout
                try {
                    val connection = punktKoncowy.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true // Niezbędne dla POST

                    OutputStreamWriter(connection.outputStream).use { writer ->
                        writer.write(daneDoWyslaniaJson)
                        writer.flush()
                    }

                    val kodOdpowiedzi = connection.responseCode
                    Log.d(TAG_REPO, "Odpowiedź serwera - Kod: $kodOdpowiedzi")

                    if (kodOdpowiedzi == HttpURLConnection.HTTP_OK) {
                        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                            val odpowiedzString = reader.readText()
                            logLargeString(TAG_REPO, "Odpowiedź serwera (surowa): $odpowiedzString")

                            // Przetwarzanie odpowiedzi
                            val obiektJsonOdpowiedzi = jsonParser.parseToJsonElement(odpowiedzString).jsonObject
                            rcSuccess = obiektJsonOdpowiedzi["rcSuccess"]?.toString()?.toBoolean() ?: false
                            // dbSuccess = obiektJsonOdpowiedzi["dbSuccess"]?.toString()?.toBoolean() ?: false

                            if (rcSuccess) {
                                val listaKonwersjiJson = obiektJsonOdpowiedzi["c"]?.jsonArray
                                if (listaKonwersjiJson != null) {
                                    val przetworzoneKursy = mutableMapOf<String, Double>()
                                    for (pojedynczyElementKonwersji in listaKonwersjiJson) {
                                        try {
                                            val konwersja = jsonParser.decodeFromJsonElement<Konwersja>(pojedynczyElementKonwersji)
                                            val wartoscRaw = konwersja.value.toDouble()

                                            val walutaFrom = Waluta.entries.find { it.symbol == konwersja.from }
                                            val walutaTo = Waluta.entries.find { it.symbol == konwersja.to }

                                            if (walutaTo?.type == CurrencyType.CRYPTO && konwersja.from == "EUR") {
                                                // API przysyła: EUR -> BTC = 60000 (cena 1 BTC).
                                                // My zapisujemy to tak, żeby matematyka w ViewModelu działała:
                                                przetworzoneKursy["EUR-${konwersja.to}"] = 1.0 / wartoscRaw  // EUR-BTC = 0.000016
                                                przetworzoneKursy["${konwersja.to}-EUR"] = wartoscRaw        // BTC-EUR = 60000
                                            }
                                            else if (walutaFrom?.type == CurrencyType.CRYPTO && konwersja.to == "EUR") {
                                                // API przysyła: BTC -> EUR = 0.000016.
                                                przetworzoneKursy["${konwersja.from}-EUR"] = 1.0 / wartoscRaw // BTC-EUR = 60000
                                                przetworzoneKursy["EUR-${konwersja.from}"] = wartoscRaw        // EUR-BTC = 0.000016
                                            }
                                            else {
                                                // Dla FIAT (PLN, USD) zostawiamy bez zmian
                                                przetworzoneKursy["${konwersja.from}-${konwersja.to}"] = wartoscRaw
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG_REPO, "Błąd dekodowania: $pojedynczyElementKonwersji", e)
                                        }
                                    }
                                    Log.d(TAG_REPO, "Pomyślnie przetworzono kursy: $przetworzoneKursy")
                                    przetworzoneKursy // Zwróć mapę kursów
                                } else {
                                    Log.w(TAG_REPO, "Pole 'c' (lista konwersji) jest puste lub nie istnieje w odpowiedzi.")
                                    emptyMap()
                                }
                            } else {
                                Log.w(TAG_REPO, "Serwer zwrócił rcSuccess = false.")
                                emptyMap()
                            }
                        }
                    } else {
                        Log.e(TAG_REPO, "Błąd serwera. Kod odpowiedzi: $kodOdpowiedzi")
                        // Możesz tu odczytać errorStream dla więcej informacji, jeśli serwer coś zwraca
                        // connection.errorStream?.bufferedReader()?.use { Log.e(TAG_REPO, it.readText()) }
                        emptyMap()
                    }
                } catch (e: IOException) {
                    Log.e(TAG_REPO, "Błąd IO podczas połączenia sieciowego", e)
                    emptyMap()
                } catch (e: Exception) { // Ogólny wyjątek dla innych problemów (np. serializacja)
                    Log.e(TAG_REPO, "Nieoczekiwany błąd podczas pobierania kursów", e)
                    emptyMap()
                }
            } ?: run {
                Log.w(TAG_REPO, "Przekroczono czas oczekiwania na odpowiedź z serwera.")
                emptyMap() // Timeout
            }
            emit(mapaKursowWynik)
        }.flowOn(Dispatchers.IO) // Wykonuj operacje sieciowe w tle


    }
    private fun logLargeString(tag: String, content: String) {
        val maxLogSize = 3000
        for (i in 0..content.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > content.length) content.length else end
            Log.d(tag, content.substring(start, end))
        }
    }
}