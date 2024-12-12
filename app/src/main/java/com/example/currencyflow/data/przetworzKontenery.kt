package com.example.currencyflow.data

import android.util.Log
import java.math.BigDecimal

fun przetworzKontenery(mapaKonwersji: Map<String, BigDecimal>, kontenery: List<C>) {
    for (kontener in kontenery) {
        val wartoscKonwersji = zdobadzMnoznikKonwersji(
            mapaKonwersji,
            kontener.from.symbol,
            kontener.to.symbol
        )
        //Log.d("MnoznikiWalut", "Konwersja dla: ${kontener.from.symbol} to ${kontener.to.symbol}: $wartoscKonwersji")
    }
}