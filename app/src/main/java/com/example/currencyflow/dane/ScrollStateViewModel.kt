package com.example.currencyflow.dane

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel

class ScrollStateViewModel: ViewModel() {
    val scrollState = mutableIntStateOf(0)
}