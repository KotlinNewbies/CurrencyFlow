package com.fluida.currencyflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluida.currencyflow.data.model.HistorycznyKurs
import com.fluida.currencyflow.data.repository.WalutyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun loadHistory(symbol: String, days: Int = 7) {
        _selectedDays.value = days
        viewModelScope.launch {
            _isLoading.value = true
            repository.pobierzHistorieKursu(symbol, days).collect {
                _historia.value = it
                _isLoading.value = false
            }
        }
    }
}
