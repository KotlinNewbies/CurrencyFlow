//package com.example.currencyflow.siec
//
//import com.example.currencyflow.dane.Konwersja
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.decodeFromJsonElement
//import kotlinx.serialization.json.jsonArray
//import kotlinx.serialization.json.jsonObject
//
//val json = Json {
//    isLenient = true // Pozwala na luźniejsze parsowanie
//    ignoreUnknownKeys = true // Ignorowanie nieznane klucze
//}
//
//fun przetworzOdpowiedzSerwera(
//    odpowiedz: String,
//    walutyViewModel: WalutyViewModel
//) {
//    val obiektJson = json.parseToJsonElement(odpowiedz).jsonObject
//    val listaKonwersji = obiektJson["c"]?.jsonArray ?: return
//
//    // Mapa konwersji z JSONa
//    val mapaKonwersji = mutableMapOf<String, Double>()
//    for (pojedynczyElementKonwersji in listaKonwersji) {
//        val konwersja = json.decodeFromJsonElement<Konwersja>(pojedynczyElementKonwersji)
//        val kluczKonwersji = "${konwersja.from}-${konwersja.to}"
//        val wartoscKonwersji = konwersja.value.toString().toDoubleOrNull()
//        if (wartoscKonwersji != null) {
//            mapaKonwersji[kluczKonwersji] = wartoscKonwersji
//        }
//    }
//
//    // Aktualizacja ViewModelu z nowymi kursami walut
//    walutyViewModel.zaktualizujMapeWalut(mapaKonwersji)
//}
//
//
