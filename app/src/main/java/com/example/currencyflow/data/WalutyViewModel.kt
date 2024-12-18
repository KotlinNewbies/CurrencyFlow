package com.example.currencyflow.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class WalutyViewModel : ViewModel() {

    // StateFlow do przechowywania mapy walut
    private val _mapaWalut = MutableStateFlow<Map<String, BigDecimal>>(emptyMap())
    val mapaWalut: StateFlow<Map<String, BigDecimal>> get() = _mapaWalut

    // Funkcja do aktualizacji mapy walut
    fun zaktualizujMapeWalut(noweMnozniki: Map<String, BigDecimal>) {
        viewModelScope.launch {
            _mapaWalut.value = noweMnozniki
        }
    }
}