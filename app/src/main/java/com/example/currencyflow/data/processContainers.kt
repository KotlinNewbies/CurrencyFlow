package com.example.currencyflow.data

import android.util.Log

fun processContainers(conversionsMap: Map<String, Double>, containers: List<C>) {
    for (container in containers) {
        val conversionValue = getConversionRate(conversionsMap, container.from.symbol, container.to.symbol)
        if (conversionValue != null) {
            Log.d("CurrencyRates", "Conversion value for ${container.from.symbol} to ${container.to.symbol}: $conversionValue")
        } else {
            Log.d("CurrencyRates", "No conversion data available for ${container.from.symbol} to ${container.to.symbol}")
        }
    }
}