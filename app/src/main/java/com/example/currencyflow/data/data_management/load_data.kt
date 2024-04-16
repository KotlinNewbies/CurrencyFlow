package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.data.DataModel
import com.google.gson.Gson
import java.io.File

fun loadData(context: Context): DataModel? {
    val fileName = "user_data.json"
    val file = File(context.filesDir, fileName)
    if (file.exists()) {
        val jsonString = file.readText()
        val gson = Gson()
        return gson.fromJson(jsonString, DataModel::class.java)
    }
    return null
}
