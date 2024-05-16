package com.example.currencyflow.ui

import com.example.currencyflow.ui.components.ValuePairsInput
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.currencyflow.R
import com.example.currencyflow.UUIDManager
import com.example.currencyflow.addPair
import com.example.currencyflow.data.data_management.loadData
import com.example.currencyflow.data.data_management.savePairCount
import com.example.currencyflow.network.isNetworkAvailable
import com.example.currencyflow.network.networking
import com.example.currencyflow.removePair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainScreen(activity: ComponentActivity, pairCount: Int) {
    var pairCountLocal = pairCount
    var elapsedTime by remember { mutableLongStateOf(0L) } // przechowywanie czasu
    val uuidString = loadData(activity)?.id ?: UUIDManager.getUUID()
    var networkError by remember { mutableStateOf(false) }
    var rcSuccess: Boolean by remember { mutableStateOf(false) }
    var dbSuccess: Boolean by remember { mutableStateOf(false) }

    // Zmiana na listę par pól
    val valuePairs = remember { mutableStateListOf<Pair<String, String>>() }

    val pacificoRegular = FontFamily(
        Font(R.font.pacifico_regular, FontWeight.Bold)
    )

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "CurrencyFlow", fontFamily = pacificoRegular, fontSize = 35.sp)
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
        Column {
            Row {

                Button(onClick = {
                    println("Ilość par L przed dodaniem: $pairCountLocal")
                    println("Ilość par przed dodaniem: $pairCount")
                    addPair(activity, valuePairs)
                    pairCountLocal += 1
                    savePairCount(activity, pairCountLocal)
                    println("Ilość par L po dodaniu: $pairCountLocal")
                    println("Ilość par po dodaniu: $pairCount")

                }

                ) {
                    Text(text = "Dodaj")

                }
                Button(
                    onClick = {

                        if (pairCountLocal > 0) {
                            pairCountLocal -= 1
                            removePair(activity, valuePairs, indexToRemove = 0)
                            savePairCount(activity, pairCountLocal)
                        }
                        println("Ilość par: $pairCountLocal")}) {
                    Text(text = "Usuń")
                }
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        )
        // generating a proper number of pairs
        if (valuePairs.size < pairCountLocal) {
            repeat(pairCountLocal - valuePairs.size) {
                addPair(activity, valuePairs)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 15.dp, end = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                ValuePairsInput(
                    valuePairs = valuePairs,
                    onValueChanged = { index, newValue1, newValue2 ->
                        valuePairs[index] = Pair(newValue1, newValue2)
                    }
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )
        Button(
            onClick = {
                if (valuePairs.all { it.first.isNotEmpty() && it.second.isNotEmpty() }) {
                    valuePairs.forEachIndexed { index, pair ->
                        Log.d("Pole ${index + 1}", pair.toString())
                    }
                }
                else {
                    // Obsługa przypadku, gdy nie wszystkie pary mają wprowadzone wartości
                    Log.e("Błąd", "Nie wszystkie pary mają wprowadzone wartości")
                }
            }
        ) {
            Text("Przelicz")
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        )
        Button(onClick = {
            // Sprawdzenie czy wszystkie pary mają wprowadzone wartości
            //if (valuePairs.all { it.first.isNotEmpty() && it.second.isNotEmpty() }) {
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
                    Log.d("Czas wykonania", "Czas wykonania: ${elapsedTime}ms")
                    Log.d("Odbiór danych: ", "Odbiór danych: [$rcSuccess]")
                    Log.d("Zapis danych: ","Zapis danych: [$dbSuccess]")
                }
            //} else {
                // Obsługa przypadku, gdy nie wszystkie pary mają wprowadzone wartości
            //    Log.e("Błąd", "Nie wszystkie pary mają wprowadzone wartości")
            //}
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

