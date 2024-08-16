package com.example.currencyflow.network

import com.example.currencyflow.data.Conversion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

val json = Json {
    isLenient = true // Zezwala na luźniejsze parsowanie
    ignoreUnknownKeys = true // Ignoruje nieznane klucze
}

fun processServerResponse(response: String) {
    val jsonObject = json.parseToJsonElement(response).jsonObject

    val conversionList = jsonObject["c"]?.jsonArray ?: return

    for (conversionElement in conversionList) {
        val conversion = json.decodeFromJsonElement<Conversion>(conversionElement)
        if (conversion.from == "EUR" && conversion.to == "USD") {
            println("Conversion value for EUR to CZK: ${conversion.value}")
        }
    }
}

