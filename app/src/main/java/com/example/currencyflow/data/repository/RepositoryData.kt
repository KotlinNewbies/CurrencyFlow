package com.example.currencyflow.data.repository

import android.content.Context
import com.example.currencyflow.data.model.ModelDanychKontenerow
import com.example.currencyflow.data.model.ModelDanychUzytkownika
import com.example.currencyflow.data.model.Waluta
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import javax.inject.Inject

class RepositoryData @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val containersData = "containersData.json"
    private val userData = "userData.json"
    private val favoriteCurrenciesData = "favoriteCurrencies.json"

    suspend fun loadContainerData(): ModelDanychKontenerow? {
        return withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, containersData)
            if (plik.exists()) {
                try {
                    val ciagJson = plik.readText()
                    // Deserializacja przy użyciu Kotlinx Serialization
                    val modelDanych = Json.decodeFromString<ModelDanychKontenerow>(ciagJson)
                    modelDanych
                } catch (ioException: IOException) {
                    null
                } catch (serializationException: SerializationException) {
                    null
                } catch (e: Exception) {
                    // Obsługa innych błędów
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Zapisuje dane kontenerów do pliku.
     *
     * @param data Obiekt ModelDanychKontenerow do zapisania.
     */
    suspend fun saveContainerData(data: ModelDanychKontenerow) {
        withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, containersData)
            // Serializacja obiektu ModelDanychKontenerow do JSON
            // Użyj domyślnego serializatora, który jest generowany automatycznie
            // jeśli ModelDanychKontenerow jest oznaczony adnotacją @Serializable
            val ciagJson = Json.encodeToString(data)
            plik.writeText(ciagJson)
        }
    }

    suspend fun loadUserData(): ModelDanychUzytkownika? {
        return withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, userData)
            if (plik.exists()) {
                try {
                    val ciagJson = plik.readText()
                    val modelDanych = Json.decodeFromString<ModelDanychUzytkownika>(ciagJson)
                    modelDanych
                } catch (ioException: IOException) {
                    null
                } catch (serializationException: SerializationException) {
                    null
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Zapisuje dane użytkownika do pliku.
     *
     * @param data Obiekt ModelDanychUzytkownika do zapisania.
     */
    suspend fun saveUserData(data: ModelDanychUzytkownika) {
        withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, userData)
            val ciagJson = Json.encodeToString(data)
            plik.writeText(ciagJson)
        }
    }

    /**
     * Wczytuje listę ulubionych walut z pliku.
     * Zwraca Listę<Waluta> lub pustą listę, jeśli plik nie istnieje lub wystąpi błąd.
     */
    suspend fun loadFavoriteCurrencies(): List<Waluta> {
        return withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, favoriteCurrenciesData)

            return@withContext if (plik.exists()) {
                try {
                    val ciagJson = plik.readText()
                    val waluty = Json.decodeFromString<List<Waluta>>(ciagJson)
                    waluty
                } catch (ioException: IOException) {
                    emptyList()
                } catch (serializationException: SerializationException) {
                    emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    /**
     * Zapisuje listę ulubionych walut do pliku.
     *
     * @param wybraneWaluty Lista Waluta do zapisania.
     */
    suspend fun saveFavoriteCurrencies(wybraneWaluty: List<Waluta>) {
        withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, favoriteCurrenciesData)
            val ciagJson = Json.encodeToString(wybraneWaluty)
            plik.writeText(ciagJson)
        }
    }
}