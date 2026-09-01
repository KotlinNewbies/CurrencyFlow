package com.fluida.currencyflow.data.repository

import android.content.Context
import com.fluida.currencyflow.data.model.ModelDanychUzytkownika
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileUserDataRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : UserDataRepository {

    private val fileName = "user_data.json"
    private val userFile: File by lazy { File(context.filesDir, fileName) }

    private val _userDataFlow = MutableStateFlow<ModelDanychUzytkownika>(loadUserDataSync())
    override val userDataFlow: StateFlow<ModelDanychUzytkownika> = _userDataFlow.asStateFlow()

    private var cachedModel: ModelDanychUzytkownika? = null

    override suspend fun getUserDataModel(): ModelDanychUzytkownika {
        cachedModel?.let { return it }
        val model = loadUserDataSync()
        cachedModel = model
        _userDataFlow.value = model
        return model
    }

    private fun loadUserDataSync(): ModelDanychUzytkownika {
        return if (userFile.exists()) {
            try {
                val jsonString = userFile.readText()
                Json.decodeFromString<ModelDanychUzytkownika>(jsonString)
            } catch (e: Exception) {
                createNewDefaultModelAndSave()
            }
        } else {
            createNewDefaultModelAndSave()
        }
    }

    override suspend fun updateUuid(newUuid: String) {
        val currentModel = getUserDataModel()
        if (currentModel.id != newUuid) {
            val updatedModel = currentModel.copy(id = newUuid)
            try {
                val jsonString = Json.encodeToString(updatedModel)
                userFile.writeText(jsonString)
                cachedModel = updatedModel
                _userDataFlow.value = updatedModel
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private fun createNewDefaultModelAndSave(): ModelDanychUzytkownika {
        val newUUID = UUID.randomUUID().toString()
        val newModel = ModelDanychUzytkownika(
            id = newUUID,
            app = "curConv", // Twoje wartości domyślne
            v = "1.0.0"     // Twoje wartości domyślne
        )
        try {
            val jsonString = Json.encodeToString(newModel)
            userFile.writeText(jsonString)
            cachedModel = newModel // Zapisz do cache
            // Log.d("FileUserDataRepo", "Zapisano nowy ModelDanychUzytkownika do pliku: $newModel")
        } catch (e: Exception) {
            // Log.e("FileUserDataRepo", "Błąd podczas zapisu nowego ModelDanychUzytkownika", e)
            // Zwróć model nawet jeśli zapis się nie udał, aby aplikacja mogła działać w pamięci
        }
        return newModel
    }
}
