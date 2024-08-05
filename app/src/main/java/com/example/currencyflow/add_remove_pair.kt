package com.example.currencyflow

import android.util.Log
import androidx.activity.ComponentActivity
import com.example.currencyflow.classes.Currency
import com.example.currencyflow.data.C
import com.example.currencyflow.data.data_management.saveContainerData

fun addContainer(containers: MutableList<C>, favoriteCurrencies: List<Currency>) {
    val firstFavoriteCurrency = favoriteCurrencies.firstOrNull()

    if (firstFavoriteCurrency != null) {
        containers.add(C(firstFavoriteCurrency, firstFavoriteCurrency, "", ""))
        Log.d("Dodawanie Kontenera", "Dodano nowy kontener dla waluty: $firstFavoriteCurrency")
    } else {
        Log.d("Dodawanie Kontenera", "Lista ulubionych walut jest pusta.")
        // Dodanie domyślnej pary walutowej lub obsługa zgodnie z logiką Twojej aplikacji
        containers.add(C(Currency.USD, Currency.GBP, "", ""))
    }
}
fun restoreInterface(containers: MutableList<C>, from: Currency, to: Currency, amount: String, result: String) {
    containers.add(C(from, to, amount, result))
}

fun removeContainerAtIndex(index: Int, containers: MutableList<C>, activity: ComponentActivity, pairCountLocal: Int) {
    Log.d("Usuwanie kontenera", "Usuwanie kontenera o indeksie: $index")
    if (index >= 0 && index < containers.size) {
        containers.removeAt(index)
        val newPairCount = pairCountLocal - 1
        saveContainerData(activity, newPairCount, containers)
        Log.d("Stan kontenerów", "Stan kontenerów po usunięciu: $containers, newPairCount = $newPairCount")
    }
}
