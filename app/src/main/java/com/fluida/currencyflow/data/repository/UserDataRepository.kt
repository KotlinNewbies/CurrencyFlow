package com.fluida.currencyflow.data.repository

import com.fluida.currencyflow.data.model.ModelDanychUzytkownika
import kotlinx.coroutines.flow.StateFlow

interface UserDataRepository {
    val userDataFlow: StateFlow<ModelDanychUzytkownika>
    suspend fun getUserDataModel(): ModelDanychUzytkownika
    suspend fun updateUuid(newUuid: String)
}