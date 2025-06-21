package com.example.currencyflow.interfejs_uzytkownika

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.currencyflow.CurrencyFlowApplication
import com.example.currencyflow.ui.navigation.Nawigacja
import com.example.currencyflow.viewmodel.FavoriteCurrenciesViewModel
import com.example.currencyflow.viewmodel.HomeViewModel
import com.example.currencyflow.interfejs_uzytkownika.theme.CurrencyFlowTheme
import com.example.currencyflow.ui.screens.GlownyEkran
import com.example.currencyflow.ui.screens.UlubioneWaluty
import com.example.currencyflow.ui.screens.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG_LIFECYCLE = "MainActivityLifecycle"

    override fun attachBaseContext(newBase: Context) {
        // Ten log jest bardzo przydatny do debugowania problemów z językiem na starcie
        Log.d(TAG_LIFECYCLE, "attachBaseContext CALLED - Original Locale: ${newBase.resources.configuration.locales[0]}")

        val localLanguageManager = CurrencyFlowApplication.getLanguageManager(newBase)
        // LanguageManager powinien być już zainicjalizowany przez Hilt w Application.onCreate()
        // i jego wewnętrzny stan języka (currentPersistedLanguageTag) ustawiony.

        val contextWithLocale = localLanguageManager.getContextWithLocale(newBase)
        // Ten log również jest bardzo przydatny
        Log.d(TAG_LIFECYCLE, "attachBaseContext - Context Locale After Wrap: ${contextWithLocale.resources.configuration.locales[0]}")
        super.attachBaseContext(contextWithLocale) // Tells ComponentActivity to use contextWithLocale instead of newBase
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Log z `Current Locale` na początku onCreate jest bardzo cenny.
        Log.d(TAG_LIFECYCLE, "onCreate CALLED - Instance: $this, SavedState: $savedInstanceState, Current Locale: ${resources.configuration.locales[0]}")
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // Ważne dla UI
        setContent {
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
                            // Hilt dostarczy instancję ViewModelu powiązaną z composable destynacją
                            val homeViewModel: HomeViewModel =
                                hiltViewModel()
                            GlownyEkran(
                                aktywnosc = this@MainActivity,
                                kontrolerNawigacji = navController,
                                homeViewModel = homeViewModel
                            )
                        }
                        composable(Nawigacja.UlubioneWaluty.route) {
                            // Hilt dostarczy nową instancję ViewModelu dla ekranu ulubionych
                            val favoriteCurrenciesViewModel: FavoriteCurrenciesViewModel =
                                hiltViewModel()
                            UlubioneWaluty(
                                navController = navController,
                                viewModel = favoriteCurrenciesViewModel
                            )
                        }
                        composable(Nawigacja.Ustawenia.route) {
                            SettingsScreen(
                                navController = navController,
                            )
                        }
                    }
                }
            }
        }
    }
}