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
import com.fluida.currencyflow.util.UiText
import com.fluida.currencyflow.R
import com.fluida.currencyflow.data.TutorialManager
import com.fluida.currencyflow.ui.tutorial.TutorialStep
import com.fluida.currencyflow.ui.tutorial.TutorialUiState
import androidx.compose.ui.geometry.Rect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class HomeUiState(
    val konteneryUI: List<C> = emptyList(),
    val czyLadowanieKursow: Boolean = false,
    val dostepneWalutyDlaKontenerow: List<Waluta> = emptyList(),
    val isInitialized: Boolean = false,
    val isEditMode: Boolean = false
) {
    val canDeleteAnyContainer: Boolean get() = konteneryUI.size > 1
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RepositoryData,
    private val walutyRepository: WalutyRepository,
    private val userDataRepository: UserDataRepository,
    private val tutorialManager: TutorialManager,
    private val connectivityObserver: ConnectivityObserver,
    private val calculator: CurrencyCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _tutorialState = MutableStateFlow(TutorialUiState())
    val tutorialState: StateFlow<TutorialUiState> = _tutorialState.asStateFlow()

    private val _mapaKursow = MutableStateFlow<Map<String, Double>>(emptyMap())
    private val _snackbarMessage = MutableStateFlow<UiText?>(null)
    val snackbarMessage: StateFlow<UiText?> = _snackbarMessage.asStateFlow()

    private var aktualnyIdentyfikatorUzytkownika: String? = null
    private var wasOfflineForSnackbar = false
    private var saveJob: kotlinx.coroutines.Job? = null

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
            it.ifEmpty {
                val defaults = listOf(Waluta.EUR, Waluta.USD)
                repository.saveFavoriteCurrencies(defaults)
                defaults
            }
        }

        val savedContainers = repository.loadContainerData()?.kontenery ?: createDefaultContainers(favoriteCurrencies)

        _uiState.update { it.copy(
            dostepneWalutyDlaKontenerow = favoriteCurrencies,
            konteneryUI = savedContainers,
            isInitialized = true
        ) }

        // 3. Sprawdź czy pokazać samouczek
        viewModelScope.launch {
            tutorialManager.isTutorialSeen.collect { seen ->
                if (!seen && !_tutorialState.value.isVisible) {
                    startTutorial()
                }
            }
        }

        // 4. Sprawdź sieć i odśwież kursy jeśli to możliwe
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
                            _snackbarMessage.value = UiText.StringResource(R.string.msg_network_restored)
                            wasOfflineForSnackbar = false
                        }
                        if (_mapaKursow.value.isEmpty() && aktualnyIdentyfikatorUzytkownika != null) {
                            odswiezKursyWalut()
                        }
                    }
                    ConnectivityObserver.Status.Lost, ConnectivityObserver.Status.Unavailable -> {
                        _snackbarMessage.value = UiText.StringResource(R.string.msg_network_lost)
                        wasOfflineForSnackbar = true
                    }
                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    fun startTutorial() {
        _tutorialState.update { it.copy(
            currentStep = TutorialStep.INPUT_FIELD,
            isVisible = true,
            positions = emptyMap()
        ) }
    }

    fun nextTutorialStep() {
        val current = _tutorialState.value.currentStep
        val next = when (current) {
            TutorialStep.INPUT_FIELD -> TutorialStep.CURRENCY_SELECTION
            TutorialStep.CURRENCY_SELECTION -> TutorialStep.SWAP_DRAG
            TutorialStep.SWAP_DRAG -> TutorialStep.DELETE
            TutorialStep.DELETE -> TutorialStep.BOTTOM_ACTIONS
            TutorialStep.BOTTOM_ACTIONS -> null
            null -> null
        }

        if (next != null) {
            _tutorialState.update { it.copy(currentStep = next) }
        } else {
            dismissTutorial()
        }
    }

    fun dismissTutorial() {
        _tutorialState.update { it.copy(isVisible = false, currentStep = null) }
        tutorialManager.setTutorialSeen(true)
    }

    fun updateTutorialHighlight(rect: Rect, step: TutorialStep) {
        _tutorialState.update { state ->
            val currentList = state.positions[step] ?: emptyList()
            // Proste zabezpieczenie przed duplikatami (z tolerancją błędu zaokrągleń)
            if (currentList.any { it.top == rect.top && it.left == rect.left }) return@update state
            
            state.copy(positions = state.positions + (step to (currentList + rect)))
        }
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
                        _snackbarMessage.value = UiText.StringResource(R.string.msg_error_fetching_rates)
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
                scheduleSave(updated)
            }
            state.copy(konteneryUI = updated)
        }
    }

    private fun scheduleSave(kontenery: List<C>) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            // Krótkie opóźnienie, aby "zebrać" szybkie zmiany (np. wpisywanie tekstu lub szybkie usuwanie)
            delay(300.milliseconds)
            try {
                repository.saveContainerData(ModelDanychKontenerow(kontenery.size, kontenery))
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to save containers", e)
            }
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

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun moveContainer(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in _uiState.value.konteneryUI.indices || toIndex !in _uiState.value.konteneryUI.indices) return
        
        _uiState.update { state ->
            val updatedList = state.konteneryUI.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
            state.copy(konteneryUI = updatedList)
        }
    }

    fun moveContainerById(id: String, direction: Int) {
        val currentList = _uiState.value.konteneryUI
        val index = currentList.indexOfFirst { it.id == id }
        if (index == -1) return
        
        val newIndex = index + direction
        if (newIndex in currentList.indices) {
            moveContainer(index, newIndex)
        }
    }

    fun zapiszKolejnoscPoPrzesunieciu() {
        scheduleSave(_uiState.value.konteneryUI)
    }

    fun usunKontenerPoId(id: String) {
        if (!_uiState.value.canDeleteAnyContainer) {
            _snackbarMessage.value = UiText.StringResource(R.string.msg_error_cannot_delete_last_container)
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
                // Sprawdzamy, czy obecnie wybrane waluty w kontenerze są nadal w ulubionych
                val fromCurrentValid = favorites.contains(container.from)
                val toCurrentValid = favorites.contains(container.to)

                // Jeśli obie są nadal dostępne, nie zmieniamy ich, nawet jeśli są takie same (zamierzone przez użytkownika)
                if (fromCurrentValid && toCurrentValid) {
                    container
                } else {
                    // Jeśli któraś waluta zniknęła z ulubionych, wybieramy nową
                    val from = if (fromCurrentValid) container.from else favorites.firstOrNull() ?: Waluta.EUR
                    var to = if (toCurrentValid) container.to else favorites.firstOrNull { it != from } ?: from

                    // Zapobiegamy from == to TYLKO wtedy, gdy musieliśmy wybrać nową walutę (bo stara zniknęła)
                    if (favorites.size > 1 && from == to) {
                        to = favorites.firstOrNull { it != from } ?: to
                    }
                    container.copy(from = from, to = to)
                }
            }
            state.copy(dostepneWalutyDlaKontenerow = favorites, konteneryUI = updatedContainers)
        }
        recalculateAll(save = true)
    }

    fun snackbarMessageShown() {
        _snackbarMessage.value = null
    }
}
