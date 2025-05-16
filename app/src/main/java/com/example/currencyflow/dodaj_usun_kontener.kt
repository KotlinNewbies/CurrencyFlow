//package com.example.currencyflow
//
//import androidx.activity.ComponentActivity
//import com.example.currencyflow.klasy.Waluta
//import com.example.currencyflow.dane.C
//import com.example.currencyflow.dane.FavoriteCurrenciesViewModel
//import com.example.currencyflow.dane.zarzadzanie_danymi.zapiszDaneKontenerow
//
//fun dodajKontener(kontenery: MutableList<C>, ulubioneWaluty: List<Waluta>) {
//    val pierwszaUlubionaWaluta = ulubioneWaluty.firstOrNull()
////    val ostatniaUlubionaWaluta = ulubioneWaluty.last()
//
//    if (pierwszaUlubionaWaluta != null) {
//        kontenery.add(C(pierwszaUlubionaWaluta, pierwszaUlubionaWaluta, "", ""))
//    } else {
//        kontenery.add(C(Waluta.EUR, Waluta.USD, "", ""))
//    }
//}
//fun dodajKontenerJesliBrak(kontenery: MutableList<C>,
//                           ulubioneWaluty: List<Waluta>,
//                           activity: ComponentActivity,
//                           viewModel: FavoriteCurrenciesViewModel) {
//    if (ulubioneWaluty.isEmpty() && kontenery.size < 1) {
//        val domyslneWaluty = listOf(Waluta.EUR, Waluta.USD)
//        kontenery.add(C(Waluta.EUR, Waluta.USD, "", ""))
//
//        // Zaktualizowanie wybranych walut w ViewModel
//        viewModel.zaktualizujWybraneWaluty(Waluta.EUR, true)
//        viewModel.zaktualizujWybraneWaluty(Waluta.USD, true)
//
//        // Zapisanie walut przez ViewModel
//        viewModel.zapiszWybraneWaluty()
//
//        zapiszDaneKontenerow(activity, kontenery)
//    }
//}
//
//
//fun przywrocInterfejs(konteneryUI: MutableList<C>, from: Waluta, to: Waluta, amount: String, result: String) {
//    konteneryUI.add(C(from, to, amount, result))
//}
//
//fun usunWybranyKontener(indeksKontenera: Int, kontenery: MutableList<C>, activity: ComponentActivity) {
//    if (indeksKontenera >= 0 && indeksKontenera < kontenery.size) {
//        kontenery.removeAt(indeksKontenera)
//        zapiszDaneKontenerow(activity, kontenery)
//    }
//}
