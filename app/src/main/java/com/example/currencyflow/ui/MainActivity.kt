package com.example.currencyflow.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.currencyflow.classes.Navigation
import com.example.currencyflow.data.data_management.loadContainerData
import com.example.currencyflow.data.data_management.saveData
import com.example.currencyflow.ui.theme.CurrencyFlowTheme
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sprawdzenie czy plik istnieje przy starcie aplikacji
        val fileName = "user_data.json"
        val file = File(filesDir, fileName)
        if (!file.exists()) {
            saveData(this) // Zapisz plik jeśli plik nie istnieje
        }

        // Dane ładowane asynchronicznie
        setContent {
            //var pairCount by remember { mutableStateOf(0) }

//            LaunchedEffect(Unit) {
//                val pairDataModel = loadContainerData(context = this@MainActivity)
//                //pairCount = pairDataModel?.pairCount ?: 0
//            }

            CurrencyFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Navigation.Home.route) {
                        composable(Navigation.Home.route) {
                            MainScreen(this@MainActivity, navController)
                        }
                        composable(Navigation.Favorites.route) {
                            FavCurrencies(navController)
                        }
                    }
                }
            }
        }
    }
}