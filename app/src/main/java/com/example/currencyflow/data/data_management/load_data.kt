package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.data.DataModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File

fun loadData(context: Context): DataModel? {
    val fileName = "user_data.json"
    val file = File(context.filesDir, fileName)
    if (file.exists()) {
        val jsonString = file.readText()
        // Deserializacja przy użyciu Kotlinx Serialization
        return try {
            Json.decodeFromString<DataModel>(jsonString)
        } catch (e: Exception) {
            // Obsługa błędów w przypadku nieudanej deserializacji
            e.printStackTrace()
            null
        }
    }
    return null
}

