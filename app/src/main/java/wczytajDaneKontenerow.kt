//package com.example.currencyflow.dane.zarzadzanie_danymi
//
//import android.content.Context
//import com.example.currencyflow.data.model.ModelDanychKontenerow
//import kotlinx.serialization.json.Json
//import java.io.File
//
//fun wczytajDaneKontenerow(context: Context): ModelDanychKontenerow? {
//    val plik = File(context.filesDir, "liczba_kontenerow.json")
//    return if (plik.exists()) {
//        val ciagJson = plik.readText()
//        // Deserializacja przy użyciu Kotlinx Serialization
//        return try {
//            Json.decodeFromString<ModelDanychKontenerow>(ciagJson)
//        } catch (e: Exception) {
//            // Obsługa błędów w przypadku nieudanej deserializacji
//            e.printStackTrace()
//            null
//        }
//    } else {
//        null
//    }
//}
