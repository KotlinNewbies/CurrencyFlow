package com.example.currencyflow.dane.zarzadzanie_danymi

import android.content.Context
import com.example.currencyflow.klasy.Waluta
import kotlinx.serialization.json.Json
import java.io.File

fun wczytajWybraneWaluty(context: Context): List<Waluta> {
    val plik = File(context.filesDir, "zapisane_waluty.json")
    return if (plik.exists()) {
        val ciagJson = plik.readText()
        // Deserializacja przy użyciu Kotlinx Serialization
        return try {
            Json.decodeFromString<List<Waluta>>(ciagJson)
        } catch (e: Exception) {
            // Obsługa błędów w przypadku nieudanej deserializacji
            e.printStackTrace()
            emptyList() // Zwrócenie pustej listy w przypadku błędu
        }
    } else {
        emptyList() // Zwrócenie pustej listy, jeśli plik nie istnieje
    }
}

