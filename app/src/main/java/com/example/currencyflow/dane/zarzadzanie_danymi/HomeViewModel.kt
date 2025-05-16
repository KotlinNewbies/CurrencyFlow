package com.example.currencyflow.dane.zarzadzanie_danymi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyflow.dane.C
import com.example.currencyflow.dane.ModelDanychKontenerow
import com.example.currencyflow.klasy.Waluta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG_HOME = "HomeViewModel" // Nowy TAG

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RepositoryData // Hilt wstrzyknie zaktualizowaną instancję RepositoryData
) : ViewModel() {

    // StateFlow dla listy kontenerów wyświetlanych na ekranie głównym
    private val _konteneryUI = MutableStateFlow<List<C>>(emptyList())
    val konteneryUI: StateFlow<List<C>> = _konteneryUI.asStateFlow()

    // StateFlow dla listy walut DOSTĘPNYCH do wyboru w kontenerach (ulubione waluty)
    private val _dostepneWalutyDlaKontenerow = MutableStateFlow<List<Waluta>>(emptyList())
    val dostepneWalutyDlaKontenerow: StateFlow<List<Waluta>> =
        _dostepneWalutyDlaKontenerow.asStateFlow()

    init {
        Log.d(TAG_HOME, "HomeViewModel init block wywołany")
        // Ładujemy dostępne waluty i kontenery przy starcie ViewModelu
        viewModelScope.launch {
            ladujDostepneWalutyIRestaurujKontenery()
        }
    }

    private suspend fun ladujDostepneWalutyIRestaurujKontenery() {
        Log.d(TAG_HOME, "ladujDostepneWalutyIRestaurujKontenery wywołany")

        // Najpierw wczytaj ulubione waluty, które będą dostępne w kontenerach
        val zapisaneWaluty = repository.loadFavoriteCurrencies()
        Log.d(TAG_HOME, "Załadowano ulubione waluty: $zapisaneWaluty")
        _dostepneWalutyDlaKontenerow.value = zapisaneWaluty

        // Następnie wczytaj zapisane dane kontenerów Z REPOZYTORIUM
        val zapisaneDaneKontenerow = repository.loadContainerData()
        Log.d(TAG_HOME, "Załadowano dane kontenerów: $zapisaneDaneKontenerow")


        // Jeśli istnieją zapisane kontenery, użyj ich
        if (zapisaneDaneKontenerow != null && zapisaneDaneKontenerow.kontenery.isNotEmpty()) {
            _konteneryUI.value = zapisaneDaneKontenerow.kontenery
            Log.d(
                TAG_HOME,
                "Restaurowanie kontenerów z zapisanych danych. Liczba: ${_konteneryUI.value.size}"
            )
        } else {
            // Jeśli brak zapisanych kontenerów, utwórz domyślny
            val domyslnaWaluta =
                zapisaneWaluty.firstOrNull() ?: Waluta.EUR // Użyj pierwszej dostępnej lub EUR
            val domyslnaLista = listOf(C(domyslnaWaluta, domyslnaWaluta, "", ""))
            _konteneryUI.value = domyslnaLista
            Log.d(TAG_HOME, "Brak zapisanych kontenerów, tworzenie domyślnego: $domyslnaLista")

            // Zapisz ten domyślny kontener od razu PRZEZ REPOZYTORIUM
            repository.saveContainerData(ModelDanychKontenerow(_konteneryUI.value.size, konteneryUI.value)) // To wywołanie jest teraz poprawne
            Log.d(TAG_HOME, "Zapisano domyślny kontener PRZEZ REPOZYTORIUM.")
        }
    }

    // Metody zarządzające listą kontenerów
    fun dodajKontener() {
        Log.d(TAG_HOME, "dodajKontener wywołany")
        // Logika dodawania nowego kontenera z domyślnymi wartościami
        val domyslnaWaluta = _dostepneWalutyDlaKontenerow.value.firstOrNull()
            ?: Waluta.EUR // Użyj pierwszej dostępnej lub EUR
        val nowyKontener = C(domyslnaWaluta, domyslnaWaluta, "", "")
        _konteneryUI.value += nowyKontener
        Log.d(TAG_HOME, "Dodano nowy kontener. Nowa lista: ${_konteneryUI.value.size}")
        viewModelScope.launch {
            repository.saveContainerData(ModelDanychKontenerow(_konteneryUI.value.size, konteneryUI.value)) // Zapisz po dodaniu PRZEZ REPOZYTORIUM
            Log.d(TAG_HOME, "Zapisano dane kontenerów po dodaniu PRZEZ REPOZYTORIUM.")
        }
    }

    fun usunKontener(index: Int) {
        Log.d(TAG_HOME, "usunKontener wywołany dla index: $index")
        if (index >= 0 && index < _konteneryUI.value.size) {
            _konteneryUI.value = _konteneryUI.value.toMutableList().apply { removeAt(index) }
            Log.d(TAG_HOME, "Usunięto kontener. Nowa lista: ${_konteneryUI.value.size}")
            viewModelScope.launch {
                repository.saveContainerData(ModelDanychKontenerow(_konteneryUI.value.size, konteneryUI.value)) // Zapisz po usunięciu PRZEZ REPOZYTORIUM
                Log.d(TAG_HOME, "Zapisano dane kontenerów po usunięciu PRZEZ REPOZYTORIUM.")
            }
        }
    }

    fun zaktualizujKontener(index: Int, updatedKontener: C) {
        Log.d(TAG_HOME, "zaktualizujKontener wywołany dla index: $index z danymi: $updatedKontener")
        if (index >= 0 && index < _konteneryUI.value.size) {
            _konteneryUI.value =
                _konteneryUI.value.toMutableList().apply { this[index] = updatedKontener }
            Log.d(TAG_HOME, "Zaktualizowano kontener. Nowa lista: ${_konteneryUI.value.size}")
            viewModelScope.launch {
                repository.saveContainerData(ModelDanychKontenerow(_konteneryUI.value.size, konteneryUI.value)) // Zapisz po aktualizacji PRZEZ REPOZYTORIUM
                Log.d(TAG_HOME, "Zapisano dane kontenerów po aktualizacji PRZEZ REPOZYTORIUM.")
            }
        }
    }

    // Ta metoda powinna być wywołana na ekranie głównym po powrocie z ekranu ulubionych walut,
    // aby odświeżyć listę dostępnych walut w kontenerach.
    fun odswiezDostepneWaluty() {
        Log.d(TAG_HOME, "odswiezDostepneWaluty wywołany")
        viewModelScope.launch {
            val zapisaneWaluty = repository.loadFavoriteCurrencies()
            _dostepneWalutyDlaKontenerow.value = zapisaneWaluty
            Log.d(TAG_HOME, "Odświeżono dostępne waluty: $zapisaneWaluty")
        }
    }
    fun zapiszAktualneKontenery() {
        Log.d(TAG_HOME, "zapiszAktualneKontenery wywołany")
        viewModelScope.launch {
            // Użyj aktualnego stanu _konteneryUI.value do zapisania
            repository.saveContainerData(ModelDanychKontenerow(_konteneryUI.value.size, _konteneryUI.value))
            Log.d(TAG_HOME, "Zapisano aktualne dane kontenerów PRZEZ REPOZYTORIUM.")
        }
    }

    // Dodaj funkcję do obsługi zadania sieciowego, która ZAKTUALIZUJE stan _konteneryUI
    // po pobraniu danych.
    /*
     fun przeliczWalutySieciowo(/* parametry, np. lista kontenerów do przeliczenia */) {
          viewModelScope.launch {
              // ... Twoja logika zadania sieciowego, która wykorzystuje repository
              // do potencjalnego wczytania/zapisania danych kursów walut ...
              // Po uzyskaniu wyników:
              // _konteneryUI.value = zaktualizowana_lista_kontenerow
          }
     }
     */
}