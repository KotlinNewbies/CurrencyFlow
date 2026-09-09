package com.fluida.currencyflow.data.repository

import android.content.Context
import android.util.Log
import com.fluida.currencyflow.data.model.ModelDanychUzytkownika
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
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

    private val _userDataFlow = MutableStateFlow<ModelDanychUzytkownika?>(null)
    override val userDataFlow: StateFlow<ModelDanychUzytkownika> = _userDataFlow
        .filterNotNull()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            started = SharingStarted.Eagerly,
            initialValue = ModelDanychUzytkownika(id = "", app = "", v = "") // Tymczasowy pusty model
        )

    init {
        // Inicjalizacja poza konstruktorem, aby nie blokować wątku głównego przy wstrzykiwaniu
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            getUserDataModel() 
        }
    }

    private var cachedModel: ModelDanychUzytkownika? = null

    override suspend fun getUserDataModel(): ModelDanychUzytkownika {
        cachedModel?.let { if (it.id.isNotEmpty()) return it }
        return withContext(Dispatchers.IO) {
            val model = loadUserDataSync()
            cachedModel = model
            _userDataFlow.value = model
            model
        }
    }

    private fun loadUserDataSync(): ModelDanychUzytkownika {
        Log.d("FileDataRepository", "loadUserDataSync: Thread ${Thread.currentThread().name}")
        return if (userFile.exists()) {
            try {
                val jsonString = userFile.readText()
                Json.decodeFromString<ModelDanychUzytkownika>(jsonString)
            } catch (e: Exception) {
                createNewDefaultModelAndSaveSync()
            }
        } else {
            createNewDefaultModelAndSaveSync()
        }
    }

    override suspend fun updateUuid(newUuid: String) {
        withContext(Dispatchers.IO) {
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
    }

    private fun createNewDefaultModelAndSaveSync(): ModelDanychUzytkownika {
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
        } catch (e: Exception) {
            // Log error
        }
        return newModel
    }
}
