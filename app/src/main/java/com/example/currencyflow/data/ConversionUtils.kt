package com.example.currencyflow.data

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

// Funkcja do uzyskania wartości konwersji z określonym Locale
fun getConversionRate(conversionsMap: Map<String, BigDecimal>, from: String, to: String): BigDecimal {
    // Obsługuje przypadek, gdy 'from' i 'to' są takie same
    if (from == to) {
        return BigDecimal.ONE
    }

    // Sprawdź bezpośredni kurs
    val directRate = conversionsMap["$from-$to"]
    if (directRate != null) {
        return directRate
    }

    // Jeśli brak bezpośredniego kursu, sprawdź przez EUR
    val fromToEur = conversionsMap["$from-EUR"]
    val eurToTo = conversionsMap["EUR-$to"]

    // Oblicz kurs konwersji przez EUR
    val conversionRate = if (fromToEur != null && eurToTo != null) {
        fromToEur.multiply(eurToTo, MathContext.DECIMAL64)
    } else {
        null
    }

    // Jeśli konwersja jest dostępna, sformatuj ją przy użyciu określonego Locale
    return conversionRate?.setScale(4, RoundingMode.HALF_UP) ?: BigDecimal.ZERO // Zwraca 0, gdy konwersja nie jest dostępna
}
