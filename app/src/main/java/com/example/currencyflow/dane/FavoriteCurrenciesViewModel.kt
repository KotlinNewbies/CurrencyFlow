package com.example.currencyflow.dane

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyflow.dane.zarzadzanie_danymi.RepositoryData
import com.example.currencyflow.klasy.Waluta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG_FAVORITES = "FavoriteCurrenciesVM" // Nowy TAG

@HiltViewModel
class FavoriteCurrenciesViewModel @Inject constructor(
    private val repository: RepositoryData
    // Potrzebujesz dostępu do listy WSZYSTKICH możliwych walut.
    // Może to być wstrzyknięte repozytorium danych walut (np. CurrencyDataRepository)
    // lub po prostu Waluta.entries.toList()
) : ViewModel() {

    // Mapa przechowująca TYMCZASOWY stan wyboru na tym ekranie (np. Waluta do Boolean)
    private val _aktualnyWyborWalut = MutableStateFlow<Map<Waluta, Boolean>>(emptyMap())
    val aktualnyWyborWalut: StateFlow<Map<Waluta, Boolean>> = _aktualnyWyborWalut.asStateFlow()

    // Pełna lista wszystkich możliwych walut (załóżmy, że ją masz, np. z enuma)
    private val _wszystkieWaluty = MutableStateFlow<List<Waluta>>(emptyList())
    val wszystkieWaluty: StateFlow<List<Waluta>> = _wszystkieWaluty.asStateFlow()


    init {
        Log.d(TAG_FAVORITES, "FavoriteCurrenciesViewModel init block wywołany")
        viewModelScope.launch {
            // Załaduj pełną listę walut (jeśli nie jest statyczna)
            _wszystkieWaluty.value = Waluta.entries.toList() // Przykład dla enuma

            // Załaduj aktualnie zapisane ulubione waluty
            val zapisaneUlubione = repository.loadFavoriteCurrencies()
            Log.d(TAG_FAVORITES, "Załadowano początkowe ulubione: $zapisaneUlubione")

            // Zainicjuj tymczasowy wybór na podstawie zapisanych ulubionych
            // Użyjemy pełnej listy walut i zaznaczymy te, które są w zapisanych ulubionych
            _aktualnyWyborWalut.value = _wszystkieWaluty.value.associateWith { zapisaneUlubione.contains(it) }
            Log.d(TAG_FAVORITES, "Stan _aktualnyWyborWalut zainicjowany: ${_aktualnyWyborWalut.value.size} elementów")
        }
    }

    // Metoda wywoływana, gdy użytkownik zmienia zaznaczenie checkboxa
    fun toggleWalutaWybrana(waluta: Waluta, czyWybrana: Boolean) {
        Log.d(TAG_FAVORITES, "toggleWalutaWybrana: $waluta, wybrana: $czyWybrana")
        _aktualnyWyborWalut.value = _aktualnyWyborWalut.value.toMutableMap().apply {
            this[waluta] = czyWybrana // Zawsze ustawiamy wartość, true/false
        }
        Log.d(TAG_FAVORITES, "Nowy stan _aktualnyWyborWalut: ${_aktualnyWyborWalut.value.filterValues { it }.keys.size} zaznaczonych")
    }

    // Metoda wywoływana, gdy użytkownik zakończy wybór (np. kliknie przycisk "Zapisz")
    fun zapiszWybraneUlubioneWaluty() {
        Log.d(TAG_FAVORITES, "zapiszWybraneUlubioneWaluty wywołany")
        viewModelScope.launch {
            // Filtrujemy tylko te, które mają wartość true
            val walutyDoZapisania = _aktualnyWyborWalut.value.filterValues { it }.keys.toList()
            repository.saveFavoriteCurrencies(walutyDoZapisania)
            Log.d(TAG_FAVORITES, "Zapisano ulubione waluty: $walutyDoZapisania")
        }
    }

    // Metoda zwracająca listę aktualnie zaznaczonych walut (do walidacji np. min 2)
    fun getCurrentlySelectedCurrencies(): List<Waluta> {
        return _aktualnyWyborWalut.value.filterValues { it }.keys.toList()
    }
}
