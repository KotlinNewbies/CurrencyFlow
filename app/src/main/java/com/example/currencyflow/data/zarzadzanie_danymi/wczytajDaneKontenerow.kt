package com.example.currencyflow.data.zarzadzanie_danymi

import android.content.Context
import com.example.currencyflow.data.ModelDanychParKontenerow
import kotlinx.serialization.json.Json
import java.io.File

fun wczytajDaneKontenerow(context: Context): ModelDanychParKontenerow? {
    val plik = File(context.filesDir, "liczba_kontenerow.json")
    return if (plik.exists()) {
        val ciagJson = plik.readText()
        // Deserializacja przy użyciu Kotlinx Serialization
        return try {
            Json.decodeFromString<ModelDanychParKontenerow>(ciagJson)
        } catch (e: Exception) {
            // Obsługa błędów w przypadku nieudanej deserializacji
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}
