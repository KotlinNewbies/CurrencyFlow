package com.fluida.currencyflow.data.repository

import com.fluida.currencyflow.data.model.BackupData
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class RegisterResponse(
    val rcSuccess: Boolean,
    val message: String,
    val first_name: String? = null,
    val api_key: String? = null,
    val device_uuid: String? = null,
    val is_premium: Boolean = false
)

@Serializable
data class BackupResponse(
    val rcSuccess: Boolean,
    val message: String? = null,
    val backup_data: BackupData? = null
)

@Singleton
class AuthRepository @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://android.propages.pl"

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        deviceId: String,
        isPremium: Boolean = false
    ): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl?option=register")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val body = json.encodeToString(mapOf(
                "first_name" to firstName,
                "last_name" to lastName,
                "email" to email,
                "phone" to phone,
                "password" to password,
                "id" to deviceId,
                "is_premium" to isPremium.toString()
            ))

            conn.outputStream.use { it.write(body.toByteArray()) }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<RegisterResponse>(responseBody)
                Result.success(response)
            } else {
                Result.failure(Exception("HTTP error: ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration failed", e)
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl?option=login")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val body = json.encodeToString(mapOf(
                "email" to email,
                "password" to password
            ))

            conn.outputStream.use { it.write(body.toByteArray()) }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<RegisterResponse>(responseBody)
                Result.success(response)
            } else {
                Result.failure(Exception("HTTP error: ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed", e)
            Result.failure(e)
        }
    }

    suspend fun saveBackup(apiKey: String, backupData: BackupData): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl?option=save_backup")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            // PHP oczekuje "api_key" i "backup_data" jako obiekt JSON
            val body = json.encodeToString(buildJsonObject {
                put("api_key", apiKey)
                put("backup_data", json.encodeToJsonElement(BackupData.serializer(), backupData))
            })

            conn.outputStream.use { it.write(body.toByteArray()) }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<RegisterResponse>(responseBody)
                Result.success(response.rcSuccess)
            } else {
                Result.failure(Exception("HTTP error: ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Save backup failed", e)
            Result.failure(e)
        }
    }

    suspend fun getBackup(apiKey: String): Result<BackupData?> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl?option=get_backup")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val body = json.encodeToString(mapOf("api_key" to apiKey))
            conn.outputStream.use { it.write(body.toByteArray()) }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<BackupResponse>(responseBody)
                if (response.rcSuccess) {
                    Result.success(response.backup_data)
                } else {
                    Result.success(null) // Brak backupu to nie błąd
                }
            } else {
                Result.failure(Exception("HTTP error: ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Get backup failed", e)
            Result.failure(e)
        }
    }
}
