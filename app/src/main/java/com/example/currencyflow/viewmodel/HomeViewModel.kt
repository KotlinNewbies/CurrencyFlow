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

    private val _bladPobieraniaKursow = MutableStateFlow<String?>(null)
    val bladPobieraniaKursow: StateFlow<String?> = _bladPobieraniaKursow.asStateFlow()

    private val _dostepneWalutyDlaKontenerow = MutableStateFlow<List<Waluta>>(emptyList())
    val dostepneWalutyDlaKontenerow: StateFlow<List<Waluta>> =
        _dostepneWalutyDlaKontenerow.asStateFlow()

    private val _mapaKursow = MutableStateFlow<Map<String, Double>>(emptyMap())
    private var aktualnyIdentyfikatorUzytkownika: String? = null

    init {
        Log.d("ViewModelLifecycle", "HomeViewModel init started")

        // 1. Rozpocznij obserwację stanu sieci od razu.
        observeNetworkStatus()

        // 2. Uruchom korutynę do zadań inicjalizacyjnych (ID użytkownika, dane początkowe).
        viewModelScope.launch {
            Log.d("ViewModelLifecycle", "init coroutine - Started")

            // 2a. Pobierz ID użytkownika.
            // Zakładamy, że userDataRepository.getUserDataModel() jest albo szybkie,
            // albo jest funkcją suspend (jeśli jest suspend, ta korutyna poczeka).
            val modelUzytkownika = userDataRepository.getUserDataModel()
            aktualnyIdentyfikatorUzytkownika = modelUzytkownika.id
            Log.d("ViewModelLifecycle", "init coroutine - User ID set: $aktualnyIdentyfikatorUzytkownika")

            // 2b. Załaduj dane początkowe kontenerów i ulubionych.
            // Funkcja ladujDanePoczatkowe() uruchamia własną korutynę.
            ladujDanePoczatkowe()
            Log.d("ViewModelLifecycle", "init coroutine - ladujDanePoczatkowe() called (runs in its own scope).")

            // 2c. Po ustawieniu ID i zainicjowaniu ładowania danych,
            // sprawdź, czy od razu trzeba odświeżyć kursy.
            // `observeNetworkStatus` również to zrobi, jeśli połączenie pojawi się później
            // lub było dostępne od początku. To jest dodatkowy trigger.
            if (connectivityObserver.getCurrentStatus() == ConnectivityObserver.Status.Available &&
                _mapaKursow.value.isEmpty() && aktualnyIdentyfikatorUzytkownika != null) {
                Log.i("ViewModelLifecycle", "init coroutine - Network available and initial refresh needed. Triggering.")
                odswiezKursyWalut()
            } else {
                Log.d("ViewModelLifecycle", "init coroutine - Not calling odswiezKursyWalut explicitly (net=${connectivityObserver.getCurrentStatus()}, coursesEmpty=${_mapaKursow.value.isEmpty()}, userIdNull=${aktualnyIdentyfikatorUzytkownika == null})")
            }
            Log.d("ViewModelLifecycle", "init coroutine - Finished")
        }
        Log.d("ViewModelLifecycle", "HomeViewModel init finished")
    }

    private fun observeNetworkStatus() {
        connectivityObserver.observe()
            .onEach { status ->
                Log.i("ViewModelNetwork", "Network status changed: $status. Current courses empty: ${_mapaKursow.value.isEmpty()}, error: ${_bladPobieraniaKursow.value != null}, UserID: $aktualnyIdentyfikatorUzytkownika")
                if (status == ConnectivityObserver.Status.Available) {
                    // Odśwież kursy tylko jeśli:
                    // 1. Mamy ID użytkownika.
                    // 2. Mapa kursów jest pusta LUB wystąpił wcześniej błąd pobierania.
                    if (aktualnyIdentyfikatorUzytkownika != null) {
                        if (_mapaKursow.value.isEmpty() || _bladPobieraniaKursow.value != null) {
                            Log.i("ViewModelNetwork", "Network available and refresh conditions met. Triggering refresh.")
                            odswiezKursyWalut()
                        } else {
                            Log.i("ViewModelNetwork", "Network available, but rates seem up-to-date or no error. No automatic refresh.")
                        }
                    } else {
                        Log.w("ViewModelNetwork", "Network available, but User ID is null. Cannot refresh rates yet.")
                        // Można rozważyć ustawienie _bladPobieraniaKursow, aby po ustawieniu ID odświeżenie nastąpiło.
                        // _bladPobieraniaKursow.value = "Oczekiwanie na ID użytkownika do pobrania kursów."
                    }
                } else if (status == ConnectivityObserver.Status.Lost || status == ConnectivityObserver.Status.Unavailable) {
                    Log.w("ViewModelNetwork", "Network lost or unavailable.")
                    // Możesz tutaj np. ustawić _bladPobieraniaKursow, aby poinformować użytkownika
                    // lub aby przywrócenie sieci wywołało odświeżenie.
                    // _bladPobieraniaKursow.value = "Brak połączenia z internetem."
                }
            }
            .launchIn(viewModelScope) // Uruchamia kolekcję flow w viewModelScope
    }

    fun odswiezKursyWalut() {
        val idUzytkownika = aktualnyIdentyfikatorUzytkownika
        if (idUzytkownika == null) {
            Log.w("ViewModelRates", "odswiezKursyWalut - Aborted: User ID is null.")
            _bladPobieraniaKursow.value = "Brak ID użytkownika. Nie można odświeżyć kursów."
            _czyLadowanieKursow.value = false // Upewnij się, że flaga ładowania jest zresetowana
            return
        }

        if (_czyLadowanieKursow.value) {
            Log.d("ViewModelRates", "odswiezKursyWalut - Aborted: Already in progress.")
            return
        }

        Log.i("ViewModelRates", "odswiezKursyWalut - Starting for user: $idUzytkownika")
        viewModelScope.launch {
            _czyLadowanieKursow.value = true
            _bladPobieraniaKursow.value = null // Resetuj błąd przed nową próbą

            walutyRepository.pobierzAktualneKursy(idUzytkownika)
                .catch { e ->
                    Log.e("ViewModelRates", "odswiezKursyWalut - Error in exchange rate flow", e)
                    _mapaKursow.value = emptyMap() // W razie błędu w flow, ustaw pustą mapę
                    _bladPobieraniaKursow.value = "Błąd pobierania kursów: ${e.localizedMessage ?: "Nieznany błąd sieciowy"}"
                    _czyLadowanieKursow.value = false
                    przeliczWszystkieKontenery() // Przelicz, aby pokazać "?.??"
                }
                .collect { pobraneKursy ->
                    Log.i("ViewModelRates", "odswiezKursyWalut - Received rates (might be empty): ${pobraneKursy.size} entries")
                    _mapaKursow.value = pobraneKursy
                    _czyLadowanieKursow.value = false

                    if (pobraneKursy.isEmpty() && _bladPobieraniaKursow.value == null) {
                        Log.w("ViewModelRates", "odswiezKursyWalut - Rates are empty, and no network error was caught. Possible server issue or no data.")
                        // Możesz chcieć ustawić tu specyficzny komunikat, jeśli puste kursy bez błędu sieciowego to problem
                        // _bladPobieraniaKursow.value = "Nie udało się pobrać aktualnych kursów walut (brak danych)."
                    }
                    przeliczWszystkieKontenery()
                }
        }
    }

    fun wyczyscBladPobieraniaKursow() {
        _bladPobieraniaKursow.value = null
    }

    // ladujDanePoczatkowe() uruchamia własną korutynę.
    // Jeśli operacje wewnątrz (loadFavoriteCurrencies, loadContainerData) są funkcjami `suspend`,
    // to wykonają się sekwencyjnie w tej wewnętrznej korutynie.
    private fun ladujDanePoczatkowe() {
        viewModelScope.launch {
            Log.d("ViewModelData", "ladujDanePoczatkowe - Started")
            // Krok 1: Ładowanie/Inicjalizacja ulubionych walut
            val obecneUlubione = repository.loadFavoriteCurrencies() // Załóżmy, że to jest suspend lub szybkie
            if (obecneUlubione.isEmpty()) {
                val domyslneUlubione = listOf(Waluta.EUR, Waluta.USD)
                repository.saveFavoriteCurrencies(domyslneUlubione)
                _dostepneWalutyDlaKontenerow.value = domyslneUlubione
            } else {
                _dostepneWalutyDlaKontenerow.value = obecneUlubione
            }
            Log.d("ViewModelData", "ladujDanePoczatkowe - Favorite currencies loaded: ${_dostepneWalutyDlaKontenerow.value.map { it.symbol }}")

            // Krok 2: Ładowanie/Inicjalizacja kontenerów
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
                    // Jeśli pierwsza i druga są takie same (np. tylko jedna ulubiona),
                    // spróbuj znaleźć inną, jeśli jest więcej niż jedna ulubiona.
                    _dostepneWalutyDlaKontenerow.value.firstOrNull { it != pierwszaDostepna } ?: pierwszaDostepna
                }

                val domyslnyKontener = C(from = pierwszaDostepna, to = walutaDo, amount = "1", result = "")
                _konteneryUI.value = listOf(domyslnyKontener)
                Log.d("ViewModelData", "ladujDanePoczatkowe - Created default container: $domyslnyKontener")
            }

            // Krok 3: Przelicz wartości dla załadowanych/domyślnych kontenerów
            // To wywołanie jest tutaj ważne, aby UI miało co pokazać od razu,
            // nawet jeśli kursy nie są jeszcze dostępne (pokaże "?.??").
            przeliczWszystkieKontenery()
            Log.d("ViewModelData", "ladujDanePoczatkowe - Finished, przeliczWszystkieKontenery called.")
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
        _konteneryUI.update { aktualnaLista ->
            val nowaLista = aktualnaLista.filterNot { it.id == idDoUsuniecia }
            if (nowaLista.size < aktualnaLista.size) {
                Log.d("ViewModelActions", "usunKontenerPoId - Removed container with ID: $idDoUsuniecia. Saving.")
            }
            nowaLista
        }
        zapiszKonteneryDoRepozytorium()
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
        // zapiszKonteneryDoRepozytorium() jest już w przeliczWszystkieKontenery()
    }

    fun odswiezDostepneWaluty() {
        viewModelScope.launch {
            Log.d("ViewModelData", "odswiezDostepneWaluty - Started")
            var noweUlubione = repository.loadFavoriteCurrencies()
            if (noweUlubione.isEmpty()) {
                noweUlubione = listOf(Waluta.EUR, Waluta.USD)
                repository.saveFavoriteCurrencies(noweUlubione)
            }
            _dostepneWalutyDlaKontenerow.value = noweUlubione
            Log.d("ViewModelData", "odswiezDostepneWaluty - New favorite currencies: ${noweUlubione.map { it.symbol }}")

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
                    nowaWalutaTo = noweUlubione.firstOrNull { it != nowaWalutaFrom }
                        ?: noweUlubione.firstOrNull()
                                ?: Waluta.USD
                    czyTenKontenerZmodyfikowany = true
                }
                if (noweUlubione.size > 1 && nowaWalutaFrom == nowaWalutaTo) {
                    noweUlubione.firstOrNull { it != nowaWalutaFrom }?.let {
                        nowaWalutaTo = it
                        czyTenKontenerZmodyfikowany = true
                    }
                }

                if (czyTenKontenerZmodyfikowany) {
                    czyModyfikacjaKontenerow = true
                    Log.d("ViewModelData", "odswiezDostepneWaluty - Modifying container ${staryKontener}: from=${staryKontener.from.symbol} to $nowaWalutaFrom, to=${staryKontener.to.symbol} to $nowaWalutaTo")
                    staryKontener.copy(from = nowaWalutaFrom, to = nowaWalutaTo, result = "")
                } else {
                    staryKontener
                }
            }

            if (czyModyfikacjaKontenerow) {
                _konteneryUI.value = zaktualizowaneWalutyKontenerow
                Log.d("ViewModelData", "odswiezDostepneWaluty - Containers modified. New list size: ${zaktualizowaneWalutyKontenerow.size}")
            }
            przeliczWszystkieKontenery()
            Log.d("ViewModelData", "odswiezDostepneWaluty - Finished, przeliczWszystkieKontenery called.")
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
                                val sformatowanyWynik = String.format(localeForUS, "%.2f", wartoscPoKonwersji)
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
}