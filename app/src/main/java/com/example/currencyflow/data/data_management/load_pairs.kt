package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.data.PairDataModel
import com.google.gson.Gson
import java.io.File

fun loadContainerData(context: Context): PairDataModel? {
    val file = File(context.filesDir, "pair_count.json")
    return if (file.exists()) {
        val jsonString = file.readText()
        val gson = Gson()
        gson.fromJson(jsonString, PairDataModel::class.java)
    } else {
        null
    }
}