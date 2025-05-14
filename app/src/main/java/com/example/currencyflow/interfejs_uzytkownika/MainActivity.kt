package com.example.currencyflow.interfejs_uzytkownika

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.currencyflow.klasy.Nawigacja
import com.example.currencyflow.dane.WybraneWalutyViewModel
import com.example.currencyflow.dane.zarzadzanie_danymi.zapiszDane
import com.example.currencyflow.interfejs_uzytkownika.theme.CurrencyFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val wybraneWalutyViewModel: WybraneWalutyViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nazwaPliku = "dane_uzytkownika.json"
        val plik = File(filesDir, nazwaPliku)
        if (!plik.exists()) { // Sprawdzenie czy plik istnieje przy starcie aplikacji
            zapiszDane(this)
        }

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
                            GlownyEkran(aktywnosc = this@MainActivity,
                                kontrolerNawigacji = navController,
                                viewModel = wybraneWalutyViewModel)
                        }
                        composable(Nawigacja.UlubioneWaluty.route) {
                            UlubioneWaluty(
                                navController = navController,
                                viewModel = wybraneWalutyViewModel )
                        }
                    }
                }
            }
        }
    }
}