package com.example.currencyflow.data

import android.util.Log
import java.math.BigDecimal

fun processContainers(conversionsMap: Map<String, BigDecimal>, containers: List<C>) {
    for (container in containers) {
        val conversionValue = getConversionRate(
            conversionsMap,
            container.from.symbol,
            container.to.symbol
        )
        Log.d("CurrencyRates", "Conversion value for ${container.from.symbol} to ${container.to.symbol}: $conversionValue")
    }
}