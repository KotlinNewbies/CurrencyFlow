package com.example.currencyflow.util

sealed class SnackbarEvent {
    data class Message(val text: String) : SnackbarEvent()
    data object NoInternet : SnackbarEvent()
    data object InternetRestored : SnackbarEvent()
    // Możesz dodać inne typy eventów, jeśli potrzebujesz
}