package com.example.currencyflow.data

import java.math.BigDecimal
import java.util.Locale

fun calculateCurrencyConversions(conversionsMap: Map<String, BigDecimal>, containers: List<C>): List<C> {
    return containers.map { container ->
        val conversionRate = getConversionRate(
            conversionsMap,
            container.from.symbol,
            container.to.symbol
        )
        val amount = container.amount.toBigDecimalOrNull()

        if (amount != null) {
            val convertedValue = amount * conversionRate
            val roundedValue = String.format(Locale.US, "%.4f", convertedValue) // Określenie lokalizacji
            container.copy(result = roundedValue)
        } else {
            container
        }
    }
}
