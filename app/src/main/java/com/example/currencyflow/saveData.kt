package com.example.currencyflow

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.lang.Exception

fun saveData(context: Context) {
    val uuidString = UUIDManager.getUUID()
    val dataModel = DataModel(uuidString,"curConv", "1.0")

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