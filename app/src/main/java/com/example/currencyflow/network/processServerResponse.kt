package com.example.currencyflow.network

import com.example.currencyflow.data.C
import com.example.currencyflow.data.Conversion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

val json = Json {
    isLenient = true // Zezwala na luźniejsze parsowanie
    ignoreUnknownKeys = true // Ignoruje nieznane klucze
}

fun processServerResponse(response: String, containers: List<C>) {
    val jsonObject = json.parseToJsonElement(response).jsonObject
    val conversionList = jsonObject["c"]?.jsonArray ?: return

    // Mapa konwersji z JSON-a
    val conversionsMap = mutableMapOf<String, Double>()
    for (conversionElement in conversionList) {
        val conversion = json.decodeFromJsonElement<Conversion>(conversionElement)
        val conversionKey = "${conversion.from}-${conversion.to}"
        val conversionValue = conversion.value.toString().toDoubleOrNull()
        if (conversionValue != null) {
            conversionsMap[conversionKey] = conversionValue
        }
    }

    // Funkcja do uzyskania wartości konwersji
    fun getConversionRate(from: String, to: String): Double? {
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

    // Przetwarzanie kontenerów
    for (container in containers) {
        val conversionValue = getConversionRate(container.from.symbol, container.to.symbol)
        if (conversionValue != null) {
            println("Conversion value for ${container.from.symbol} to ${container.to.symbol}: $conversionValue")
        } else {
            println("No conversion data available for ${container.from.symbol} to ${container.to.symbol}")
        }
    }
}


