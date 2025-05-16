package com.example.currencyflow.dane.zarzadzanie_danymi

import android.content.Context
import com.example.currencyflow.dane.ModelDanychUzytkownika // Upewnij się, że import jest poprawny
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface UserDataRepository {
    suspend fun getUserDataModel(): ModelDanychUzytkownika
    // Możesz dodać metodę do zapisu, jeśli inne części aplikacji będą modyfikować ModelDanychUzytkownika
    // suspend fun saveUserDataModel(model: ModelDanychUzytkownika)
}

@Singleton
class FileUserDataRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : UserDataRepository {

    private val fileName = "dane_uzytkownika.json"
    private val userFile: File by lazy { File(context.filesDir, fileName) }

    private var cachedModel: ModelDanychUzytkownika? = null

    override suspend fun getUserDataModel(): ModelDanychUzytkownika {
        // Zwróć z cache, jeśli już załadowano, aby uniknąć wielokrotnego odczytu pliku
        // (Przydatne, jeśli wiele miejsc w krótkim czasie by o to pytało.
        // Dla HomeViewModel inicjalizowanego raz, może nie być krytyczne, ale to dobra praktyka)
        cachedModel?.let { return it }

        return if (userFile.exists()) {
            try {
                val jsonString = userFile.readText()
                val model = Json.decodeFromString<ModelDanychUzytkownika>(jsonString)
                cachedModel = model // Zapisz do cache
                // Log.d("FileUserDataRepo", "Odczytano ModelDanychUzytkownika z pliku: $model")
                model
            } catch (e: Exception) {
                // Log.e("FileUserDataRepo", "Błąd podczas deserializacji ModelDanychUzytkownika", e)
                // Jeśli plik istnieje, ale jest uszkodzony, utwórz nowy model domyślny
                createNewDefaultModelAndSave()
            }
        } else {
            // Log.d("FileUserDataRepo", "Plik dane_uzytkownika.json nie istnieje. Tworzenie nowego.")
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

// Opcjonalna metoda, jeśli będziesz chciał zapisywać zmiany w ModelDanychUzytkownika z innych miejsc
// override suspend fun saveUserDataModel(model: ModelDanychUzytkownika) {
//     try {
//         val jsonString = Json.encodeToString(model)
//         userFile.writeText(jsonString)
//         cachedModel = model
//         // Log.d("FileUserDataRepo", "Zapisano ModelDanychUzytkownika: $model")
//     } catch (e: Exception) {
//         // Log.e("FileUserDataRepo", "Błąd podczas zapisu ModelDanychUzytkownika", e)
//     }
// }