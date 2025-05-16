package com.example.currencyflow.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.currencyflow.data.model.Konwersja
import com.example.currencyflow.data.model.ModelDanychUzytkownika
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
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
import kotlin.text.toDoubleOrNull

private const val TAG_REPO = "WalutyRepository"

@Singleton
class WalutyRepository @Inject constructor(
    @ApplicationContext private val appContext: Context // Do sprawdzania stanu sieci
) {

    private val jsonParser = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    private fun sprawdzDostepnoscInternetu(): Boolean {
        val menadzerLacznosci = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val siec = menadzerLacznosci.activeNetwork ?: return false
        val mozliwosci = menadzerLacznosci.getNetworkCapabilities(siec) ?: return false
        return mozliwosci.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                mozliwosci.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun pobierzAktualneKursy(identyfikatorUzytkownika: String): Flow<Map<String, Double>> {
        return flow {
            if (!sprawdzDostepnoscInternetu()) {
                Log.w(TAG_REPO, "Brak połączenia z internetem.")
                emit(emptyMap()) // Emituj pustą mapę lub rzuć wyjątek/emituj stan błędu
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
                            Log.d(TAG_REPO, "Odpowiedź serwera (surowa): $odpowiedzString")

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
                                            val kluczKonwersji = "${konwersja.from}-${konwersja.to}"
                                            // Upewnij się, że 'konwersja.value' jest stringiem lub go odpowiednio obsłuż
                                            val wartoscKonwersji = konwersja.value.toString().toDoubleOrNull()
                                            if (wartoscKonwersji != null) {
                                                przetworzoneKursy[kluczKonwersji] = wartoscKonwersji
                                            } else {
                                                Log.w(TAG_REPO, "Nie można sparsować wartości dla konwersji: $konwersja")
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG_REPO, "Błąd podczas dekodowania elementu konwersji: $pojedynczyElementKonwersji", e)
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
}