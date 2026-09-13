package com.fluida.currencyflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluida.currencyflow.data.model.HistorycznyKurs
import com.fluida.currencyflow.data.repository.WalutyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
            
            val flowFrom = repository.pobierzHistorieKursu(from, days)
            val flowTo = repository.pobierzHistorieKursu(to, days)

            combine(flowFrom, flowTo) { listFrom, listTo ->
                calculateCrossHistory(from, to, listFrom, listTo)
            }.collect {
                _historia.value = it
                _isLoading.value = false
            }
        }
    }

    private fun calculateCrossHistory(
        from: String,
        to: String,
        listFrom: List<HistorycznyKurs>,
        listTo: List<HistorycznyKurs>
    ): List<HistorycznyKurs> {
        if (from == "EUR") return listTo
        if (to == "EUR") return listFrom.map { it.copy(value = 1.0 / it.value) }

        // Cross rate: rate(from->to) = EUR_to / EUR_from
        // Zakładamy, że punkty czasowe się pokrywają, bo są zapisywane w jednej sesji.
        // Jeśli listy mają różne długości, zipujemy do krótszej.
        return listFrom.zip(listTo) { pointFrom, pointTo ->
            HistorycznyKurs(
                value = pointTo.value / pointFrom.value,
                date = pointTo.date
            )
        }
    }
}
