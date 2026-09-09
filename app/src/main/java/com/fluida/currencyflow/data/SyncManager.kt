package com.fluida.currencyflow.data

import android.util.Log
import com.fluida.currencyflow.data.model.BackupData
import com.fluida.currencyflow.data.model.ModelDanychKontenerow
import com.fluida.currencyflow.data.repository.AuthRepository
import com.fluida.currencyflow.data.repository.RepositoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager,
    private val repositoryData: RepositoryData,
    private val settingsManager: SettingsManager,
    private val languageManager: LanguageManager
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isSyncing = AtomicBoolean(false)

    private val _backupAppliedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val backupAppliedEvent: SharedFlow<Unit> = _backupAppliedEvent.asSharedFlow()

    /**
     * Wysyła aktualne ustawienia na serwer, jeśli użytkownik jest zalogowany i ma Premium.
     */
    fun uploadBackup() {
        if (isSyncing.get()) {
            Log.d("SyncManager", "Upload skipped: sync in progress")
            return
        }

        val apiKey = authManager.apiKey.value
        val isPremium = authManager.isPremium.value

        if (apiKey.isNullOrBlank() || !isPremium) {
            return
        }
// ... reszta funkcji ...

        scope.launch {
            try {
                val favorites = repositoryData.loadFavoriteCurrencies()
                val containers = repositoryData.loadContainerData()?.kontenery ?: emptyList()
                val lang = languageManager.currentLanguageTagFlow.value ?: ""
                val decimals = settingsManager.decimalPlaces.value

                val backup = BackupData(
                    favoriteCurrencies = favorites,
                    containers = containers,
                    languageTag = lang,
                    decimalPlaces = decimals
                )

                val result = authRepository.saveBackup(apiKey, backup)
                if (result.isSuccess) {
                    Log.d("SyncManager", "Backup uploaded successfully")
                } else {
                    Log.e("SyncManager", "Backup upload failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("SyncManager", "Error during backup upload", e)
            }
        }
    }

    /**
     * Pobiera kopię zapasową z serwera i aplikuje ją lokalnie.
     * Zwraca true, jeśli backup został znaleziony i zaaplikowany.
     */
    suspend fun downloadAndApplyBackup(explicitApiKey: String? = null): Boolean {
        val apiKey = explicitApiKey ?: authManager.apiKey.value ?: return false
        
        isSyncing.set(true)
        try {
            val result = authRepository.getBackup(apiKey)
            return result.fold(
                onSuccess = { backup ->
                    if (backup != null) {
                        applyBackup(backup)
                        true
                    } else {
                        Log.d("SyncManager", "No backup found on server")
                        false
                    }
                },
                onFailure = {
                    Log.e("SyncManager", "Failed to download backup", it)
                    false
                }
            )
        } finally {
            isSyncing.set(false)
        }
    }

    private suspend fun applyBackup(backup: BackupData) {
        Log.d("SyncManager", "Applying backup from server...")
        
        // 1. Waluty
        repositoryData.saveFavoriteCurrencies(backup.favoriteCurrencies)
        
        // 2. Kontenery
        repositoryData.saveContainerData(ModelDanychKontenerow(backup.containers.size, backup.containers))
        
        // 3. Ustawienia (Decimal places)
        settingsManager.setDecimalPlaces(backup.decimalPlaces)
        
        // 4. Język
        languageManager.setApplicationLanguage(backup.languageTag)
        
        // Aplikuj język do systemu, aby UI się odświeżyło
        languageManager.applyPersistedLanguageToSystem()
        
        // Powiadom ViewModele o konieczności przeładowania danych z plików
        _backupAppliedEvent.emit(Unit)
        
        Log.d("SyncManager", "Backup applied successfully")
    }
}
