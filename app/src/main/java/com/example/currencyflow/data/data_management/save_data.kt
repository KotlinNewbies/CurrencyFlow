package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.UUIDManager
import com.example.currencyflow.data.DataModel
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.lang.Exception

fun saveData(context: Context) {
    val uuidString = UUIDManager.getUUID()
    val dataModel = DataModel(uuidString,"curConv", "1.0.0")

    val fileName = "user_data.json"
    val file = File(context.filesDir, fileName)
    try {
        PrintWriter(FileWriter(file)).use {
            val gson = Gson()
            val jsonString = gson.toJson(dataModel)
            it.write(jsonString)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}