package com.example.currencyflow.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class ViewModel : ViewModel() {
    private val valuePairs = mutableStateListOf<Pair<String, String>>()

    fun addPair(pair: Pair<String, String>) {
        valuePairs.add(pair)
    }

    fun removePair(indexToRemove: Int) {
        if (indexToRemove in valuePairs.indices) {
            valuePairs.removeAt(indexToRemove)
        }
    }
}
