package com.example.currencyflow.interfejs_uzytkownika

import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
                                homeViewModel = homeViewModel // Przekazujemy nowy ViewModel
                            )
                        }
                        composable(Nawigacja.UlubioneWaluty.route) {
                            // Hilt dostarczy nową instancję ViewModelu dla ekranu ulubionych
                            val favoriteCurrenciesViewModel: FavoriteCurrenciesViewModel =
                                hiltViewModel()
                            UlubioneWaluty(
                                navController = navController,
                                viewModel = favoriteCurrenciesViewModel // Przekazujemy nowy ViewModel
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