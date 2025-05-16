package com.example.currencyflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyflow.data.repository.RepositoryData
import com.example.currencyflow.data.repository.WalutyRepository
import com.example.currencyflow.data.model.C
import com.example.currencyflow.data.model.ModelDanychKontenerow
import com.example.currencyflow.data.repository.UserDataRepository
import com.example.currencyflow.data.model.Waluta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.format


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RepositoryData,
    private val walutyRepository: WalutyRepository, // NOWO DODANE REPOZYTORIUM
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    private val _konteneryUI = MutableStateFlow<List<C>>(emptyList())
    val konteneryUI: StateFlow<List<C>> = _konteneryUI.asStateFlow()
    private val _czyLadowanieKursow = MutableStateFlow(false)
    val czyLadowanieKursow: StateFlow<Boolean> = _czyLadowanieKursow.asStateFlow()

    private val _bladPobieraniaKursow = MutableStateFlow<String?>(null)
    val bladPobieraniaKursow: StateFlow<String?> = _bladPobieraniaKursow.asStateFlow()

    private val _dostepneWalutyDlaKontenerow = MutableStateFlow<List<Waluta>>(emptyList())
    val dostepneWalutyDlaKontenerow: StateFlow<List<Waluta>> =
        _dostepneWalutyDlaKontenerow.asStateFlow()
    private val _mapaKursow = MutableStateFlow<Map<String, Double>>(emptyMap())
    private var aktualnyIdentyfikatorUzytkownika: String? = null // Przechowaj ID
    init {
        ladujDanePoczatkowe()
        viewModelScope.launch {
            // Pobierz ModelDanychUzytkownika (z pliku lub nowy)
            val modelUzytkownika = userDataRepository.getUserDataModel()

            aktualnyIdentyfikatorUzytkownika = modelUzytkownika.id // Zapisz ID
            // Teraz, gdy masz ID, możesz odświeżyć kursy
            odswiezKursyWalut() //
        }
    }

    fun odswiezKursyWalut() {
        // Sprawdź, czy identyfikator użytkownika jest dostępny
        aktualnyIdentyfikatorUzytkownika?.let { idUzytkownika ->
            viewModelScope.launch { // Użyj głównego dispatchera dla aktualizacji UI-related StateFlows
                _czyLadowanieKursow.value = true
                _bladPobieraniaKursow.value = null // Resetuj błąd przed nową próbą

                walutyRepository.pobierzAktualneKursy(idUzytkownika) // Użyj zapisanego ID
                    // .flowOn(dispatcherProvider.io) // Usunięte, zakładając że repozytorium zarządza swoim dispatcherem
                    .catch { e ->
                        _mapaKursow.value = emptyMap() // W razie błędu w flow, ustaw pustą mapę
                        _bladPobieraniaKursow.value = "Błąd pobierania kursów: ${e.localizedMessage ?: "Nieznany błąd sieciowy"}"
                        _czyLadowanieKursow.value = false
                    }
                    .collect { pobraneKursy ->
                        _mapaKursow.value = pobraneKursy
                        _czyLadowanieKursow.value = false // Ustaw ładowanie na false po otrzymaniu danych lub błędzie

                        // Sprawdzenie, czy kursy są puste i czy wcześniej nie było błędu z 'catch'
                        // Ten warunek może być bardziej precyzyjny, jeśli repozytorium zwraca konkretne sygnały.

                        // Po otrzymaniu nowych kursów (nawet jeśli są puste, bo chcemy zaktualizować np. "??" na wynikach),
                        // przelicz wszystkie kontenery.
                        przeliczWszystkieKontenery() // Ta funkcja powinna używać _mapaKursow.value
                    }
            }
        } ?: run {
            // Ten blok zostanie wykonany, jeśli aktualnyIdentyfikatorUzytkownika jest null
            _bladPobieraniaKursow.value = "Brak ID użytkownika. Nie można odświeżyć kursów."
            _czyLadowanieKursow.value = false // Upewnij się, że ładowanie jest wyłączone
        }
    }

    fun wyczyscBladPobieraniaKursow() {
        _bladPobieraniaKursow.value = null
    }

    private fun ladujDanePoczatkowe() {
        viewModelScope.launch {
            // Krok 1: Ładowanie/Inicjalizacja ulubionych walut
            val obecneUlubione = repository.loadFavoriteCurrencies()
            if (obecneUlubione.isEmpty()) {
                val domyslneUlubione = listOf(Waluta.EUR, Waluta.USD)
                repository.saveFavoriteCurrencies(domyslneUlubione)
                _dostepneWalutyDlaKontenerow.value = domyslneUlubione
            } else {
                _dostepneWalutyDlaKontenerow.value = obecneUlubione
            }

            // Krok 2: Ładowanie/Inicjalizacja kontenerów
            val zapisaneDaneKontenerow = repository.loadContainerData()
            if (zapisaneDaneKontenerow != null && zapisaneDaneKontenerow.kontenery.isNotEmpty()) {
                _konteneryUI.value = zapisaneDaneKontenerow.kontenery
            } else {
                val pierwszaDostepna = _dostepneWalutyDlaKontenerow.value.firstOrNull() ?: Waluta.EUR
                val drugaDostepna = _dostepneWalutyDlaKontenerow.value.getOrNull(1) ?: Waluta.USD
                val walutaDo = if (_dostepneWalutyDlaKontenerow.value.size > 1) drugaDostepna else pierwszaDostepna
                val domyslnyKontener = C(from = pierwszaDostepna, to = walutaDo, amount = "1", result = "") // Domyślnie amount = "1"
                _konteneryUI.value = listOf(domyslnyKontener) // Użyj listOf dla niezmienialnej listy na początku
                // Nie ma potrzeby zapisywać tutaj, przeliczenie i zapis nastąpią poniżej
            }

            // Krok 3: Przelicz wartości dla załadowanych/domyślnych kontenerów
            przeliczWszystkieKontenery()
        }
    }

    fun dodajKontener() {
        val domyslnaWaluta = _dostepneWalutyDlaKontenerow.value.firstOrNull() ?: Waluta.EUR
        // Domyślnie dajmy kwotę "1", żeby od razu coś przeliczyło
        val nowyKontener = C(domyslnaWaluta, domyslnaWaluta, "1", "")
        _konteneryUI.value += nowyKontener // Dodaj do listy
        przeliczWszystkieKontenery() // Przelicz i zapisz
    }

    fun usunKontener(index: Int) {
        if (index >= 0 && index < _konteneryUI.value.size) {
            _konteneryUI.value = _konteneryUI.value.toMutableList().apply { removeAt(index) }.toList()
            // Po usunięciu nie ma potrzeby przeliczania, wystarczy zapisać
            zapiszKonteneryDoRepozytorium()
        }
    }

    /**
     * Funkcja wywoływana z UI, gdy użytkownik zmienia wartość w polu tekstowym (amount),
     * wybiera inną walutę "from" lub "to".
     */
    fun zaktualizujKontenerIPrzelicz(index: Int, zaktualizowanyKontener: C) {
        if (index >= 0 && index < _konteneryUI.value.size) {
            val obecneKontenery = _konteneryUI.value.toMutableList()
            obecneKontenery[index] = zaktualizowanyKontener // Bezpośrednio podstawiamy zaktualizowany kontener z UI
            _konteneryUI.value = obecneKontenery.toList()
            // Po każdej aktualizacji (zmiana kwoty, waluty from/to) przeliczamy wszystko
            // To upraszcza logikę, zamiast przeliczać tylko jeden kontener.
            // Dla małej liczby kontenerów nie powinno to stanowić problemu wydajnościowego.
            przeliczWszystkieKontenery()
        }
    }

    fun odswiezDostepneWaluty() {
        viewModelScope.launch {
            var noweUlubione = repository.loadFavoriteCurrencies()
            if (noweUlubione.isEmpty()) {
                noweUlubione = listOf(Waluta.EUR, Waluta.USD)
                repository.saveFavoriteCurrencies(noweUlubione)
            }
            _dostepneWalutyDlaKontenerow.value = noweUlubione

            // Zaktualizuj waluty w istniejących kontenerach, jeśli jest to konieczne
            val aktualneKontenery = _konteneryUI.value
            var czyModyfikacjaKontenerow = false
            val zaktualizowaneWalutyKontenerow = aktualneKontenery.map { staryKontener ->
                var nowaWalutaFrom = staryKontener.from
                var nowaWalutaTo = staryKontener.to
                var czyTenKontenerZmodyfikowany = false

                if (!noweUlubione.contains(staryKontener.from)) {
                    nowaWalutaFrom = noweUlubione.firstOrNull() ?: Waluta.EUR
                    czyTenKontenerZmodyfikowany = true
                }
                if (!noweUlubione.contains(staryKontener.to)) {
                    nowaWalutaTo = noweUlubione.firstOrNull { it != nowaWalutaFrom } ?: noweUlubione.firstOrNull() ?: Waluta.USD
                    czyTenKontenerZmodyfikowany = true
                }
                // Prosta logika: jeśli obie waluty są takie same, a jest więcej niż jedna ulubiona, spróbuj ustawić drugą inną.
                if (noweUlubione.size > 1 && nowaWalutaFrom == nowaWalutaTo) {
                    noweUlubione.firstOrNull { it != nowaWalutaFrom }?.let {
                        nowaWalutaTo = it
                        czyTenKontenerZmodyfikowany = true
                    }
                }

                if (czyTenKontenerZmodyfikowany) {
                    czyModyfikacjaKontenerow = true
                    // result zostanie zresetowany i przeliczony w przeliczWszystkieKontenery()
                    staryKontener.copy(from = nowaWalutaFrom, to = nowaWalutaTo, result = "")
                } else {
                    staryKontener
                }
            }

            if (czyModyfikacjaKontenerow) {
                _konteneryUI.value = zaktualizowaneWalutyKontenerow
            }
            // Zawsze przelicz po odświeżeniu ulubionych, ponieważ mogły się zmienić waluty w kontenerach
            przeliczWszystkieKontenery()
        }
    }


    private fun zdobadzMnoznikKonwersji(mapaKonwersji: Map<String, Double>, fromSymbol: String, toSymbol: String): Double {
        if (fromSymbol == toSymbol) return 1.0 // Zamiast BigDecimal.ONE
        mapaKonwersji["$fromSymbol-$toSymbol"]?.let { return it }

        val fromToEur = mapaKonwersji["$fromSymbol-EUR"]
        val eurToTo = mapaKonwersji["EUR-$toSymbol"]

        return if (fromToEur != null && eurToTo != null) {
            // Bezpośrednie mnożenie dla Double
            fromToEur * eurToTo
        } else {
            0.0 // Zamiast BigDecimal.ZERO
        }
    }

    private fun przeliczWszystkieKontenery() {
        // Ta funkcja teraz używa _mapaKursow.value, która jest aktualizowana przez odswiezKursyWalut()
        viewModelScope.launch {
            val aktualnaMapaKursow = _mapaKursow.value // Użyj mapy przechowywanej w ViewModel

            val konteneryDoPrzeliczenia = _konteneryUI.value
            val zaktualizowaneKontenery = konteneryDoPrzeliczenia.map { kontener ->
                if (aktualnaMapaKursow.isEmpty() && kontener.from.symbol != kontener.to.symbol) {
                    kontener.copy(result = if (kontener.amount.isNotBlank()) "?.??" else "")
                } else if (kontener.amount.isBlank()) {
                    kontener.copy(result = "")
                } else {
                    val kwotaDouble = try {
                        kontener.amount.replace(',', '.').toDouble()
                    } catch (e: NumberFormatException) {
                        null
                    }

                    if (kwotaDouble != null) {
                        val mnoznik = zdobadzMnoznikKonwersji(
                            aktualnaMapaKursow, // Użyj mapy z ViewModelu
                            kontener.from.symbol,
                            kontener.to.symbol
                        )
                        val wartoscPoKonwersji = kwotaDouble * mnoznik
                        val localeForUS = java.util.Locale("en", "US")
                        val sformatowanyWynik = String.format(localeForUS, "%.2f", wartoscPoKonwersji)
                        kontener.copy(result = sformatowanyWynik)
                    } else {
                        kontener.copy(result = "Error")
                    }
                }
            }
            _konteneryUI.value = zaktualizowaneKontenery
            zapiszKonteneryDoRepozytorium()
        }
    }

    private fun zapiszKonteneryDoRepozytorium() {
        viewModelScope.launch {
            repository.saveContainerData(ModelDanychKontenerow(_konteneryUI.value.size, _konteneryUI.value))
        }
    }
}