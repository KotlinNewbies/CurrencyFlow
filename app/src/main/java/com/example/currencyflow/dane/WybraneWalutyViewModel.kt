package com.example.currencyflow.dane

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyflow.dane.zarzadzanie_danymi.RepositoryData
import com.example.currencyflow.klasy.Waluta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
private const val TAG = "WybraneWalutyViewModel" // Dodaj TAG
class WybraneWalutyViewModel(
    private val repository: RepositoryData
) : ViewModel() {

    private val _wybraneWaluty = MutableStateFlow<Map<Waluta, Boolean>>(emptyMap())
    val wybraneWaluty: StateFlow<Map<Waluta, Boolean>> = _wybraneWaluty

    fun inicjalizacjaWalut(
        wszystkieWaluty: List<Waluta>,
        poczatkowoWybraneWaluty: List<Waluta>
    ) {
        Log.d(TAG, "inicjalizacjaWalut wywołana") // Log przy wywołaniu
        viewModelScope.launch {
            val zapisaneWaluty = repository.loadFavoriteCurrencies()
            Log.d(TAG, "Zapisane waluty załadowane: $zapisaneWaluty") // Log załadowanych walut

            val wybrane = when {
                zapisaneWaluty.isNotEmpty() -> zapisaneWaluty
                poczatkowoWybraneWaluty.isNotEmpty() -> poczatkowoWybraneWaluty
                else -> emptyList()
            }
            Log.d(TAG, "Finalne wybrane waluty do inicjalizacji: $wybrane") // Log finalnej listy

            val domyslnyWybor = wszystkieWaluty.associateWith { wybrane.contains(it) }
            _wybraneWaluty.value = domyslnyWybor
            Log.d(TAG, "Stan _wybraneWaluty zaktualizowany: ${_wybraneWaluty.value}") // Log finalnego stanu
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
