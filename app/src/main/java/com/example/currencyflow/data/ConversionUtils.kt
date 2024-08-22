package com.example.currencyflow.data

// Funkcja do uzyskania wartości konwersji
fun getConversionRate(conversionsMap: Map<String, Double>, from: String, to: String): Double? {
    val directRate = conversionsMap["$from-$to"]
    if (directRate != null) {
        return directRate
    }

    // Jeśli brak bezpośredniego kursu, sprawdź przez EUR
    val fromToEur = conversionsMap["$from-EUR"]
    val eurToTo = conversionsMap["EUR-$to"]

    return if (fromToEur != null && eurToTo != null) {
        fromToEur * eurToTo
    } else {
        null
    }
}