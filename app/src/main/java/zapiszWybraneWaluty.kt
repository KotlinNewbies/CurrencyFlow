//package com.example.currencyflow.dane.zarzadzanie_danymi
//
//import android.content.Context
//import com.example.currencyflow.data.model.Waluta
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.encodeToString
//import java.io.File
//
//fun zapiszWybraneWaluty(context: Context, wybraneWaluty: List<Waluta>) {
//    val nazwaPliku = "zapisane_waluty.json"
//    val plik = File(context.filesDir, nazwaPliku)
//
//    try {
//        // Serializacja listy wybranych walut do JSON
//        val ciagJson = Json.encodeToString(wybraneWaluty)
//
//        // Zapis do pliku
//        plik.writeText(ciagJson)
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//}
//
