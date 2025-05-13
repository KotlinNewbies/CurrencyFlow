package com.example.currencyflow.dane.zarzadzanie_danymi

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.currencyflow.dane.WybraneWalutyViewModel

class WybraneWalutyViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WybraneWalutyViewModel::class.java)) {
            val repository = RepositoryData(context)
            return WybraneWalutyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

