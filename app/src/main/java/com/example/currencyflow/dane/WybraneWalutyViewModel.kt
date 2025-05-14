package com.example.currencyflow.dane

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyflow.dane.zarzadzanie_danymi.RepositoryData
import com.example.currencyflow.klasy.Waluta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "WybraneWalutyViewModel"

class WybraneWalutyViewModel(
    private val repository: RepositoryData
) : ViewModel() {

    private val _wybraneWaluty = MutableStateFlow<Map<Waluta, Boolean>>(emptyMap())
    val wybraneWaluty: StateFlow<Map<Waluta, Boolean>> = _wybraneWaluty

    // Inicjalizacja w bloku init - ViewModel sam ładuje ulubione waluty
    init {
        Log.d(TAG, "WybraneWalutyViewModel init block wywołany")
        viewModelScope.launch {
            // Pobieramy wszystkie możliwe waluty (możesz przekazać w konstruktorze jeśli lista jest dynamiczna)
            val wszystkieWaluty = Waluta.entries.toList()
            val zapisaneWaluty = repository.loadFavoriteCurrencies()
            Log.d(TAG, "Zapisane waluty załadowane w ViewModelu: $zapisaneWaluty")

            // Ustawiamy początkowy stan _wybraneWaluty
            val domyslnyWybor = wszystkieWaluty.associateWith { zapisaneWaluty.contains(it) }
            _wybraneWaluty.value = domyslnyWybor
            Log.d(TAG, "Stan _wybraneWaluty zaktualizowany w ViewModelu: ${_wybraneWaluty.value}")
        }
    }

    fun zaktualizujWybraneWaluty(waluta: Waluta, jestWybrana: Boolean) {
        _wybraneWaluty.value = _wybraneWaluty.value.toMutableMap().apply {
            this[waluta] = jestWybrana
        }
        // Zapisz zmiany w ulubionych walutach od razu po zmianie
        zapiszWybraneWaluty()
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
