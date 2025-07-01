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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val ADMOB_TAG_MAIN = "AdMobMainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG_LIFECYCLE = "MainActivityLifecycle"

    @Inject
    lateinit var languageManager: LanguageManager

    override fun attachBaseContext(newBase: Context) {
        Log.d(TAG_LIFECYCLE, "attachBaseContext CALLED - Original Locale: ${newBase.resources.configuration.locales[0]}")

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
            var isAdSdkInitialized by remember { mutableStateOf(false) }
            val currentContext = LocalContext.current // Pobierz kontekst tutaj, aby użyć w LaunchedEffect
            LaunchedEffect(isLanguageLoaded) {
                if (isLanguageLoaded) {
                    Log.d(TAG_LIFECYCLE, "Language is loaded. Applying persisted language to system via MainActivity.")
                    languageManager.applyPersistedLanguageToSystem()

                    Log.d(ADMOB_TAG_MAIN, "Language loaded, attempting to initialize AdMob SDK after a delay.")
                    delay(500L) // Opóźnienie 500ms (dostosuj w razie potrzeby)

                    CurrencyFlowApplication.initializeMobileAdsSdk(currentContext.applicationContext) {
                        Log.d(ADMOB_TAG_MAIN, "AdMob SDK initialized callback in MainActivity.")
                        isAdSdkInitialized = true
                    }
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
                CurrencyFlowTheme { // Użyj motywu, aby ekran ładowania wyglądał spójnie
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}