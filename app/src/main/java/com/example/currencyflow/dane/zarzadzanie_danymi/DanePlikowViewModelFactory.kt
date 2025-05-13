package com.example.currencyflow.dane.zarzadzanie_danymi

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Fabryka do tworzenia instancji DanePlikowViewModel
class DanePlikowViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    // Metoda create jest wywoływana przez system Android, aby utworzyć instancję ViewModelu
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Sprawdź, czy żądany ViewModel to DanePlikowViewModel
        if (modelClass.isAssignableFrom(DanePlikowViewModel::class.java)) {
            // Utwórz instancję RepositoryData, przekazując wymagany Context
            val repositoryData = RepositoryData(context)
            // Utwórz instancję DanePlikowViewModel, przekazując RepositoryData
            @Suppress("UNCHECKED_CAST") // Rzutowanie jest bezpieczne, ponieważ sprawdziliśmy isAssignableFrom
            return DanePlikowViewModel(repositoryData) as T
        }
        // Jeśli żądany ViewModel nie jest DanePlikowViewModel, zgłoś błąd
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}