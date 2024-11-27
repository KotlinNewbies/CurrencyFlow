package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.classes.Currency
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

fun saveSelectedCurrencies(context: Context, selectedCurrencies: List<Currency>) {
    val fileName = "selected_currencies.json"
    val file = File(context.filesDir, fileName)

    try {
        // Serializacja listy selectedCurrencies do JSON
        val jsonString = Json.encodeToString(selectedCurrencies)

        // Zapis do pliku
        file.writeText(jsonString)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

