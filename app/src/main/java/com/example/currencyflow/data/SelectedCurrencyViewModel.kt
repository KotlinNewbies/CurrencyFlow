package com.example.currencyflow.data

import androidx.lifecycle.ViewModel
import com.example.currencyflow.classes.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SelectedCurrenciesViewModel : ViewModel() {
    private val _selectedCurrencies = MutableStateFlow<Map<Currency, Boolean>>(emptyMap())
    val selectedCurrencies: StateFlow<Map<Currency, Boolean>> = _selectedCurrencies

    fun initializeCurrencies(allCurrencies: List<Currency>, initialSelectedCurrencies: List<Currency>) {
        if (_selectedCurrencies.value.isEmpty()) {
            val defaultSelection = allCurrencies.associateWith { initialSelectedCurrencies.contains(it) }
            _selectedCurrencies.value = defaultSelection
        }
    }

    fun updateCurrencySelection(currency: Currency, isSelected: Boolean) {
        _selectedCurrencies.value = _selectedCurrencies.value.toMutableMap().apply {
            this[currency] = isSelected
        }
    }

    fun getSelectedCurrencies(): List<Currency> {
        return _selectedCurrencies.value.filterValues { it }.keys.toList()
    }
}

