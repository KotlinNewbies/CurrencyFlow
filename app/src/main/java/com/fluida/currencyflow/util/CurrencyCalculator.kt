package com.fluida.currencyflow.util

import android.util.Log
import com.fluida.currencyflow.data.SettingsManager
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyCalculator @Inject constructor(
    private val settingsManager: SettingsManager
) {

    fun calculateResult(
        amount: String,
        fromSymbol: String,
        toSymbol: String,
        rates: Map<String, Double>
    ): String {
        if (amount.isBlank()) return ""
        
        val kwotaDouble = amount.replace(',', '.').toDoubleOrNull() ?: return "Error"
        
        if (fromSymbol == toSymbol) return format(kwotaDouble)
        
        // Jeśli nie mamy kursów, a waluty są różne, zwracamy sformatowane zero
        if (rates.isEmpty()) return format(0.0)

        val multiplier = getMultiplier(rates, fromSymbol, toSymbol)
        return if (multiplier == 0.0) format(0.0) else format(kwotaDouble * multiplier)
    }

    private fun getMultiplier(rates: Map<String, Double>, from: String, to: String): Double {
        // 1. Bezpośredni kurs
        rates["$from-$to"]?.let { return it }

        // 2. Przez EUR (mnożenie)
        val fromToEur = rates["$from-EUR"]
        val eurToTo = rates["EUR-$to"]
        if (fromToEur != null && eurToTo != null) return fromToEur * eurToTo

        // 3. Przez EUR (dzielenie)
        val eurToFrom = rates["EUR-$from"]
        if (eurToFrom != null && eurToFrom != 0.0 && eurToTo != null) return eurToTo / eurToFrom

        Log.w("CurrencyCalculator", "Brak ścieżki konwersji dla $from -> $to")
        return 0.0
    }

    private fun format(value: Double): String {
        val places = settingsManager.decimalPlaces.value
        return String.format(Locale.US, "%.${places}f", value)
    }
}
