package com.example.currencyflow.dane

import androidx.lifecycle.ViewModel
import com.example.currencyflow.klasy.Waluta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WybraneWalutyViewModel : ViewModel() {
    private val _wybraneWaluty = MutableStateFlow<Map<Waluta, Boolean>>(emptyMap())
    val wybraneWaluty: StateFlow<Map<Waluta, Boolean>> = _wybraneWaluty

    fun inicjaliacjaWalut(wszystkieWaluty: List<Waluta>, poczatkowoWybraneWaluty: List<Waluta>) {
        if (_wybraneWaluty.value.isEmpty()) {
            val domyslnyWybor = wszystkieWaluty.associateWith { poczatkowoWybraneWaluty.contains(it) }
            _wybraneWaluty.value = domyslnyWybor
        }
    }

    fun zaktualizujWybraneWaluty(waluta: Waluta, jestWybrana: Boolean) {
        _wybraneWaluty.value = _wybraneWaluty.value.toMutableMap().apply {
            this[waluta] = jestWybrana
        }
    }

    fun zdobadzWybraneWaluty(): List<Waluta> {
        return _wybraneWaluty.value.filterValues { it }.keys.toList()
    }
}

