package com.example.currencyflow.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.currencyflow.data.data_management.loadContainerData
import com.example.currencyflow.data.data_management.saveData
import com.example.currencyflow.ui.theme.CurrencyFlowTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        // Załaduj dane asynchronicznie
        setContent {
            var pairCount by remember { mutableStateOf(0) }

            LaunchedEffect(Unit) {
                val pairDataModel = loadContainerData(context = this@MainActivity)
                pairCount = pairDataModel?.pairCount ?: 0
            }

            CurrencyFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(this@MainActivity, pairCount)
                }
            }
        }
    }
}