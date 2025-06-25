package com.example.currencyflow.viewmodel

import android.util.Log // Pamiętaj o tym imporcie
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyflow.data.model.C
import com.example.currencyflow.data.model.ModelDanychKontenerow
import com.example.currencyflow.data.model.Waluta
import com.example.currencyflow.data.repository.RepositoryData
import com.example.currencyflow.data.repository.UserDataRepository
import com.example.currencyflow.data.repository.WalutyRepository
import com.example.currencyflow.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RepositoryData,
    private val walutyRepository: WalutyRepository,
    private val userDataRepository: UserDataRepository,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val _konteneryUI = MutableStateFlow<List<C>>(emptyList())
    val konteneryUI: StateFlow<List<C>> = _konteneryUI.asStateFlow()

    private val _czyLadowanieKursow = MutableStateFlow(false)
    val czyLadowanieKursow: StateFlow<Boolean> = _czyLadowanieKursow.asStateFlow()

    // NOWA FLAGA GLOBALNA
    val canDeleteAnyContainer: StateFlow<Boolean> = _konteneryUI
        .map {
            Log.d("ViewModelFlags", "Mapping _konteneryUI.size = ${it.size} to canDeleteAnyContainer = ${it.size > 1}")
            it.size > 1
        } // Można usuwać, jeśli jest więcej niż 1 kontener
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _konteneryUI.value.size > 1 // Ustawienie wartości początkowej na podstawie aktualnego stanu _konteneryUI
        )

    private val _dostepneWalutyDlaKontenerow = MutableStateFlow<List<Waluta>>(emptyList())
    val dostepneWalutyDlaKontenerow: StateFlow<List<Waluta>> =
        _dostepneWalutyDlaKontenerow.asStateFlow()

    private val _mapaKursow = MutableStateFlow<Map<String, Double>>(emptyMap())
    private var aktualnyIdentyfikatorUzytkownika: String? = null

    // W HomeViewModel na górze, z innymi StateFlows
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Flaga do śledzenia, czy użytkownik był w stanie "offline"
    private var wasOfflineForSnackbar = false

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()


    init {
        Log.d("ViewModelLifecycle", "HomeViewModel init started")
        observeNetworkStatus() // Uruchamia własną korutynę

        viewModelScope.launch { // Główna korutyna inicjalizacyjna
            Log.d("ViewModelLifecycle", "init coroutine - Started")
            // Krok 1: Pobierz ID użytkownika (zakładając, że to szybkie lub suspend)
            val modelUzytkownika = userDataRepository.getUserDataModel()
            aktualnyIdentyfikatorUzytkownika = modelUzytkownika.id
            Log.d("ViewModelLifecycle", "init coroutine - User ID set: $aktualnyIdentyfikatorUzytkownika")

            // Krok 2: Uruchom ładowanie danych początkowych (bez join)
            // UI będzie obserwować _konteneryUI i _dostepneWalutyDlaKontenerow
            val jobLadujDane = ladujDanePoczatkowe()
            // Możesz poczekać na załadowanie danych kontenerów, jeśli są niezbędne do dalszych kroków
            // np. jeśli odświeżanie kursów wymaga informacji z kontenerów, ale na razie upraszczamy.
            // jobLadujDane.join() // Usunięte lub przemyślane

            // Krok 3: Uruchom odświeżanie kursów, jeśli potrzebne (bez join)
            // UI będzie obserwować _czyLadowanieKursow i _mapaKursow
            var kursyZostalyPobraneLubProcesZainicjowany = false
            if (connectivityObserver.getCurrentStatus() == ConnectivityObserver.Status.Available &&
                _mapaKursow.value.isEmpty() && aktualnyIdentyfikatorUzytkownika != null) {
                Log.i("ViewModelLifecycle", "init coroutine - Network available. Triggering initial rates refresh.")
                odswiezKursyWalut() // Uruchamia własną korutynę, nie czekaj na nią
                kursyZostalyPobraneLubProcesZainicjowany = true
            }

            // Krok 4: Poczekaj na załadowanie podstawowych danych jeśli to absolutnie konieczne
            // przed ustawieniem _isInitialized, ale staraj się tego unikać.
            // Na przykład, jeśli UI nie może nic sensownego wyświetlić bez listy kontenerów:
            jobLadujDane.join() // Przywrócone, jeśli kontenery są krytyczne dla pierwszego renderu

            // Krok 5: Jeśli kursy nie zostały pobrane, a mamy kontenery, wykonaj pierwsze przeliczenie
            // Ta logika może wymagać dostosowania w zależności od tego, kiedy dane są dostępne
            if (!kursyZostalyPobraneLubProcesZainicjowany && _konteneryUI.value.isNotEmpty()) {
                Log.d("ViewModelLifecycle", "init coroutine - Rates not (yet) fetched, performing initial calculation and save.")
                przeliczWszystkieKontenery()
            } else if (_konteneryUI.value.isEmpty() && jobLadujDane.isCompleted) {
                // Jeśli ładowanie danych się zakończyło, a kontenery nadal puste
                Log.d("ViewModelLifecycle", "init coroutine - Containers are empty after loading, calling przeliczWszystkieKontenery.")
                przeliczWszystkieKontenery()
            }
            // Jeśli odswiezKursyWalut() zostało wywołane, to ono samo wywoła przeliczWszystkieKontenery po pobraniu kursów.

            // Ustaw ViewModel jako zainicjalizowany wcześniej. UI powinno radzić sobie
            // z przejściowymi stanami ładowania poszczególnych danych.
            _isInitialized.value = true
            Log.i("ViewModelLifecycle", "HomeViewModel IS NOW CONSIDERED INITIALIZED (may still be loading data in background)")
        }
        Log.d("ViewModelLifecycle", "HomeViewModel init finished (constructor part)")
    }

    private fun observeNetworkStatus() {
        connectivityObserver.observe()
            .distinctUntilChanged() // Ważne, aby nie reagować wielokrotnie na ten sam status
            .onEach { status ->
                Log.i("ViewModelNetwork", "Network status changed: $status. Current courses empty: ${_mapaKursow.value.isEmpty()}, UserID: $aktualnyIdentyfikatorUzytkownika, WasOffline: $wasOfflineForSnackbar")

                when (status) {
                    ConnectivityObserver.Status.Available -> {
                        if (wasOfflineForSnackbar) { // Sprawdź, czy wcześniej byliśmy offline
                            _snackbarMessage.value = "Połączenie z siecią przywrócone."
                            wasOfflineForSnackbar = false // Zresetuj flagę
                        }

                        // --- ISTNIEJĄCA LOGIKA PONAWIANIA ---
                        if (aktualnyIdentyfikatorUzytkownika != null) {
                            if (_mapaKursow.value.isEmpty()) {
                                Log.i("ViewModelNetwork", "[Snackbar Logic] Network available and refresh conditions met (map empty). Triggering refresh.")
                                odswiezKursyWalut()
                            } else {
                                Log.i("ViewModelNetwork", "[Snackbar Logic] Network available, but rates seem up-to-date or no error. No automatic refresh.")
                            }
                        } else {
                            Log.w("ViewModelNetwork", "[Snackbar Logic] Network available, but User ID is null. Cannot refresh rates yet.")
                        }
                        // --- KONIEC ISTNIEJĄCEJ LOGIKI PONAWIANIA ---
                    }
                    ConnectivityObserver.Status.Lost,
                    ConnectivityObserver.Status.Unavailable -> {
                        Log.w("ViewModelNetwork", "Network lost or unavailable.")
                        _snackbarMessage.value = "Brak połączenia z internetem."
                        wasOfflineForSnackbar = true // Ustaw flagę, że byliśmy offline
                    }
                    ConnectivityObserver.Status.Losing -> {
                        // Możesz dodać logikę dla 'Losing' jeśli chcesz, np. "Połączenie sieciowe jest niestabilne."
                        // Na razie pomijamy, aby było prościej.
                        Log.i("ViewModelNetwork", "Network is losing.")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun odswiezKursyWalut(): Job {
        val idUzytkownika = aktualnyIdentyfikatorUzytkownika
        if (idUzytkownika == null) {
            Log.w("ViewModelRates", "odswiezKursyWalut - Aborted: User ID is null.")
            _czyLadowanieKursow.value = false // Upewnij się, że flaga ładowania jest zresetowana
            return Job().apply { complete() } // Zwróć zakończony Job
        }

        if (_czyLadowanieKursow.value) {
            Log.d("ViewModelRates", "odswiezKursyWalut - Aborted: Already in progress.")
            return Job().apply { complete() } // Zwróć zakończony Job
        }

        Log.i("ViewModelRates", "odswiezKursyWalut - Starting for user: $idUzytkownika")
        return viewModelScope.launch {
            _czyLadowanieKursow.value = true

            walutyRepository.pobierzAktualneKursy(idUzytkownika)
                .catch { e ->
                    Log.e("ViewModelRates", "odswiezKursyWalut - Error in exchange rate flow", e)
                    _mapaKursow.value = emptyMap() // W razie błędu w flow, ustaw pustą mapę
                    _czyLadowanieKursow.value = false
                    // Dodatkowy snackbar dla błędu serwera/pobierania
                    if (connectivityObserver.getCurrentStatus() == ConnectivityObserver.Status.Available) { // Tylko jeśli sieć JEST, a mimo to błąd
                        _snackbarMessage.value = "Wystąpił błąd podczas pobierania kursów z serwera."
                    } // Jeśli sieci nie ma, snackbar o braku sieci już powinien być aktywny

                    przeliczWszystkieKontenery()
                }
                .collect { pobraneKursy ->
                    Log.i("ViewModelRates", "odswiezKursyWalut - Received rates (might be empty): ${pobraneKursy.size} entries")
                    _mapaKursow.value = pobraneKursy
                    _czyLadowanieKursow.value = false

                    if (pobraneKursy.isEmpty()) {
                        Log.w("ViewModelRates", "odswiezKursyWalut - Rates are empty, and no network error was caught. Possible server issue or no data.")
                    }
                    przeliczWszystkieKontenery()
                }
        }
    }

    private fun ladujDanePoczatkowe(): Job {
        return viewModelScope.launch {
            Log.d("ViewModelData", "ladujDanePoczatkowe - Started")
            val obecneUlubione = repository.loadFavoriteCurrencies() // Załóżmy, że to jest suspend lub szybkie
            if (obecneUlubione.isEmpty()) {
                val domyslneUlubione = listOf(Waluta.EUR, Waluta.USD)
                repository.saveFavoriteCurrencies(domyslneUlubione)
                _dostepneWalutyDlaKontenerow.value = domyslneUlubione
            } else {
                _dostepneWalutyDlaKontenerow.value = obecneUlubione
            }
            Log.d("ViewModelData", "ladujDanePoczatkowe - Favorite currencies loaded: ${_dostepneWalutyDlaKontenerow.value.map { it.symbol }}")

            val zapisaneDaneKontenerow = repository.loadContainerData() // Załóżmy, że to jest suspend lub szybkie
            if (zapisaneDaneKontenerow != null && zapisaneDaneKontenerow.kontenery.isNotEmpty()) {
                _konteneryUI.value = zapisaneDaneKontenerow.kontenery
                Log.d("ViewModelData", "ladujDanePoczatkowe - Loaded ${zapisaneDaneKontenerow.kontenery.size} saved containers.")
            } else {
                Log.d("ViewModelData", "ladujDanePoczatkowe - No saved containers, creating default.")
                val pierwszaDostepna = _dostepneWalutyDlaKontenerow.value.firstOrNull() ?: Waluta.EUR
                val drugaDostepna = _dostepneWalutyDlaKontenerow.value.getOrNull(1)
                    ?: _dostepneWalutyDlaKontenerow.value.firstOrNull() // Jeśli jest tylko jedna, użyj jej
                    ?: Waluta.USD // Fallback

                // Upewnij się, że 'to' jest inne niż 'from', jeśli to możliwe
                val walutaDo = if (pierwszaDostepna != drugaDostepna) {
                    drugaDostepna
                } else {
                    _dostepneWalutyDlaKontenerow.value.firstOrNull { it != pierwszaDostepna } ?: pierwszaDostepna
                }

                val domyslnyKontener = C(from = pierwszaDostepna, to = walutaDo, amount = "1", result = "")
                _konteneryUI.value = listOf(domyslnyKontener)
                Log.d("ViewModelData", "ladujDanePoczatkowe - Created default container: $domyslnyKontener")
            }
            //przeliczWszystkieKontenery()
           // Log.d("ViewModelData", "ladujDanePoczatkowe - Finished, przeliczWszystkieKontenery called.")
        }
    }

    fun dodajKontener() {
        val domyslnaWalutaFrom = _dostepneWalutyDlaKontenerow.value.firstOrNull() ?: Waluta.EUR
        val domyslnaWalutaTo = _dostepneWalutyDlaKontenerow.value.firstOrNull { it != domyslnaWalutaFrom }
            ?: _dostepneWalutyDlaKontenerow.value.firstOrNull() // Jeśli tylko jedna lub brak innych, użyj tej samej
            ?: Waluta.USD // Ostateczny fallback

        val nowyKontener = C(from = domyslnaWalutaFrom, to = domyslnaWalutaTo, amount = "1", result = "")
        _konteneryUI.value += nowyKontener
        Log.d("ViewModelActions", "dodajKontener - Added: $nowyKontener. Recalculating all.")
        przeliczWszystkieKontenery()
    }

    fun usunKontenerPoId(idDoUsuniecia: String) {
        Log.d("ViewModelActions", "usunKontenerPoId called for ID: $idDoUsuniecia. Current canDeleteAnyContainer.value: ${canDeleteAnyContainer.value}, _konteneryUI.size: ${_konteneryUI.value.size}")
        if (!canDeleteAnyContainer.value && _konteneryUI.value.any { it.id == idDoUsuniecia }) {
            Log.w("ViewModelActions", "usunKontenerPoId - Attempted to delete when not allowed (e.g., last item). Action aborted by ViewModel guard.")
            _snackbarMessage.value = "Nie można usunąć ostatniego przelicznika."
            return
        }

        val rozmiarPrzedUsunieciem = _konteneryUI.value.size
        _konteneryUI.update { aktualnaLista ->
            val nowaLista = aktualnaLista.filterNot { it.id == idDoUsuniecia }
            if (nowaLista.size < aktualnaLista.size) {
                Log.d("ViewModelActions", "usunKontenerPoId - Removed container with ID: $idDoUsuniecia.")
            } else {
                Log.w("ViewModelActions", "usunKontenerPoId - Container with ID: $idDoUsuniecia not found for removal.")
            }
            nowaLista
        }

        if (_konteneryUI.value.size < rozmiarPrzedUsunieciem) { // Sprawdź, czy faktycznie usunięto
            Log.d("ViewModelActions", "usunKontenerPoId - Saving to repository after removal.")
            zapiszKonteneryDoRepozytorium()
        }
    }

    fun zaktualizujKontenerIPrzelicz(idKontenera: String, zaktualizowanyKontener: C) {
        _konteneryUI.update { aktualnaLista ->
            val nowaLista = aktualnaLista.map { kontener ->
                if (kontener.id == idKontenera) {
                    zaktualizowanyKontener
                } else {
                    kontener
                }
            }
            if (nowaLista != aktualnaLista) { // Sprawdź, czy faktycznie coś się zmieniło
                Log.d("ViewModelActions", "zaktualizujKontenerPoIdIPrzelicz - Updated container with ID $idKontenera to: $zaktualizowanyKontener. Recalculating all.")
            }
            nowaLista
        }
        przeliczWszystkieKontenery()
    }

    fun odswiezDostepneWaluty() {
        viewModelScope.launch {
            Log.d("ViewModelData", "odswiezDostepneWaluty - Started")
            var noweUlubione = repository.loadFavoriteCurrencies()
            if (noweUlubione.isEmpty()) {
                noweUlubione = listOf(Waluta.EUR, Waluta.USD)
                // Rozważ, czy ten zapis ulubionych jest tu zawsze potrzebny,
                // czy może powinien być obsługiwany bardziej centralnie przy inicjalizacji, jeśli ulubione są puste.
                // Na razie zostawiam, ale to drobna rzecz do przemyślenia w kontekście "single source of truth" dla domyślnych ulubionych.
                repository.saveFavoriteCurrencies(noweUlubione)
            }
            // Sprawdź, czy lista faktycznie się zmieniła, aby uniknąć niepotrzebnego przetwarzania, jeśli jest identyczna.
            if (_dostepneWalutyDlaKontenerow.value == noweUlubione && _konteneryUI.value.isNotEmpty()) {
                Log.d("ViewModelData", "odswiezDostepneWaluty - Favorite currencies are the same as current. Checking if recalculation is needed due to empty rates.")
                // Jeśli kursy są puste, to znaczy, że poprzednie przeliczenie mogło dać złe wyniki.
                // Warto przeliczyć (i potencjalnie zapisać, jeśli `przeliczWszystkieKontenery` tak zdecyduje)
                if (_mapaKursow.value.isEmpty()) {
                    Log.d("ViewModelData", "odswiezDostepneWaluty - Rates are empty, forcing full recalculation and save.")
                    przeliczWszystkieKontenery()
                } else {
                    // Jeśli ulubione te same i kursy są, to nie ma potrzeby robić nic więcej.
                    // Chyba że chcemy wymusić aktualizację UI, wtedy `przeliczKonteneryBezZapisu()`.
                    // Na razie załóżmy, że UI jest spójne.
                    Log.d("ViewModelData", "odswiezDostepneWaluty - Favorite currencies and rates are current. No structural changes or recalculation needed.")
                }
                // Zakończ wcześniej, jeśli ulubione się nie zmieniły i nie ma potrzeby przeliczania z powodu pustych kursów.
                return@launch
            }


            _dostepneWalutyDlaKontenerow.value = noweUlubione
            Log.d("ViewModelData", "odswiezDostepneWaluty - New favorite currencies set: ${noweUlubione.map { it.symbol }}")

            val aktualneKontenery = _konteneryUI.value
            var czyStrukturaKontenerowZmodyfikowana = false // Zmieniona nazwa flagi dla jasności
            val zaktualizowaneWalutyKontenerow = aktualneKontenery.map { staryKontener ->
                var nowaWalutaFrom = staryKontener.from
                var nowaWalutaTo = staryKontener.to
                var czyTenKontenerZmodyfikowany = false

                if (!noweUlubione.contains(staryKontener.from)) {
                    nowaWalutaFrom = noweUlubione.firstOrNull() ?: Waluta.EUR // Domyślna, jeśli lista ulubionych jest pusta
                    czyTenKontenerZmodyfikowany = true
                }
                if (!noweUlubione.contains(staryKontener.to)) {
                    nowaWalutaTo = noweUlubione.firstOrNull { it != nowaWalutaFrom }
                        ?: noweUlubione.firstOrNull() // Jeśli tylko jedna ulubiona lub ta sama co 'from'
                                ?: Waluta.USD // Ostateczny fallback
                    czyTenKontenerZmodyfikowany = true
                }

                // Dodatkowe sprawdzenie: jeśli mamy więcej niż jedną ulubioną walutę,
                // a po potencjalnych zmianach powyżej 'from' i 'to' są takie same,
                // spróbujmy ustawić 'to' na inną dostępną ulubioną.
                if (noweUlubione.size > 1 && nowaWalutaFrom == nowaWalutaTo) {
                    noweUlubione.firstOrNull { it != nowaWalutaFrom }?.let {
                        nowaWalutaTo = it
                        czyTenKontenerZmodyfikowany = true
                    }
                }

                if (czyTenKontenerZmodyfikowany) {
                    czyStrukturaKontenerowZmodyfikowana = true // Ustaw główną flagę
                    Log.d("ViewModelData", "odswiezDostepneWaluty - Modifying container ${staryKontener.id}: from=${staryKontener.from.symbol} to ${nowaWalutaFrom.symbol}, to=${staryKontener.to.symbol} to ${nowaWalutaTo.symbol}")
                    staryKontener.copy(from = nowaWalutaFrom, to = nowaWalutaTo, result = "") // Zeruj wynik, bo waluty się zmieniły
                } else {
                    staryKontener
                }
            }

            if (czyStrukturaKontenerowZmodyfikowana) {
                _konteneryUI.value = zaktualizowaneWalutyKontenerow
                Log.d("ViewModelData", "odswiezDostepneWaluty - Containers' structure MODIFIED due to favorite currency changes. New list size: ${zaktualizowaneWalutyKontenerow.size}. Triggering full recalculation and SAVE.")
                przeliczWszystkieKontenery() // Zapisze zmiany, bo struktura się zmieniła
            } else {
                Log.d("ViewModelData", "odswiezDostepneWaluty - Containers' structure UNCHANGED by favorite currency update. Triggering recalculation for UI update WITHOUT SAVE (unless rates are empty).")
                // Jeśli kursy są puste, to nawet jeśli struktura się nie zmieniła, chcemy pełne przeliczenie z zapisem
                // na wypadek, gdyby poprzednio wyniki były "0.00"
                if (_mapaKursow.value.isEmpty() && _konteneryUI.value.isNotEmpty()) {
                    Log.d("ViewModelData", "odswiezDostepneWaluty - Rates are empty, but structure is fine. Forcing full recalculation and SAVE to update results.")
                    przeliczWszystkieKontenery()
                } else {
                    przeliczKonteneryBezZapisu() // Tylko aktualizuj wyniki w UI, bez zapisu
                }
            }
            Log.d("ViewModelData", "odswiezDostepneWaluty - Finished.")
        }
    }

    private fun przeliczKonteneryBezZapisu() {
        viewModelScope.launch {
            val aktualnaMapaKursow = _mapaKursow.value
            Log.d("ViewModelCalc", "przeliczKonteneryBezZapisu - Started. Rates empty: ${aktualnaMapaKursow.isEmpty()}. Containers: ${_konteneryUI.value.size}")

            val zaktualizowaneKontenery = _konteneryUI.value.map { kontener ->
                if (kontener.amount.isBlank()) {
                    kontener.copy(result = "")
                } else {
                    val kwotaDouble = try {
                        kontener.amount.replace(',', '.').toDouble()
                    } catch (e: NumberFormatException) {
                        Log.w("ViewModelCalc", "Error parsing amount: '${kontener.amount}' for container ${kontener.id}. Setting result to 'Error'.") // Dodano ID dla lepszego logowania
                        null
                    }

                    if (kwotaDouble != null) {
                        if (aktualnaMapaKursow.isEmpty() && kontener.from.symbol != kontener.to.symbol) {
                            kontener.copy(result = "0.00")
                        } else {
                            val mnoznik = zdobadzMnoznikKonwersji(
                                aktualnaMapaKursow,
                                kontener.from.symbol,
                                kontener.to.symbol
                            )
                            if (mnoznik == 0.0 && kontener.from.symbol != kontener.to.symbol) {
                                Log.w("ViewModelCalc", "Multiplier is 0.0 for ${kontener.from.symbol} to ${kontener.to.symbol} in container ${kontener.id}. Setting result to '0.00'")
                                kontener.copy(result = "0.00")
                            } else {
                                val wartoscPoKonwersji = kwotaDouble * mnoznik
                                val localeForUS = java.util.Locale("en", "US")
                                val sformatowanyWynik = String.format(localeForUS, "%.4f", wartoscPoKonwersji)
                                kontener.copy(result = sformatowanyWynik)
                            }
                        }
                    } else {
                        kontener.copy(result = "Error")
                    }
                }
            }
            _konteneryUI.value = zaktualizowaneKontenery
            Log.d("ViewModelCalc", "przeliczKonteneryBezZapisu - Finished. Updated UI containers list. NO SAVE to repo.")
            // USUNIĘTO: zapiszKonteneryDoRepozytorium()
        }
    }

    private fun zdobadzMnoznikKonwersji(mapaKonwersji: Map<String, Double>, fromSymbol: String, toSymbol: String): Double {
        if (fromSymbol == toSymbol) return 1.0
        mapaKonwersji["$fromSymbol-$toSymbol"]?.let { return it }

        // Próba konwersji przez EUR jako walutę pośredniczącą
        // X -> EUR -> Y  ==> kurs(X->EUR) * kurs(EUR->Y)
        val fromToEur = mapaKonwersji["$fromSymbol-EUR"]
        val eurToTo = mapaKonwersji["EUR-$toSymbol"]
        if (fromToEur != null && eurToTo != null) {
            return fromToEur * eurToTo
        }

        // Próba konwersji, jeśli mamy kursy względem EUR:
        // EUR -> X i EUR -> Y ==> kurs(X->Y) = kurs(EUR->Y) / kurs(EUR->X)
        val eurToFrom = mapaKonwersji["EUR-$fromSymbol"]
        // eurToTo już mamy z poprzedniej próby
        if (eurToFrom != null && eurToFrom != 0.0 && eurToTo != null) {
            return eurToTo / eurToFrom
        }

        Log.w("ViewModelCalc", "Nie znaleziono ścieżki konwersji dla $fromSymbol -> $toSymbol. Mapa kursów: $mapaKonwersji")
        return 0.0 // Nie znaleziono odpowiedniego mnożnika
    }

    private fun przeliczWszystkieKontenery() {
        viewModelScope.launch {
            val aktualnaMapaKursow = _mapaKursow.value
            Log.d("ViewModelCalc", "przeliczWszystkieKontenery - Started. Rates empty: ${aktualnaMapaKursow.isEmpty()}. Containers: ${_konteneryUI.value.size}")

            val zaktualizowaneKontenery = _konteneryUI.value.map { kontener ->
                if (kontener.amount.isBlank()) {
                    kontener.copy(result = "")
                } else {
                    val kwotaDouble = try {
                        kontener.amount.replace(',', '.').toDouble()
                    } catch (e: NumberFormatException) {
                        Log.w("ViewModelCalc", "Error parsing amount: '${kontener.amount}' for container id (if present). Setting result to 'Error'.")
                        null
                    }

                    if (kwotaDouble != null) {
                        if (aktualnaMapaKursow.isEmpty() && kontener.from.symbol != kontener.to.symbol) {
                            // Jeśli mapa kursów jest pusta (np. błąd sieci) i waluty są różne
                            kontener.copy(result = "0.00")
                        } else {
                            val mnoznik = zdobadzMnoznikKonwersji(
                                aktualnaMapaKursow,
                                kontener.from.symbol,
                                kontener.to.symbol
                            )
                            if (mnoznik == 0.0 && kontener.from.symbol != kontener.to.symbol) {
                                // Jeśli mnożnik to 0.0, a waluty nie są takie same, prawdopodobnie nie ma ścieżki konwersji
                                Log.w("ViewModelCalc", "Multiplier is 0.0 for ${kontener.from.symbol} to ${kontener.to.symbol}. Setting result to '?.??'")
                                kontener.copy(result = "0.00")
                            } else {
                                val wartoscPoKonwersji = kwotaDouble * mnoznik
                                val localeForUS = java.util.Locale("en", "US") // Dla spójnego formatowania z kropką
                                val sformatowanyWynik = String.format(localeForUS, "%.4f", wartoscPoKonwersji)
                                kontener.copy(result = sformatowanyWynik)
                            }
                        }
                    } else {
                        // kwotaDouble jest null (błąd parsowania)
                        kontener.copy(result = "Error")
                    }
                }
            }
            _konteneryUI.value = zaktualizowaneKontenery
            Log.d("ViewModelCalc", "przeliczWszystkieKontenery - Finished. Updated UI containers list. Saving to repo.")
            zapiszKonteneryDoRepozytorium()
        }
    }

    private fun zapiszKonteneryDoRepozytorium() {
        viewModelScope.launch {
            Log.d("ViewModelData", "zapiszKonteneryDoRepozytorium - Saving ${_konteneryUI.value.size} containers.")
            repository.saveContainerData(ModelDanychKontenerow(_konteneryUI.value.size, _konteneryUI.value))
        }
    }
    fun snackbarMessageShown() {
        _snackbarMessage.value = null
    }
}