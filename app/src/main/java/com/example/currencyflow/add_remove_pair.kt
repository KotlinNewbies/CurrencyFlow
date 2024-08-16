package com.example.currencyflow

import android.util.Log
import androidx.activity.ComponentActivity
import com.example.currencyflow.classes.Currency
import com.example.currencyflow.data.C
import com.example.currencyflow.data.data_management.loadSelectedCurrencies
import com.example.currencyflow.data.data_management.saveContainerData
import com.example.currencyflow.data.data_management.saveSelectedCurrencies

fun addContainer(containers: MutableList<C>, favoriteCurrencies: List<Currency>) {
    val firstFavoriteCurrency = favoriteCurrencies.firstOrNull()

    if (firstFavoriteCurrency != null) {
        containers.add(C(firstFavoriteCurrency, firstFavoriteCurrency, "", ""))
        Log.d("Dodawanie Kontenera", "Dodano nowy kontener dla waluty: $firstFavoriteCurrency")
    } else {
        Log.d("Dodawanie Kontenera", "Lista ulubionych walut jest pusta.")
        containers.add(C(Currency.EUR, Currency.USD, "", ""))
    }
}
fun addContainerIfEmpty(containers: MutableList<C>, favoriteCurrencies: List<Currency>, activity: ComponentActivity) {
        if (favoriteCurrencies.isEmpty() && containers.size < 1) {
            val defaultCurrencies = listOf(Currency.EUR, Currency.USD)
            containers.add(C(Currency.EUR, Currency.USD, "", ""))
            saveSelectedCurrencies(activity, defaultCurrencies)
            saveContainerData(activity, containers)
        }
}

fun restoreInterface(containers: MutableList<C>, from: Currency, to: Currency, amount: String, result: String) {
    containers.add(C(from, to, amount, result))
}

fun removeContainerAtIndex(index: Int, containers: MutableList<C>, activity: ComponentActivity) {
    Log.d("Usuwanie kontenera", "Usuwanie kontenera o indeksie: $index")
    if (index >= 0 && index < containers.size) {
        containers.removeAt(index)
        // val newPairCount = pairCountLocal - 1
        saveContainerData(activity, containers)
        Log.d("Stan kontenerów", "Stan kontenerów po usunięciu: ${containers.size}")
    }
}
