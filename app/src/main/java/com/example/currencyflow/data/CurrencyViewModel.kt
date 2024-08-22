package com.example.currencyflow.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CurrencyViewModel : ViewModel() {

    // StateFlow do przechowywania listy walut
    private val _currencyRates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val currencyRates: StateFlow<Map<String, Double>> get() = _currencyRates

    // Funkcja do aktualizacji kursów walut
    fun updateCurrencyRates(newRates: Map<String, Double>) {
        viewModelScope.launch {
            _currencyRates.value = newRates
        }
    }
}