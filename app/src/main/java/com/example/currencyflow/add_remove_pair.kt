package com.example.currencyflow


import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableIntState
import com.example.currencyflow.classes.Currency
import com.example.currencyflow.data.C
import com.example.currencyflow.data.data_management.savePairCount
import java.util.UUID

fun addContainer(containers: MutableList<C>, newContainer: C = C(Currency.USD, Currency.GBP, "", "")) {
    containers.add(newContainer)
}

fun removePair(
    context: Context,
    valuePairs: MutableList<Pair<String, String>>,
    indexToRemove: Int
) {
    if (indexToRemove in 0 until valuePairs.size) {
        valuePairs.removeAt(indexToRemove)
        savePairCount(context, valuePairs.size) // Zapisanie zaktualizowanej liczby par
    }
}
/*
fun removePairAtIndex(activity: ComponentActivity,
                      valuePairs: MutableList<Pair<String, String>>,
                      index: Int
) {
    Log.d("Usuwanie pary", "Usuwanie pary o indeksie: $index")
    if (index >= 0 && index < valuePairs.size) {
        valuePairs.removeAt(index)
        savePairCount(activity, valuePairs.size)
    }
}

 */