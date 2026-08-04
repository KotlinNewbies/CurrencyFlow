package com.fluida.currencyflow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluida.currencyflow.data.model.C
import com.fluida.currencyflow.data.model.ModelDanychKontenerow
import com.fluida.currencyflow.data.model.Waluta
import com.fluida.currencyflow.data.repository.RepositoryData
import com.fluida.currencyflow.data.repository.UserDataRepository
import com.fluida.currencyflow.data.repository.WalutyRepository
import com.fluida.currencyflow.util.ConnectivityObserver
import com.fluida.currencyflow.util.CurrencyCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val konteneryUI: List<C> = emptyList(),
    val czyLadowanieKursow: Boolean = false,
    val dostepneWalutyDlaKontenerow: List<Waluta> = emptyList(),
    val isInitialized: Boolean = false,
    val canDeleteAnyContainer: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RepositoryData,
    private val walutyRepository: WalutyRepository,
    private val userDataRepository: UserDataRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val calculator: CurrencyCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _mapaKursow = MutableStateFlow<Map<String, Double>>(emptyMap())
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var aktualnyIdentyfikatorUzytkownika: String? = null
    private var wasOfflineForSnackbar = false

    init {
        initialization()
        observeNetworkStatus()
    }

    private fun initialization() = viewModelScope.launch {
        Log.d("HomeViewModel", "Initialization started")
        
        // 1. Pobierz ID użytkownika
        aktualnyIdentyfikatorUzytkownika = userDataRepository.getUserDataModel().id
        
        // 2. Ładuj dane poczatkowe (ulubione i kontenery)
        val favoriteCurrencies = repository.loadFavoriteCurrencies().let {
            if (it.isEmpty()) {
                val defaults = listOf(Waluta.EUR, Waluta.USD)
                repository.saveFavoriteCurrencies(defaults)
                defaults
            } else it
        }

        val savedContainers = repository.loadContainerData()?.kontenery ?: createDefaultContainers(favoriteCurrencies)

        _uiState.update { it.copy(
            dostepneWalutyDlaKontenerow = favoriteCurrencies,
            konteneryUI = savedContainers,
            isInitialized = true,
            canDeleteAnyContainer = savedContainers.size > 1
        ) }

        // 3. Sprawdź sieć i odśwież kursy jeśli to możliwe
        if (connectivityObserver.getCurrentStatus() == ConnectivityObserver.Status.Available && aktualnyIdentyfikatorUzytkownika != null) {
            odswiezKursyWalut()
        } else {
            recalculateAll(save = false)
        }
    }

    private fun createDefaultContainers(favorites: List<Waluta>): List<C> {
        val from = favorites.firstOrNull() ?: Waluta.EUR
        val to = favorites.getOrNull(1) ?: favorites.firstOrNull { it != from } ?: Waluta.USD
        return listOf(C(from = from, to = to, amount = "1", result = ""))
    }

    private fun observeNetworkStatus() {
        connectivityObserver.observe()
            .distinctUntilChanged()
            .onEach { status ->
                when (status) {
                    ConnectivityObserver.Status.Available -> {
                        if (wasOfflineForSnackbar) {
                            _snackbarMessage.value = "Połączenie z siecią przywrócone."
                            wasOfflineForSnackbar = false
                        }
                        if (_mapaKursow.value.isEmpty() && aktualnyIdentyfikatorUzytkownika != null) {
                            odswiezKursyWalut()
                        }
                    }
                    ConnectivityObserver.Status.Lost, ConnectivityObserver.Status.Unavailable -> {
                        _snackbarMessage.value = "Brak połączenia z internetem."
                        wasOfflineForSnackbar = true
                    }
                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    fun odswiezKursyWalut() {
        val userId = aktualnyIdentyfikatorUzytkownika ?: return
        if (_uiState.value.czyLadowanieKursow) return

        viewModelScope.launch {
            _uiState.update { it.copy(czyLadowanieKursow = true) }
            
            walutyRepository.pobierzAktualneKursy(userId)
                .catch { e ->
                    Log.e("HomeViewModel", "Error fetching rates", e)
                    _uiState.update { it.copy(czyLadowanieKursow = false) }
                    if (connectivityObserver.getCurrentStatus() == ConnectivityObserver.Status.Available) {
                        _snackbarMessage.value = "Błąd pobierania kursów."
                    }
                    recalculateAll(save = false)
                }
                .collect { newRates ->
                    _mapaKursow.value = newRates
                    _uiState.update { it.copy(czyLadowanieKursow = false) }
                    recalculateAll(save = true)
                }
        }
    }

    fun recalculateAll(save: Boolean = true) {
        _uiState.update { state ->
            val updated = state.konteneryUI.map { container ->
                container.copy(result = calculator.calculateResult(
                    container.amount, 
                    container.from.symbol, 
                    container.to.symbol, 
                    _mapaKursow.value
                ))
            }
            if (save) {
                viewModelScope.launch {
                    repository.saveContainerData(ModelDanychKontenerow(updated.size, updated))
                }
            }
            state.copy(konteneryUI = updated, canDeleteAnyContainer = updated.size > 1)
        }
    }

    fun dodajKontener() {
        _uiState.update { state ->
            val from = state.dostepneWalutyDlaKontenerow.firstOrNull() ?: Waluta.EUR
            val to = state.dostepneWalutyDlaKontenerow.firstOrNull { it != from } ?: from
            val updated = state.konteneryUI + C(from = from, to = to, amount = "1", result = "")
            state.copy(konteneryUI = updated)
        }
        recalculateAll(save = true)
    }

    fun usunKontenerPoId(id: String) {
        if (!_uiState.value.canDeleteAnyContainer) {
            _snackbarMessage.value = "Nie można usunąć ostatniego przelicznika."
            return
        }
        _uiState.update { state ->
            val updated = state.konteneryUI.filterNot { it.id == id }
            state.copy(konteneryUI = updated)
        }
        recalculateAll(save = true)
    }

    fun zaktualizujKontenerIPrzelicz(id: String, updatedContainer: C) {
        _uiState.update { state ->
            val updated = state.konteneryUI.map { if (it.id == id) updatedContainer else it }
            state.copy(konteneryUI = updated)
        }
        recalculateAll(save = true)
    }

    fun odswiezDostepneWaluty() = viewModelScope.launch {
        val favorites = repository.loadFavoriteCurrencies().ifEmpty { 
            listOf(Waluta.EUR, Waluta.USD).also { repository.saveFavoriteCurrencies(it) }
        }
        
        if (_uiState.value.dostepneWalutyDlaKontenerow == favorites && _mapaKursow.value.isNotEmpty()) return@launch

        _uiState.update { state ->
            val updatedContainers = state.konteneryUI.map { container ->
                val from = if (favorites.contains(container.from)) container.from else favorites.firstOrNull() ?: Waluta.EUR
                var to = if (favorites.contains(container.to)) container.to else favorites.firstOrNull { it != from } ?: from
                
                if (favorites.size > 1 && from == to) {
                    to = favorites.firstOrNull { it != from } ?: to
                }
                container.copy(from = from, to = to)
            }
            state.copy(dostepneWalutyDlaKontenerow = favorites, konteneryUI = updatedContainers)
        }
        recalculateAll(save = true)
    }

    fun snackbarMessageShown() {
        _snackbarMessage.value = null
    }
}
