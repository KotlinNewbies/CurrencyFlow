package com.fluida.currencyflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluida.currencyflow.data.SyncManager
import com.fluida.currencyflow.data.repository.RepositoryData
import com.fluida.currencyflow.data.model.Waluta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteCurrenciesViewModel @Inject constructor(
    private val repository: RepositoryData,
    private val syncManager: SyncManager
) : ViewModel() {

    // Mapa przechowująca TYMCZASOWY stan wyboru na tym ekranie (np. Waluta do Boolean)
    private val _aktualnyWyborWalut = MutableStateFlow<Map<Waluta, Boolean>>(emptyMap())
    val aktualnyWyborWalut: StateFlow<Map<Waluta, Boolean>> = _aktualnyWyborWalut.asStateFlow()

    // Pełna lista wszystkich możliwych walut (załóżmy, że ją masz, np. z enuma)
    private val _wszystkieWaluty = MutableStateFlow<List<Waluta>>(emptyList())
    val wszystkieWaluty: StateFlow<List<Waluta>> = _wszystkieWaluty.asStateFlow()


    init {
        loadData()
        observeBackupEvents()
    }

    private fun observeBackupEvents() {
        viewModelScope.launch {
            syncManager.backupAppliedEvent.collect {
                loadData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            // Załaduj pełną listę walut
            _wszystkieWaluty.value = Waluta.entries.toList()

            // Załaduj aktualnie zapisane ulubione waluty z repozytorium
            val zapisaneUlubione = repository.loadFavoriteCurrencies()

            // Zainicjuj tymczasowy wybór na podstawie zapisanych ulubionych
            _aktualnyWyborWalut.value = _wszystkieWaluty.value.associateWith { zapisaneUlubione.contains(it) }
        }
    }

    // Metoda wywoływana, gdy użytkownik zmienia zaznaczenie checkboxa
    fun toggleWalutaWybrana(waluta: Waluta, czyWybrana: Boolean) {
        _aktualnyWyborWalut.value = _aktualnyWyborWalut.value.toMutableMap().apply {
            this[waluta] = czyWybrana // Zawsze ustawiamy wartość, true/false
        }
    }

    // Metoda wywoływana, gdy użytkownik zakończy wybór (np. kliknie przycisk "Zapisz")
    fun zapiszWybraneUlubioneWaluty() {
        viewModelScope.launch {
            // Filtrujemy tylko te, które mają wartość true
            val walutyDoZapisania = _aktualnyWyborWalut.value.filterValues { it }.keys.toList()
            repository.saveFavoriteCurrencies(walutyDoZapisania)
            syncManager.uploadBackup()
        }
    }

    // Metoda zwracająca listę aktualnie zaznaczonych walut (do walidacji np. min 2)
    fun getCurrentlySelectedCurrencies(): List<Waluta> {
        return _aktualnyWyborWalut.value.filterValues { it }.keys.toList()
    }
}