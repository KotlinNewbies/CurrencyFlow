package com.example.currencyflow.siec

import com.example.currencyflow.data.C
import com.example.currencyflow.data.WalutyViewModel
import com.example.currencyflow.data.ModelDanychUzytkownika
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

suspend fun zadanieSieci(identyfikatorUzytkownika: String,
                         kontenery: List<C>,
                         walutyViewModel: WalutyViewModel
): Pair<Boolean, Boolean> {
    var rcSuccess = false
    var dbSuccess = false

    val daneJson = Json.encodeToString(
        ModelDanychUzytkownika(
            id = identyfikatorUzytkownika,
            app = "curConv",
            v = "1.0.0",
        )
    )
    val punktKoncowy = URL("https://android.propages.pl")

    val rezultat = withTimeoutOrNull(10000) {
        try {
            with(punktKoncowy.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                val zapisywacz = OutputStreamWriter(outputStream)
                zapisywacz.write(daneJson)
                zapisywacz.flush()

                println("URL : $punktKoncowy")
                println("Kod_odpowiedzi : $responseCode")

                // Obsługa odpowiedzi serwera
                val kodOdpowiedzi = responseCode
                if (kodOdpowiedzi == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(inputStream)).use {
                        val sb = StringBuffer()
                        var liniaWejscia = it.readLine()
                        while (liniaWejscia != null) {
                            sb.append(liniaWejscia)
                            liniaWejscia = it.readLine()
                        }
                        val odpowiedz = sb.toString()

                        // Obiekt JSON
                        val obiektJson: Map<String, JsonElement> = Json.parseToJsonElement(odpowiedz).jsonObject
                        przetworzOdpowiedzSerwera(odpowiedz, walutyViewModel)
                        rcSuccess = obiektJson["rcSuccess"].toString().toBoolean()
                        dbSuccess = obiektJson["dbSuccess"].toString().toBoolean()
                        println("${obiektJson.entries}")
                    }
                } else {
                    // Obsługa innych kodów odpowiedzi
                    println("Błąd serwera. Kod odpowiedzi: $kodOdpowiedzi")
                }
            }
        } catch (e: IOException) {
            // Złap wyjątek połączenia sieciowego
            println("Błąd połączenia: ${e.message}")
            rcSuccess = false
            dbSuccess = false
        }
    }
    if (rezultat == null) {
        println("Przekroczono czas oczekiwania na odpowiedź z serwera")
        rcSuccess = false
        dbSuccess = false
    }
    return Pair(rcSuccess, dbSuccess)
}
