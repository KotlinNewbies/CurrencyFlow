package com.example.currencyflow.dane.zarzadzanie_danymi

import android.content.Context
import android.util.Log
import com.example.currencyflow.dane.ModelDanychKontenerow
import com.example.currencyflow.dane.ModelDanychUzytkownika
import com.example.currencyflow.klasy.Waluta
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val TAG_REPOSITORY = "RepositoryData" // Dodaj TAG
class RepositoryData @Inject constructor(
    @ApplicationContext private val context: Context) {
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
                    Log.d(TAG_REPOSITORY, "Dane kontenerów wczytane pomyślnie.")
                    modelDanych
                } catch (ioException: IOException) {
                    Log.e(TAG_REPOSITORY, "Błąd I/O podczas wczytywania danych kontenerów", ioException)
                    null
                } catch (serializationException: SerializationException) {
                    Log.e(TAG_REPOSITORY, "Błąd serializacji podczas wczytywania danych kontenerów", serializationException)
                    null
                } catch (e: Exception) {
                    // Obsługa innych błędów
                    Log.e(TAG_REPOSITORY, "Nieznany błąd podczas wczytywania danych kontenerów", e)
                    null
                }
            } else {
                Log.d(TAG_REPOSITORY, "Plik z danymi kontenerów nie istnieje.")
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
            try {
                // Serializacja obiektu ModelDanychKontenerow do JSON
                // Użyj domyślnego serializatora, który jest generowany automatycznie
                // jeśli ModelDanychKontenerow jest oznaczony adnotacją @Serializable
                val ciagJson = Json.encodeToString(data)
                plik.writeText(ciagJson)
                Log.d(TAG_REPOSITORY, "Dane kontenerów zapisane pomyślnie.")
            } catch (ioException: IOException) {
                Log.e(TAG_REPOSITORY, "Błąd I/O podczas zapisywania danych kontenerów", ioException)
            } catch (serializationException: SerializationException) {
                Log.e(TAG_REPOSITORY, "Błąd serializacji podczas zapisywania danych kontenerów", serializationException)
            } catch (e: Exception) {
                Log.e(TAG_REPOSITORY, "Nieznany błąd podczas zapisywania danych kontenerów", e)
            }
        }
    }

    suspend fun loadUserData(): ModelDanychUzytkownika? {
        return withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, userData)
            if (plik.exists()) {
                try {
                    val ciagJson = plik.readText()
                    val modelDanych = Json.decodeFromString<ModelDanychUzytkownika>(ciagJson)
                    Log.d(TAG_REPOSITORY, "Dane użytkownika wczytane pomyślnie.")
                    modelDanych
                } catch (ioException: IOException) {
                    Log.e(TAG_REPOSITORY, "Błąd I/O podczas wczytywania danych użytkownika", ioException)
                    null
                } catch (serializationException: SerializationException) {
                    Log.e(TAG_REPOSITORY, "Błąd serializacji podczas wczytywania danych użytkownika", serializationException)
                    null
                } catch (e: Exception) {
                    Log.e(TAG_REPOSITORY, "Nieznany błąd podczas wczytywania danych użytkownika", e)
                    null
                }
            } else {
                Log.d(TAG_REPOSITORY, "Plik z danymi użytkownika nie istnieje.")
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

            try {
                val ciagJson = Json.encodeToString(data)
                plik.writeText(ciagJson)
                Log.d(TAG_REPOSITORY, "Dane użytkownika zapisane pomyślnie.")
            } catch (ioException: IOException) {
                Log.e(TAG_REPOSITORY, "Błąd I/O podczas zapisywania danych użytkownika", ioException)
            } catch (serializationException: SerializationException) {
                Log.e(TAG_REPOSITORY, "Błąd serializacji podczas zapisywania danych użytkownika", serializationException)
            }
            catch (e: Exception) {
                Log.e(TAG_REPOSITORY, "Nieznany błąd podczas zapisywania danych użytkownika", e)
            }
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
                    Log.d(TAG_REPOSITORY, "Ulubione waluty wczytane pomyślnie.")
                    waluty
                } catch (ioException: IOException) {
                    Log.e(TAG_REPOSITORY, "Błąd I/O podczas wczytywania ulubionych walut", ioException)
                    emptyList()
                } catch (serializationException: SerializationException) {
                    Log.e(TAG_REPOSITORY, "Błąd serializacji podczas wczytywania ulubionych walut", serializationException)
                    emptyList()
                } catch (e: Exception) {
                    Log.e(TAG_REPOSITORY, "Nieznany błąd podczas wczytywania ulubionych walut", e)
                    emptyList()
                }
            } else {
                Log.d(TAG_REPOSITORY, "Plik z ulubionymi walutami nie istnieje.")
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

            try {
                val ciagJson = Json.encodeToString(wybraneWaluty)
                plik.writeText(ciagJson)
                Log.d(TAG_REPOSITORY, "Ulubione waluty zapisane pomyślnie.")
            } catch (ioException: IOException) {
                Log.e(TAG_REPOSITORY, "Błąd I/O podczas zapisywania ulubionych walut", ioException)
            } catch (serializationException: SerializationException) {
                Log.e(TAG_REPOSITORY, "Błąd serializacji podczas zapisywania ulubionych walut", serializationException)
            } catch (e: Exception) {
                Log.e(TAG_REPOSITORY, "Nieznany błąd podczas zapisywania ulubionych walut", e)
            }
        }
    }
}