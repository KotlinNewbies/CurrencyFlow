package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.classes.Currency
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File

fun loadSelectedCurrencies(context: Context): List<Currency> {
    val file = File(context.filesDir, "selected_currencies.json")
    return if (file.exists()) {
        val jsonString = file.readText()
        // Deserializacja przy użyciu Kotlinx Serialization
        return try {
            Json.decodeFromString<List<Currency>>(jsonString)
        } catch (e: Exception) {
            // Obsługa błędów w przypadku nieudanej deserializacji
            e.printStackTrace()
            emptyList() // Zwrócenie pustej listy w przypadku błędu
        }
    } else {
        emptyList() // Zwrócenie pustej listy, jeśli plik nie istnieje
    }
}

