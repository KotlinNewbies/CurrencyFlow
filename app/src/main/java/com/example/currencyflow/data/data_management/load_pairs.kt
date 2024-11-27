package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.data.PairDataModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File

fun loadContainerData(context: Context): PairDataModel? {
    val file = File(context.filesDir, "pair_count.json")
    return if (file.exists()) {
        val jsonString = file.readText()
        // Deserializacja przy użyciu Kotlinx Serialization
        return try {
            Json.decodeFromString<PairDataModel>(jsonString)
        } catch (e: Exception) {
            // Obsługa błędów w przypadku nieudanej deserializacji
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}
