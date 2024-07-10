package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.classes.Currency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.lang.Exception

fun saveSelectedCurrencies(context: Context, selectedCurrencies: List<Currency>) {
    val fileName = "selected_currencies.json"
    val file = File(context.filesDir, fileName)
    try {
        PrintWriter(FileWriter(file)).use {
            val gson = Gson()
            val jsonString = gson.toJson(selectedCurrencies)
            it.write(jsonString)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
