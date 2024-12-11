package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.UUIDManager
import com.example.currencyflow.data.DataModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.lang.Exception

fun saveData(context: Context) {
    val uuidString = UUIDManager.getUUID()
    val dataModel = DataModel(uuidString, "curConv", "1.0.0")

    val fileName = "user_data.json"
    val file = File(context.filesDir, fileName)
    try {
        PrintWriter(FileWriter(file)).use {
            // Zapis do pliku JSON przy użyciu Kotlinx Serialization
            val jsonString = Json.encodeToString(dataModel) // serializacja
            it.write(jsonString)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
