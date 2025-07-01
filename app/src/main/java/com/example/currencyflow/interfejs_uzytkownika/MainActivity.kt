package com.example.currencyflow.interfejs_uzytkownika

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.currencyflow.CurrencyFlowApplication
import com.example.currencyflow.data.LanguageManager
import com.example.currencyflow.ui.navigation.Nawigacja
import com.example.currencyflow.viewmodel.FavoriteCurrenciesViewModel
import com.example.currencyflow.viewmodel.HomeViewModel
import com.example.currencyflow.interfejs_uzytkownika.theme.CurrencyFlowTheme
import com.example.currencyflow.ui.screens.GlownyEkran
import com.example.currencyflow.ui.screens.UlubioneWaluty
import com.example.currencyflow.ui.screens.SettingsScreen
import com.example.currencyflow.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG_LIFECYCLE = "MainActivityLifecycle"

    // Wstrzyknij LanguageManager, aby mieć do niego dostęp w onCreate/setContent
    @Inject
    lateinit var languageManager: LanguageManager

    override fun attachBaseContext(newBase: Context) {
        // Ta część pozostaje taka sama, ponieważ LanguageManager.getContextWithLocale
        // jest teraz zaprojektowany do obsługi stanu, gdy język może jeszcze nie być załadowany.
        Log.d(TAG_LIFECYCLE, "attachBaseContext CALLED - Original Locale: ${newBase.resources.configuration.locales[0]}")

        // Możesz użyć wstrzykniętej instancji, jeśli Hilt już ją tu dostarczy,
        // ale CurrencyFlowApplication.getLanguageManager jest bezpieczniejsze na tym etapie cyklu życia.
        val localLanguageManager = CurrencyFlowApplication.getLanguageManager(newBase)

        val contextWithLocale = localLanguageManager.getContextWithLocale(newBase)
        Log.d(TAG_LIFECYCLE, "attachBaseContext - Context Locale After Wrap: ${contextWithLocale.resources.configuration.locales[0]}")
        super.attachBaseContext(contextWithLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG_LIFECYCLE, "onCreate CALLED - Instance: $this, SavedState: $savedInstanceState, Current Locale: ${resources.configuration.locales[0]}")
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Obserwuj stan załadowania języka
            val isLanguageLoaded by languageManager.initialLanguageLoaded.collectAsState()
            // Możesz też obserwować aktualny tag dla celów debugowania lub logiki
            // val currentLanguageTag by languageManager.currentLanguageTagFlow.collectAsState()

            // Użyj LaunchedEffect do wykonania akcji po załadowaniu języka.
            // Klucz `isLanguageLoaded` zapewni, że efekt uruchomi się, gdy stan się zmieni.
            LaunchedEffect(isLanguageLoaded) {
                if (isLanguageLoaded) {
                    // Język został zainicjowany w LanguageManager.
                    // Wywołaj applyPersistedLanguageToSystem, aby upewnić się, że
                    // AppCompatDelegate.setApplicationLocales zostało wywołane z poprawnym (załadowanym) językiem.
                    // To jest szczególnie ważne przy pierwszym uruchomieniu po instalacji
                    // lub jeśli attachBaseContext użyło języka systemowego, bo dane nie były jeszcze dostępne.
                    Log.d(TAG_LIFECYCLE, "Language is loaded. Applying persisted language to system via MainActivity.")
                    languageManager.applyPersistedLanguageToSystem()

                    // UWAGA: `applyPersistedLanguageToSystem` wywołuje `setApplicationLocales`,
                    // co może (ale nie musi zawsze, zwłaszcza przy pierwszym ustawieniu) prowadzić do
                    // odtworzenia Activity. Jeśli Activity się odtworzy, ten LaunchedEffect
                    // uruchomi się ponownie. Jeśli `isLanguageLoaded` jest już true,
                    // ponowne wywołanie `applyPersistedLanguageToSystem` nie powinno szkodzić,
                    // ale miej to na uwadze. W dobrze skonfigurowanym systemie, po pierwszym
                    // poprawnym ustawieniu, kolejne wywołania z tym samym językiem nie powinny
                    // powodować kolejnych rekreacji.
                }
            }

            if (isLanguageLoaded) {
                // Jeśli język jest załadowany, wyświetl główne UI aplikacji
                CurrencyFlowTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = Nawigacja.Dom.route
                        ) {
                            composable(Nawigacja.Dom.route) {
                                val homeViewModel: HomeViewModel = hiltViewModel()
                                GlownyEkran(
                                    aktywnosc = this@MainActivity,
                                    kontrolerNawigacji = navController,
                                    homeViewModel = homeViewModel
                                )
                            }
                            composable(Nawigacja.UlubioneWaluty.route) {
                                val favoriteCurrenciesViewModel: FavoriteCurrenciesViewModel = hiltViewModel()
                                UlubioneWaluty(
                                    navController = navController,
                                    viewModel = favoriteCurrenciesViewModel
                                )
                            }
                            composable(Nawigacja.Ustawenia.route) {
                                // SettingsViewModel będzie teraz potrzebował LanguageManager
                                // i będzie obserwował currentLanguageTagFlow: StateFlow<String?>
                                val settingsViewModel: SettingsViewModel = hiltViewModel() // Zakładając, że masz ViewModel
                                SettingsScreen(
                                    navController = navController,
                                    viewModel = settingsViewModel // Przekaż ViewModel
                                )
                            }
                        }
                    }
                }
            } else {
                // Jeśli język nie jest jeszcze załadowany, wyświetl ekran ładowania
                // Możesz tu umieścić bardziej rozbudowany splash screen
                CurrencyFlowTheme { // Użyj motywu, aby ekran ładowania wyglądał spójnie
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Możesz tu dodać bardziej zaawansowany ekran ładowania
                            CircularProgressIndicator()
                            // Opcjonalnie tekst, pamiętaj o jego tłumaczeniu lub użyciu generycznego
                            // Text(text = stringResource(id = R.string.loading_language_settings))
                        }
                    }
                }
            }
        }
    }
}