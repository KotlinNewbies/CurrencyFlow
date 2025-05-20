package com.example.currencyflow.data.repository

import android.content.Context
import com.example.currencyflow.data.model.ModelDanychUzytkownika
import dagger.hilt.android.qualifiers.ApplicationContext
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

    private var cachedModel: ModelDanychUzytkownika? = null

    override suspend fun getUserDataModel(): ModelDanychUzytkownika {
        cachedModel?.let { return it }

        return if (userFile.exists()) {
            try {
                val jsonString = userFile.readText()
                val model = Json.decodeFromString<ModelDanychUzytkownika>(jsonString)
                cachedModel = model // Zapisz do cache
                model
            } catch (e: Exception) {
                createNewDefaultModelAndSave()
            }
        } else {
            createNewDefaultModelAndSave()
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
