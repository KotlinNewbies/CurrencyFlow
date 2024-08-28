package com.example.currencyflow.network

import com.example.currencyflow.data.C
import com.example.currencyflow.data.Conversion
import com.example.currencyflow.data.CurrencyViewModel
import com.example.currencyflow.data.processContainers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.math.BigDecimal

val json = Json {
    isLenient = true // Zezwala na luźniejsze parsowanie
    ignoreUnknownKeys = true // Ignoruje nieznane klucze
}

fun processServerResponse(
    response: String,
    containers: List<C>,
    currencyViewModel: CurrencyViewModel
) {
    val jsonObject = json.parseToJsonElement(response).jsonObject
    val conversionList = jsonObject["c"]?.jsonArray ?: return

    // Mapa konwersji z JSON-a
    val conversionsMap = mutableMapOf<String, BigDecimal>()
    for (conversionElement in conversionList) {
        val conversion = json.decodeFromJsonElement<Conversion>(conversionElement)
        val conversionKey = "${conversion.from}-${conversion.to}"
        val conversionValue = conversion.value.toString().toBigDecimalOrNull()
        if (conversionValue != null) {
            conversionsMap[conversionKey] = conversionValue
        }
    }
    // Przetwarzanie kontenerów
    processContainers(conversionsMap, containers)

    // Aktualizacja ViewModelu z nowymi kursami walut
    currencyViewModel.updateCurrencyRates(conversionsMap)
}


