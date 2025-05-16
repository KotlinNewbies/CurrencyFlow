//package com.example.currencyflow.dane.zarzadzanie_danymi
//
//import android.content.Context
//import com.example.currencyflow.UUIDMenadzer
//import com.example.currencyflow.dane.ModelDanychUzytkownika
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import java.io.File
//import java.io.FileWriter
//import java.io.PrintWriter
//import java.lang.Exception
//
//fun zapiszDane(context: Context) {
//    val ciagUUID = UUIDMenadzer.zdobadzUUID()
//    val modelDanychUzytkownika = ModelDanychUzytkownika(ciagUUID, "curConv", "1.0.0")
//
//    val nazwaPliku = "dane_uzytkownika.json"
//    val plik = File(context.filesDir, nazwaPliku)
//    try {
//        PrintWriter(FileWriter(plik)).use {
//            // Zapis do pliku JSON przy użyciu Kotlinx Serialization
//            val ciagJson = Json.encodeToString(modelDanychUzytkownika) // serializacja
//            it.write(ciagJson)
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//}
