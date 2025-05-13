package com.example.currencyflow.dane

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyflow.dane.zarzadzanie_danymi.RepositoryData
import com.example.currencyflow.klasy.Waluta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WybraneWalutyViewModel(
    private val repository: RepositoryData
) : ViewModel() {

    private val _wybraneWaluty = MutableStateFlow<Map<Waluta, Boolean>>(emptyMap())
    val wybraneWaluty: StateFlow<Map<Waluta, Boolean>> = _wybraneWaluty

    fun inicjalizacjaWalut(
        wszystkieWaluty: List<Waluta>,
        poczatkowoWybraneWaluty: List<Waluta>
    ) {
        viewModelScope.launch {
            val zapisaneWaluty = repository.loadFavoriteCurrencies()

            val wybrane = when {
                zapisaneWaluty.isNotEmpty() -> zapisaneWaluty
                poczatkowoWybraneWaluty.isNotEmpty() -> poczatkowoWybraneWaluty
                else -> emptyList()
            }

            val domyslnyWybor = wszystkieWaluty.associateWith { wybrane.contains(it) }
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

    fun zapiszWybraneWaluty() {
        viewModelScope.launch {
            val wybrane = zdobadzWybraneWaluty()
            repository.saveFavoriteCurrencies(wybrane)
        }
    }
}
