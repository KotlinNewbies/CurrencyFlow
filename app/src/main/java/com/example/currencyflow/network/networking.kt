package com.example.currencyflow.network

import com.example.currencyflow.data.C
import com.example.currencyflow.data.CurrencyViewModel
import com.example.currencyflow.data.DataModel
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

suspend fun networking(uuidString: String,
                       containers: List<C>,
                       currencyViewModel: CurrencyViewModel // Dodano ViewModel jako argument
): Pair<Boolean, Boolean> {
    var rcSuccess = false
    var dbSuccess = false

    val jsonData = Json.encodeToString(
        DataModel(
            id = uuidString,
            app = "curConv",
            v = "1.0.0",
        )
    )
    val url = URL("https://android.propages.pl")

    val result = withTimeoutOrNull(10000) {
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json") // Ustawienie nagłówka Content-Type
            doOutput = true
            val wr = OutputStreamWriter(outputStream)
            wr.write(jsonData)
            wr.flush()

            println("URL : $url")
            println("Response Code : $responseCode")

            // Obsługa odpowiedzi serwera
            val responseCode = responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Jeśli odpowiedź serwera jest OK (200)
                BufferedReader(InputStreamReader(inputStream)).use {
                    val sb = StringBuffer()

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        sb.append(inputLine)
                        inputLine = it.readLine()
                    }
                    val response = sb.toString()



                    // Obiekt JSON
                    val json: Map<String, JsonElement> = Json.parseToJsonElement(response).jsonObject
                    // Wywołanie funkcji do przetworzenia odpowiedzi z dynamicznymi kontenerami
                    processServerResponse(response, containers, currencyViewModel)
                    rcSuccess = json["rcSuccess"].toString().toBoolean()
                    dbSuccess = json["dbSuccess"].toString().toBoolean()
                    println("${json.entries}")
                }
            } else {
                // Obsługa innych kodów odpowiedzi
                println("Błąd serwera. Kod odpowiedzi: $responseCode")
            }
        }
    }
    if (result == null) {
        println("Przekroczono czas oczekiwania na odpowiedź z serwera")
        rcSuccess = false
        dbSuccess = false
    }
    return Pair(rcSuccess, dbSuccess)
}
