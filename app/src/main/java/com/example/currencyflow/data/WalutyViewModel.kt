package com.example.currencyflow.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class WalutyViewModel : ViewModel() {

    // StateFlow do przechowywania listy walut
    private val _mnoznikiWalut = MutableStateFlow<Map<String, BigDecimal>>(emptyMap())
    val mnoznikiWalut: StateFlow<Map<String, BigDecimal>> get() = _mnoznikiWalut

    // Funkcja do aktualizacji kursów walut
    fun zaktualizujMnoznikiWalut(noweMnozniki: Map<String, BigDecimal>) {
        viewModelScope.launch {
            _mnoznikiWalut.value = noweMnozniki
        }
    }
}