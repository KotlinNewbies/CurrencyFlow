package com.fluida.currencyflow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluida.currencyflow.data.model.HistorycznyKurs
import com.fluida.currencyflow.data.repository.WalutyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyHistoryViewModel @Inject constructor(
    private val repository: WalutyRepository
) : ViewModel() {

    private val _historia = MutableStateFlow<List<HistorycznyKurs>>(emptyList())
    val historia: StateFlow<List<HistorycznyKurs>> = _historia.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedDays = MutableStateFlow(7)
    val selectedDays: StateFlow<Int> = _selectedDays.asStateFlow()

    fun loadHistory(from: String, to: String, days: Int = 7) {
        _selectedDays.value = days
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("HistoryVM", "Loading history for $from -> $to ($days days)")

            val flowFrom = repository.pobierzHistorieKursu(from, days)
            val flowTo = repository.pobierzHistorieKursu(to, days)

            // Używamy combine zamiast zip, aby mieć większą kontrolę
            combine(flowFrom, flowTo) { listFrom, listTo ->
                Log.d("HistoryVM", "Data received: $from(${listFrom.size} pts), $to(${listTo.size} pts)")
                
                when {
                    from == "EUR" -> listTo
                    to == "EUR" -> listFrom.map { it.copy(value = 1.0 / it.value) }
                    listFrom.isEmpty() || listTo.isEmpty() -> {
                        Log.w("HistoryVM", "One of the lists is empty, cannot calculate cross-rate")
                        emptyList()
                    }
                    else -> {
                        // Obliczanie kursu krzyżowego dla każdego punktu
                        listFrom.zip(listTo) { pFrom, pTo ->
                            HistorycznyKurs(
                                value = pTo.value / pFrom.value,
                                date = pTo.date
                            )
                        }
                    }
                }
            }.collect {
                _historia.value = it
                _isLoading.value = false
            }
        }
    }
}
