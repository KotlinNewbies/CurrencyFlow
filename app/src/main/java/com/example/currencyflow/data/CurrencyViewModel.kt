package com.example.currencyflow.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CurrencyViewModel : ViewModel() {

    // StateFlow do przechowywania listy walut
    private val _currencyRates = MutableStateFlow<Map<String, BigDecimal>>(emptyMap())
    val currencyRates: StateFlow<Map<String, BigDecimal>> get() = _currencyRates

    // Funkcja do aktualizacji kursów walut
    fun updateCurrencyRates(newRates: Map<String, BigDecimal>) {
        viewModelScope.launch {
            _currencyRates.value = newRates
        }
    }
}