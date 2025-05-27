//package com.example.currencyflow.dane
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class WalutyViewModel @Inject constructor() : ViewModel() {
//
//    // StateFlow do przechowywania mapy walut
//    private val _mapaWalut = MutableStateFlow<Map<String, Double>>(emptyMap())
//    val mapaWalut: StateFlow<Map<String, Double>> get() = _mapaWalut
//
//    // Funkcja do aktualizacji mapy walut
//    fun zaktualizujMapeWalut(noweMnozniki: Map<String, Double>) {
//        viewModelScope.launch {
//            _mapaWalut.value = noweMnozniki
//        }
//    }
//}