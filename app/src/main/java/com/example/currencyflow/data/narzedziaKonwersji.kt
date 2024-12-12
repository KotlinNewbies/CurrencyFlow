package com.example.currencyflow.data

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

fun zdobadzMnoznikKonwersji(mapaKonwersji: Map<String, BigDecimal>, from: String, to: String): BigDecimal {
    // Gdy from i to są takie same
    if (from == to) {
        return BigDecimal.ONE
    }

    // Bezpośredni kurs
    val bezposredniMnoznik = mapaKonwersji["$from-$to"]
    if (bezposredniMnoznik != null) {
        return bezposredniMnoznik
    }

    // Jeśli brak bezpośredniego kursu
    val fromToEur = mapaKonwersji["$from-EUR"]
    val eurToTo = mapaKonwersji["EUR-$to"]

    // Obliczenie kursu przez Euro
    val mnoznikKonwersji = if (fromToEur != null && eurToTo != null) {
        fromToEur.multiply(eurToTo, MathContext.DECIMAL64)
    } else {
        null
    }

    // formatowanie
    return mnoznikKonwersji?.setScale(4, RoundingMode.HALF_UP) ?: BigDecimal.ZERO // Gdy nie ma dostępnej konwersji, zwraca 0
}
