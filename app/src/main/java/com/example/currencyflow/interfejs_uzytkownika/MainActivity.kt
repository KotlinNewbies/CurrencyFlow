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
import androidx.hilt.navigation.compose.hiltViewModel // Używane
import androidx.navigation.compose.NavHost // Używane
import androidx.navigation.compose.composable // Używane
import androidx.navigation.compose.rememberNavController // Używane
import com.example.currencyflow.CurrencyFlowApplication // Używane
import com.example.currencyflow.ui.navigation.Nawigacja // Używane
import com.example.currencyflow.viewmodel.FavoriteCurrenciesViewModel // Używane
import com.example.currencyflow.viewmodel.HomeViewModel // Używane
import com.example.currencyflow.interfejs_uzytkownika.theme.CurrencyFlowTheme // Używane
import com.example.currencyflow.ui.screens.GlownyEkran // Używane
import com.example.currencyflow.ui.screens.UlubioneWaluty // Używane
import com.example.currencyflow.ui.screens.SettingsScreen // Używane
import dagger.hilt.android.AndroidEntryPoint // Używane

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG_LIFECYCLE = "MainActivityLifecycle" // Używane przez logi

    override fun attachBaseContext(newBase: Context) {
        // Ten log jest bardzo przydatny do debugowania problemów z językiem na starcie
        Log.d(TAG_LIFECYCLE, "attachBaseContext CALLED - Original Locale: ${newBase.resources.configuration.locales[0]}")

        val localLanguageManager = CurrencyFlowApplication.getLanguageManager(newBase)
        // Komentarz poniżej jest dobry, wyjaśnia działanie
        // LanguageManager powinien być już zainicjalizowany przez Hilt w Application.onCreate()
        // i jego wewnętrzny stan języka (currentPersistedLanguageTag) ustawiony.

        val contextWithLocale = localLanguageManager.getContextWithLocale(newBase)
        // Ten log również jest bardzo przydatny
        Log.d(TAG_LIFECYCLE, "attachBaseContext - Context Locale After Wrap: ${contextWithLocale.resources.configuration.locales[0]}")
        super.attachBaseContext(contextWithLocale)
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

    // --- DODANE METODY CYKLU ŻYCIA Z LOGAMI ---
    // Te logi są przydatne do ogólnego debugowania cyklu życia.
    // Jeśli nie debugujesz aktywnie problemów z cyklem życia, można by je usunąć
    // lub obniżyć ich poziom logowania (np. na VERBOSE), aby nie zaśmiecały Logcata
    // podczas normalnego użytkowania. Decyzja zależy od Ciebie.
    // Na razie, jeśli nie przeszkadzają, można je zostawić.
//    override fun onStart() {
//        super.onStart()
//        Log.d(TAG_LIFECYCLE, "onStart CALLED - Instance: $this")
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Log.d(TAG_LIFECYCLE, "onResume CALLED - Instance: $this")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Log.d(TAG_LIFECYCLE, "onPause CALLED - Instance: $this")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Log.d(TAG_LIFECYCLE, "onStop CALLED - Instance: $this")
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        Log.d(TAG_LIFECYCLE, "onSaveInstanceState CALLED - Instance: $this")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(TAG_LIFECYCLE, "onDestroy CALLED - Instance: $this")
//    }
//    // --- KONIEC DODANYCH METOD CYKLU ŻYCIA ---
}