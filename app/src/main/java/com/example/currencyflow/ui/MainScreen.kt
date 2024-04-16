package com.example.currencyflow.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.currencyflow.R
import com.example.currencyflow.UUIDManager
import com.example.currencyflow.data.data_management.loadData
import com.example.currencyflow.network.isNetworkAvailable
import com.example.currencyflow.network.networking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainScreen(activity: ComponentActivity) {
    var elapsedTime by remember { mutableLongStateOf(0L) } // przechowywanie czasu
    val uuidString = loadData(activity)?.id ?: UUIDManager.getUUID()
    var networkError by remember { mutableStateOf(false) }
    var rcSuccess:Boolean by remember { mutableStateOf(false) }
    var dbSuccess:Boolean by remember { mutableStateOf(false) }

    // zmienne pól tekstowych
    var var1 by remember { mutableStateOf("")}
    var var1filling by remember { mutableStateOf("") }

    val pacificoRegular = FontFamily(
        Font(R.font.pacifico_regular, FontWeight.Bold)
    )

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "CurrencyFlow", fontFamily = pacificoRegular, fontSize = 35.sp)
        }
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(250.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .width(140.dp),
                value = "",
                onValueChange ={}
            )
            Icon( modifier = Modifier.size(40.dp),
                painter = painterResource(id = R.drawable.swap_horizontal),
                contentDescription = null)
            OutlinedTextField(
                modifier = Modifier
                    .width(140.dp),
                value = "",
                onValueChange ={}
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )
        Button(onClick = {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                if (!isNetworkAvailable(activity)) {
                    // Obsługa braku połączenia z internetem
                    networkError = true
                    return@launch
                }

                val startTime = System.currentTimeMillis() // początek mierzenia czasu
                val (rc, db) = networking(uuidString)
                rcSuccess = rc
                dbSuccess = db

                networkError = !rc // kontrola zmiennej w przypadku dostępności i braku neta
                val endTime = System.currentTimeMillis() // koniec mierzenia czasu
                elapsedTime = endTime - startTime
                println("Czas wykonania: ${elapsedTime}ms")
                println("Odbiór danych: [$rcSuccess]")
                println("Zapis danych: [$dbSuccess]")
            }
        }) {
            Text("Wysyłanie danych")
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )
        if (networkError) {
            Text(text = "Brak neta")
        }
    }
}
