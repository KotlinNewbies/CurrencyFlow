package com.example.currencyflow.ui

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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

    // Zmiana na listę par pól
    val valuePairs = remember { mutableStateListOf<Pair<String, String>>() }

    // Funkcja dodająca parę pól do listy par
    fun addPair() {
        valuePairs.add("" to "")
    }
    fun removePair() {
        valuePairs.remove("" to "")
    }

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
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
        )

        Button(onClick = { addPair() }) {
            Text(text = "Dodaj")
        }
        Button(onClick = { removePair() }) {
            Text(text = "Usuń")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                valuePairs.forEachIndexed { index, (value1, value2) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.width(150.dp),
                            value = value1,
                            onValueChange = { newValue ->
                                valuePairs[index] = Pair(newValue, valuePairs[index].second)
                            }
                        )
                        Icon(
                            modifier = Modifier.size(40.dp),
                            painter = painterResource(id = R.drawable.swap_horizontal),
                            contentDescription = null
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(150.dp),
                            value = value2,
                            onValueChange = { newValue ->
                                valuePairs[index] = Pair(valuePairs[index].first, newValue)
                            }
                        )

                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )
        Button(
            onClick = {
                    valuePairs.forEachIndexed { index, pair ->
                        Log.d("Pole ${index + 1}", pair.toString())
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
