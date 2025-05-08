package com.example.currencyflow.dane

import java.math.BigDecimal
import java.util.Locale

fun przeliczKonwersjeWalutowe(mapaKonwersji: Map<String, BigDecimal>, kontenery: List<C>): List<C> {
    return kontenery.map { kontener ->
        val mnoznikKonwersji = zdobadzMnoznikKonwersji(
            mapaKonwersji,
            kontener.from.symbol,
            kontener.to.symbol
        )
        val wartosc = kontener.amount.toBigDecimalOrNull()

        if (wartosc != null) {
            val wartoscPoKonwersji = wartosc * mnoznikKonwersji
            val zaokraglonaWartosc = String.format(Locale.US, "%.4f", wartoscPoKonwersji) // Określenie lokalizacji
            kontener.copy(result = zaokraglonaWartosc)
        } else {
            kontener
        }
    }
}
