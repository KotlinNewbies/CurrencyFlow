package com.example.currencyflow


import android.content.Context
import com.example.currencyflow.data.data_management.savePairCount

fun addPair(context: Context, valuePairs: MutableList<Pair<String, String>>, newValue: Pair<String, String> = "" to "") {
    valuePairs.add(newValue)
}

fun removePair(
    context: Context,
    valuePairs: MutableList<Pair<String, String>>,
    indexToRemove: Int
) {
    if (indexToRemove in 0 until valuePairs.size) {
        valuePairs.removeAt(indexToRemove)
        savePairCount(context, valuePairs.size) // Zapisz zaktualizowaną liczbę par
    }
}


