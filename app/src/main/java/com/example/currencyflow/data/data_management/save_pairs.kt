package com.example.currencyflow.data.data_management

import android.content.Context
import com.example.currencyflow.data.C
import com.example.currencyflow.data.PairDataModel
import com.google.gson.Gson
import java.io.File

fun saveContainerData(context: Context, pairCount: Int, containers: List<C>) {
    val pairCountModel = PairDataModel(pairCount, containers)
    val gson = Gson()
    val jsonString = gson.toJson(pairCountModel)
    val file = File(context.filesDir, "pair_count.json")
    file.writeText(jsonString)
}
