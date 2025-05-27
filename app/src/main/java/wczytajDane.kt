//package com.example.currencyflow.dane.zarzadzanie_danymi
//
//import android.content.Context
//import com.example.currencyflow.dane.ModelDanychUzytkownika
//import kotlinx.serialization.json.Json
//import java.io.File
//
//fun wczytajDane(context: Context): ModelDanychUzytkownika? {
//    val nazwaPliku = "dane_uzytkownika.json"
//    val plik = File(context.filesDir, nazwaPliku)
//    if (plik.exists()) {
//        val ciagJson = plik.readText()
//        // Deserializacja przy użyciu Kotlinx Serialization
//        return try {
//            Json.decodeFromString<ModelDanychUzytkownika>(ciagJson)
//        } catch (e: Exception) {
//            // Obsługa błędów w przypadku nieudanej deserializacji
//            e.printStackTrace()
//            null
//        }
//    }
//    return null
//}
//
