package com.example.currencyflow.data.data_management

import androidx.compose.runtime.MutableState
import com.example.currencyflow.classes.Currency
import com.example.currencyflow.data.C

fun updateSelectedCurrenciesWithNewContainer(containers: List<C>, selectedCurrencies: MutableState<List<Currency>>) {
    if (containers.isNotEmpty()) {
        val firstCurrency = containers.first().from // Pobierz pierwszą walutę z nowego kontenera
        selectedCurrencies.value = selectedCurrencies.value.toMutableList().apply {
            if (!contains(firstCurrency)) {
                add(firstCurrency) // Dodaj pierwszą walutę do listy wybranych walut, jeśli nie jest już zawarta
            }
        }
    }
}
