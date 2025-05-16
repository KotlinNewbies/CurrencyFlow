package com.example.currencyflow.data.repository

import com.example.currencyflow.data.model.ModelDanychUzytkownika // Upewnij się, że import jest poprawny

interface UserDataRepository {
    suspend fun getUserDataModel(): ModelDanychUzytkownika
    // Możesz dodać metodę do zapisu, jeśli inne części aplikacji będą modyfikować ModelDanychUzytkownika
    // suspend fun saveUserDataModel(model: ModelDanychUzytkownika)
}