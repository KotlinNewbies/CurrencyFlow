package com.example.currencyflow.data

fun processContainers(conversionsMap: Map<String, Double>, containers: List<C>) {
    for (container in containers) {
        val conversionValue = getConversionRate(conversionsMap, container.from.symbol, container.to.symbol)
        if (conversionValue != null) {
            println("Conversion value for ${container.from.symbol} to ${container.to.symbol}: $conversionValue")
        } else {
            println("No conversion data available for ${container.from.symbol} to ${container.to.symbol}")
        }
    }
}