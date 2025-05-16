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
        Log.d(TAG_HOME, "HomeViewModel init block")
        ladujDanePoczatkowe() // Zmieniamy nazwę dla jasności
    }

    private fun ladujDanePoczatkowe() {
        viewModelScope.launch {
            // Krok 1: Sprawdź i zainicjuj domyślne ulubione waluty, JEŚLI TRZEBA
            val obecneUlubione = repository.loadFavoriteCurrencies()
            if (obecneUlubione.isEmpty()) {
                Log.d(TAG_HOME, "Brak zapisanych ulubionych walut. Inicjalizuję domyślne EUR i USD.")
                val domyslneUlubione = listOf(Waluta.EUR, Waluta.USD)
                repository.saveFavoriteCurrencies(domyslneUlubione) // Zapisz domyślne
                _dostepneWalutyDlaKontenerow.value = domyslneUlubione // Ustaw jako dostępne
            } else {
                _dostepneWalutyDlaKontenerow.value = obecneUlubione
            }
            Log.d(TAG_HOME, "Dostępne waluty dla kontenerów po inicjalizacji/ładowaniu: ${_dostepneWalutyDlaKontenerow.value}")

            // Krok 2: Załaduj lub zainicjuj kontenery
            val zapisaneDaneKontenerow = repository.loadContainerData()
            if (zapisaneDaneKontenerow != null && zapisaneDaneKontenerow.kontenery.isNotEmpty()) {
                Log.d(TAG_HOME, "Przywracam zapisane kontenery: ${zapisaneDaneKontenerow.kontenery.size} szt.")
                _konteneryUI.value = zapisaneDaneKontenerow.kontenery.toMutableList()
            } else {
                Log.d(TAG_HOME, "Brak zapisanych kontenerów. Tworzę domyślny kontener.")
                // Użyj pierwszej z dostępnych walut (które już załadowaliśmy/zainicjalizowaliśmy)
                val pierwszaDostepna = _dostepneWalutyDlaKontenerow.value.firstOrNull() ?: Waluta.EUR // Fallback na EUR
                val drugaDostepna = _dostepneWalutyDlaKontenerow.value.getOrNull(1) ?: Waluta.USD // Fallback na USD

                // Jeśli _dostepneWalutyDlaKontenerow ma tylko jedną walutę, użyj jej dwa razy
                val walutaDo = if (_dostepneWalutyDlaKontenerow.value.size > 1) drugaDostepna else pierwszaDostepna

                val domyslnyKontener = C(from = pierwszaDostepna, to = walutaDo, amount = "", result = "")
                _konteneryUI.value = mutableListOf(domyslnyKontener)
                // Zapisz ten nowo utworzony domyślny kontener
                repository.saveContainerData(ModelDanychKontenerow(_konteneryUI.value.size, _konteneryUI.value))
                Log.d(TAG_HOME, "Utworzono i zapisano domyślny kontener: $domyslnyKontener")
            }
            Log.d(TAG_HOME, "Stan _konteneryUI po inicjalizacji/ładowaniu: ${_konteneryUI.value}")
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
            var noweUlubione = repository.loadFavoriteCurrencies()
            Log.d(TAG_HOME, "Załadowano nowe ulubione z repo: ${noweUlubione.map { it.symbol }}")
            Log.d("HomeViewModel", "odswiezDostepneWaluty: noweUlubione = ${noweUlubione.map { it.symbol }}")

            // Krok 1: Upewnij się, że lista ulubionych nie jest pusta.
            // Jeśli jest, użyj domyślnych i zapisz je, aby uniknąć problemów.
            if (noweUlubione.isEmpty()) {
                Log.d(
                    TAG_HOME,
                    "Lista ulubionych po załadowaniu jest pusta. Ustawiam domyślne EUR i USD."
                )
                noweUlubione = listOf(Waluta.EUR, Waluta.USD)
                repository.saveFavoriteCurrencies(noweUlubione) // Zapisz domyślne, aby następne load je widziało
            }
            _dostepneWalutyDlaKontenerow.value = noweUlubione
            Log.d(
                TAG_HOME,
                "Zaktualizowano _dostepneWalutyDlaKontenerow: ${noweUlubione.map { it.symbol }}"
            )

            // Krok 2: Zaktualizuj istniejące kontenery w _konteneryUI
            val aktualneKontenery = _konteneryUI.value
            // Oblicz zaktualizowane kontenery
            val zaktualizowaneKonteneryWynik = // Zmieniona nazwa, aby uniknąć konfliktu w if
                aktualneKontenery.mapIndexed { index, staryKontener ->
                    var nowaWalutaFrom = staryKontener.from
                    var nowaWalutaTo = staryKontener.to
                    var czyKontenerZmodyfikowany = false

                    // Sprawdź 'from'
                    if (!noweUlubione.contains(staryKontener.from)) {
                        nowaWalutaFrom = noweUlubione.first() // Wybierz pierwszą dostępną
                        Log.d(
                            TAG_HOME,
                            "Kontener (index $index): waluta 'from' (${staryKontener.from.symbol}) nie jest już dostępna. Zmieniam na ${nowaWalutaFrom.symbol}"
                        )
                        czyKontenerZmodyfikowany = true
                    }

                    // Sprawdź 'to'
                    if (!noweUlubione.contains(staryKontener.to)) {
                        nowaWalutaTo = noweUlubione.firstOrNull { it != nowaWalutaFrom }
                            ?: noweUlubione.first()
                        Log.d(
                            TAG_HOME,
                            "Kontener (index $index): waluta 'to' (${staryKontener.to.symbol}) nie jest już dostępna. Zmieniam na ${nowaWalutaTo.symbol}"
                        )
                        czyKontenerZmodyfikowany = true
                    }

                    if (noweUlubione.size > 1 && nowaWalutaFrom == nowaWalutaTo) {
                        val potencjalnaInnaWalutaTo =
                            noweUlubione.firstOrNull { it != nowaWalutaFrom }
                        if (potencjalnaInnaWalutaTo != null) {
                            nowaWalutaTo = potencjalnaInnaWalutaTo
                            Log.d(
                                TAG_HOME,
                                "Kontener (index $index): 'from' i 'to' były takie same (${nowaWalutaFrom.symbol}). Zmieniam 'to' na ${nowaWalutaTo.symbol}"
                            )
                            czyKontenerZmodyfikowany = true
                        }
                    }

                    if (czyKontenerZmodyfikowany) {
                        staryKontener.copy(from = nowaWalutaFrom, to = nowaWalutaTo, result = "")
                    } else {
                        staryKontener
                    }
                }

            // Tylko jeśli faktycznie coś się zmieniło w kontenerach, zaktualizuj StateFlow i zapisz
            if (zaktualizowaneKonteneryWynik != aktualneKontenery) {
                _konteneryUI.value = zaktualizowaneKonteneryWynik // <-- UŻYCIE POPRAWIONEJ ZMIENNEJ
                Log.d(
                    TAG_HOME,
                    "Zaktualizowano _konteneryUI po odświeżeniu dostępnych walut: ${zaktualizowaneKonteneryWynik.map { "(${it.from.symbol}-${it.to.symbol})" }}"
                )
                repository.saveContainerData(
                    ModelDanychKontenerow(
                        _konteneryUI.value.size,
                        _konteneryUI.value
                    )
                )
                Log.d(TAG_HOME, "Zapisano zaktualizowane kontenery do repozytorium.")
            } else {
                Log.d(TAG_HOME, "Kontenery UI nie wymagały aktualizacji po zmianie dostępnych walut.")
            }
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