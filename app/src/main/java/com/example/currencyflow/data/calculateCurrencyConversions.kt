package com.example.currencyflow.data

import java.util.Locale

fun calculateCurrencyConversions(conversionsMap: Map<String, Double>, containers: List<C>): List<C> {
    return containers.map { container ->
        val conversionRate = getConversionRate(conversionsMap, container.from.symbol, container.to.symbol)
        val amount = container.amount.toDoubleOrNull()

        if (conversionRate != null && amount != null) {
            val convertedValue = amount * conversionRate
            val roundedValue = String.format(Locale.US, "%.4f", convertedValue) // Określenie lokalizacji
            container.copy(result = roundedValue)
        } else {
            container
        }
    }
}
