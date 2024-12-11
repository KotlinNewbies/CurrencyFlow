package com.example.currencyflow.data

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel

class ScrollStateViewModel: ViewModel() {
    val scrollState = mutableIntStateOf(0)
}