package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.classes.Currency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

fun loadSelectedCurrencies(context: Context): List<Currency> {
    val fileName = "selected_currencies.json"
    val file = File(context.filesDir, fileName)
    if (file.exists()) {
        val jsonString = file.readText()
        val gson = Gson()
        val type = object : TypeToken<List<Currency>>() {}.type
        return gson.fromJson(jsonString, type)
    }
    return emptyList()
}
