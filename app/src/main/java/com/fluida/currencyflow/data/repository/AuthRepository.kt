package com.fluida.currencyflow.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class RegisterResponse(
    val rcSuccess: Boolean,
    val message: String,
    val api_key: String? = null,
    val device_uuid: String? = null,
    val is_premium: Boolean = false
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
        deviceId: String
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
                "id" to deviceId
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
}
