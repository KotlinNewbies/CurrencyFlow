package com.example.currencyflow.data.repository

import android.content.Context
import android.util.Log
import com.example.currencyflow.data.model.ModelDanychKontenerow
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
    private val containersData = "pair_count.json"
    private val favoriteCurrenciesData = "selected_currencies.json"

    suspend fun loadContainerData(): ModelDanychKontenerow? {
        return withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, containersData)
            if (plik.exists()) {
                try {
                    val fileContent = plik.readText()
                    if (fileContent.isBlank()) {
                        Log.w("RepositoryData", "loadContainerData - File '$containersData' exists but is empty/blank.")
                        return@withContext null // Traktuj pusty plik jako brak danych
                    }
                    Log.d("RepositoryData", "loadContainerData - Attempting to deserialize from '$containersData'. Content length: ${fileContent.length}")
                    val modelDanych = Json.decodeFromString<ModelDanychKontenerow>(fileContent)
                    Log.i("RepositoryData", "loadContainerData - Successfully deserialized ${modelDanych.kontenery.size} containers from '$containersData'.")
                    modelDanych
                } catch (ioException: IOException) {
                    Log.e("RepositoryData", "loadContainerData - IOException while reading '$containersData'", ioException)
                    null
                } catch (serializationException: SerializationException) {
                    Log.e("RepositoryData", "loadContainerData - SerializationException while parsing '$containersData'", serializationException)
                    // Możesz tu zalogować fragment pliku, jeśli to pomoże w debugowaniu, ale ostrożnie z wrażliwymi danymi
                    // Log.d("RepositoryData", "Problematic JSON content (first 500 chars): ${plik.readText().take(500)}")
                    null
                } catch (e: Exception) {
                    Log.e("RepositoryData", "loadContainerData - Unexpected error while processing '$containersData'", e)
                    null
                }
            } else {
                Log.i("RepositoryData", "loadContainerData - File '$containersData' does not exist.")
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
            val plikDocelowy = File(context.filesDir, containersData)
            val plikTymczasowy = File(context.filesDir, "$containersData.tmp")

            try {
                Log.d("RepositoryData", "saveContainerData - Attempting to save ${data.kontenery.size} containers to '$containersData'.")
                val ciagJson = Json.encodeToString(data)
                plikTymczasowy.writeText(ciagJson) // 1. Zapisz do pliku tymczasowego

                // Usuń stary plik docelowy, jeśli istnieje, zanim przemianujesz nowy.
                // Na niektórych systemach rename może się nie powieść, jeśli plik docelowy istnieje.
                if (plikDocelowy.exists()) {
                    if (!plikDocelowy.delete()) {
                        Log.w("RepositoryData", "saveContainerData - Could not delete old target file: ${plikDocelowy.absolutePath}")
                        // Kontynuuj, rename może się mimo to udać lub obsłuż błąd inaczej
                    }
                }

                if (!plikTymczasowy.renameTo(plikDocelowy)) { // 2. Atomowo przemianuj
                    Log.e("RepositoryData", "saveContainerData - Failed to rename temporary file to target file. Attempting fallback copy.")
                    // Fallback: jeśli rename się nie uda, spróbuj skopiować i usunąć tymczasowy
                    // To nie jest już atomowe, ale lepsze niż nic.
                    try {
                        plikTymczasowy.copyTo(plikDocelowy, overwrite = true)
                        plikTymczasowy.delete() // Usuń tymczasowy po skopiowaniu
                        Log.i("RepositoryData", "saveContainerData - Successfully saved data using fallback copy for '$containersData'.")
                    } catch (copyException: Exception) {
                        Log.e("RepositoryData", "saveContainerData - Fallback copy also failed for '$containersData'. Data might not be saved.", copyException)
                        // Rozważ usunięcie pliku tymczasowego, aby nie został przy następnym odczycie, jeśli jego logika na to pozwala
                        plikTymczasowy.delete()
                        throw copyException // Rzuć dalej, aby ViewModel mógł potencjalnie zareagować
                    }
                } else {
                    Log.i("RepositoryData", "saveContainerData - Successfully saved data to '$containersData' via atomic rename.")
                }
            } catch (e: Exception) {
                Log.e("RepositoryData", "saveContainerData - Error saving data to '$containersData'", e)
                // Upewnij się, że plik tymczasowy jest usuwany w przypadku błędu, aby nie został jako "śmieć"
                if (plikTymczasowy.exists()) {
                    plikTymczasowy.delete()
                }
                throw e // Rzuć wyjątek dalej, aby ViewModel wiedział, że zapis się nie powiódł
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
            if (plik.exists()) {
                try {
                    val fileContent = plik.readText()
                    if (fileContent.isBlank()) {
                        Log.w("RepositoryData", "loadFavoriteCurrencies - File '$favoriteCurrenciesData' exists but is empty/blank.")
                        return@withContext emptyList() // Pusty plik traktujemy jako brak ulubionych
                    }
                    Log.d("RepositoryData", "loadFavoriteCurrencies - Attempting to deserialize from '$favoriteCurrenciesData'. Content length: ${fileContent.length}")
                    val waluty = Json.decodeFromString<List<Waluta>>(fileContent)
                    Log.i("RepositoryData", "loadFavoriteCurrencies - Successfully deserialized ${waluty.size} favorite currencies from '$favoriteCurrenciesData'.")
                    waluty
                } catch (ioException: IOException) {
                    Log.e("RepositoryData", "loadFavoriteCurrencies - IOException while reading '$favoriteCurrenciesData'", ioException)
                    emptyList()
                } catch (serializationException: SerializationException) {
                    Log.e("RepositoryData", "loadFavoriteCurrencies - SerializationException while parsing '$favoriteCurrenciesData'", serializationException)
                    // Log.d("RepositoryData", "Problematic JSON content for favorites (first 500 chars): ${plik.readText().take(500)}")
                    emptyList()
                } catch (e: Exception) {
                    Log.e("RepositoryData", "loadFavoriteCurrencies - Unexpected error while processing '$favoriteCurrenciesData'", e)
                    emptyList()
                }
            } else {
                Log.i("RepositoryData", "loadFavoriteCurrencies - File '$favoriteCurrenciesData' does not exist.")
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
            val plikDocelowy = File(context.filesDir, favoriteCurrenciesData)
            val plikTymczasowy = File(context.filesDir, "$favoriteCurrenciesData.tmp")

            try {
                Log.d("RepositoryData", "saveFavoriteCurrencies - Attempting to save ${wybraneWaluty.size} favorite currencies to '$favoriteCurrenciesData'.")
                val ciagJson = Json.encodeToString(wybraneWaluty)
                plikTymczasowy.writeText(ciagJson)

                if (plikDocelowy.exists()) {
                    if (!plikDocelowy.delete()) {
                        Log.w("RepositoryData", "saveFavoriteCurrencies - Could not delete old target file: ${plikDocelowy.absolutePath}")
                    }
                }

                if (!plikTymczasowy.renameTo(plikDocelowy)) {
                    Log.e("RepositoryData", "saveFavoriteCurrencies - Failed to rename temporary file to target file for favorites. Attempting fallback copy.")
                    try {
                        plikTymczasowy.copyTo(plikDocelowy, overwrite = true)
                        plikTymczasowy.delete()
                        Log.i("RepositoryData", "saveFavoriteCurrencies - Successfully saved favorite currencies using fallback copy for '$favoriteCurrenciesData'.")
                    } catch (copyException: Exception) {
                        Log.e("RepositoryData", "saveFavoriteCurrencies - Fallback copy also failed for favorites '$favoriteCurrenciesData'. Data might not be saved.", copyException)
                        plikTymczasowy.delete()
                        throw copyException
                    }
                } else {
                    Log.i("RepositoryData", "saveFavoriteCurrencies - Successfully saved favorite currencies to '$favoriteCurrenciesData' via atomic rename.")
                }
            } catch (e: Exception) {
                Log.e("RepositoryData", "saveFavoriteCurrencies - Error saving favorite currencies to '$favoriteCurrenciesData'", e)
                if (plikTymczasowy.exists()) {
                    plikTymczasowy.delete()
                }
                throw e
            }
        }
    }
}