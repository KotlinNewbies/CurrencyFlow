package com.example.currencyflow.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.currencyflow.data.data_management.loadPairCount
import com.example.currencyflow.data.data_management.saveData
import com.example.currencyflow.ui.theme.CurrencyFlowTheme
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Sprawdzenie czy plik istnieje przy starcie aplikacji
        val fileName = "user_data.json"
        val file = File(filesDir, fileName)
        if (!file.exists()) {
            saveData(this) // Zapisz plik jeśli plik nie istnieje
        }
        var pairCount = loadPairCount(this)
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyFlowTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    pairCount = loadPairCount(this)
                    println("Ilość par po włączeniu: $pairCount")
                    MainScreen(this@MainActivity, pairCount) // Przekazujemy kontekst com.example.template.MainActivity do funkcji com.example.template.Screen

                }
            }
        }

    }
}