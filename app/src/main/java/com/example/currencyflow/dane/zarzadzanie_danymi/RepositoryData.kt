package com.example.currencyflow.dane.zarzadzanie_danymi

import android.content.Context
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
                    Json.decodeFromString<ModelDanychKontenerow>(ciagJson)
                } catch (ioException: IOException) {
                    ioException.printStackTrace()
                    null
                } catch (serializationException: SerializationException) {
                    serializationException.printStackTrace()
                    null
                } catch (e: Exception) {
                    // Obsługa innych błędów
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }
        }
    }

    suspend fun saveContainerData(data: ModelDanychKontenerow) {
        withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, containersData)
            try {
                val ciagJson = Json.encodeToString(ModelDanychKontenerow.serializer(), data)
                plik.writeText(ciagJson)
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            } catch (serializationException: SerializationException) {
                serializationException.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun loadUserData(): ModelDanychUzytkownika? {
        return withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, userData)
            if (plik.exists()) {
                try {
                    val ciagJson = plik.readText()
                    // Deserializacja przy użyciu Kotlinx Serialization
                    Json.decodeFromString<ModelDanychUzytkownika>(ciagJson)
                } catch (ioException: IOException) {
                    // Obsługa błędów wejścia/wyjścia (np. brak dostępu, uszkodzony plik)
                    ioException.printStackTrace()
                    null
                } catch (serializationException: SerializationException) {
                    // Obsługa błędów deserializacji (np. niezgodna struktura danych)
                    serializationException.printStackTrace()
                    null
                } catch (e: Exception) {
                    // Obsługa innych nieznanych błędów
                    e.printStackTrace()
                    null
                }
            } else {
                // Plik nie istnieje - zwróć null
                null
            }
        }
    }

    suspend fun saveUserData(data: ModelDanychUzytkownika) {
        withContext(Dispatchers.IO) {
            val plik = File(context.filesDir, userData)

            try {
                // Używamy prostszej metody writeText()
                val ciagJson = Json.encodeToString(data) // serializacja (używa danych przekazanych jako argument)
                plik.writeText(ciagJson)
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            } catch (serializationException: SerializationException) {
                serializationException.printStackTrace()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun loadFavoriteCurrencies(): List<Waluta> {
        return withContext(Dispatchers.IO) { // Wykonaj operację w wątku IO
            val plik = File(context.filesDir, favoriteCurrenciesData) // Użyj nazwy pliku dla ulubionych walut

            return@withContext if (plik.exists()) { // Zwróć wartość z tego bloku withContext
                try {
                    val ciagJson = plik.readText()
                    // Deserializacja Listy<Waluta>
                    Json.decodeFromString<List<Waluta>>(ciagJson)
                } catch (ioException: IOException) {
                    ioException.printStackTrace()
                    emptyList() // Zwróć pustą listę w przypadku błędu I/O
                } catch (serializationException: SerializationException) {
                    serializationException.printStackTrace()
                    emptyList() // Zwróć pustą listę w przypadku błędu deserializacji
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList() // Zwróć pustą listę w przypadku innych błędów
                }
            } else {
                // Plik nie istnieje - zwróć pustą listę
                emptyList()
            }
        }
    }

    suspend fun saveFavoriteCurrencies(wybraneWaluty: List<Waluta>) { // Funkcja zawieszenia
        withContext(Dispatchers.IO) { // Wykonanie w wątku IO
            val plik = File(context.filesDir, favoriteCurrenciesData) // Utworzenie obiektu File

            try {
                // Serializacja listy wybranych walut do JSON
                val ciagJson = Json.encodeToString(wybraneWaluty)

                // Zapis do pliku przy użyciu writeText()
                plik.writeText(ciagJson)
            } catch (ioException: IOException) { // Bardziej szczegółowa obsługa błędów
                ioException.printStackTrace()
            } catch (serializationException: SerializationException) {
                serializationException.printStackTrace()
            } catch (e: Exception) {
                // Obsługa innych błędów
                e.printStackTrace()
            }
        }
    }
}