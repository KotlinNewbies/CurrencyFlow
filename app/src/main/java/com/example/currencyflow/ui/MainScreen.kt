package com.example.currencyflow.ui

import com.example.currencyflow.ui.components.ValuePairsInput
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.UUIDManager
import com.example.currencyflow.addContainer
import com.example.currencyflow.classes.Navigation
import com.example.currencyflow.data.C
import com.example.currencyflow.data.data_management.loadContainerData
import com.example.currencyflow.data.data_management.loadData
import com.example.currencyflow.data.data_management.saveContainerData
import com.example.currencyflow.network.isNetworkAvailable
import com.example.currencyflow.network.networking
import com.example.currencyflow.removeContainerAtIndex
import com.example.currencyflow.restoreInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainScreen(activity: ComponentActivity, pairCount: Int, navController: NavController) {
    var elapsedTime by remember { mutableLongStateOf(0L) } // Przechowywanie czasu
    val uuidString = loadData(activity)?.id ?: UUIDManager.getUUID()
    var networkError by remember { mutableStateOf(false) }
    var rcSuccess by remember { mutableStateOf(false) }
    var dbSuccess by remember { mutableStateOf(false) }

    // Ustawienie wartości par z pliku
    val pairDataModel = loadContainerData(context = activity)
    val containers = remember { mutableStateListOf<C>() }
    var pairCountLocal = pairDataModel?.pairCount ?: pairCount // Ustawienie pairCountLocal na wartość z pliku lub domyślną

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
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    println("Ilość kontenerów L przed dodaniem: $pairCountLocal")
                    addContainer(containers)
                    pairCountLocal += 1
                    saveContainerData(activity, pairCountLocal, containers)
                    println("Ilość par L po dodaniu: $pairCountLocal")
                }) {
                    Text(text = "Dodaj")
                }
                Spacer(
                    modifier = Modifier
                        .width(20.dp)
                )
                Icon(
                    modifier = Modifier
                        .clickable {
                            navController.navigate(Navigation.Favorites.route)
                        },
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_favorite_border_24),
                    contentDescription = null
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        )
        LaunchedEffect(Unit) {
            // Inicjalizacja kontenerów przy pierwszym renderowaniu
            pairDataModel?.containers?.forEach { container ->
                restoreInterface(containers, container.from, container.to, container.amount, container.result)
            }
            repeat(pairCountLocal - containers.size) {
                addContainer(containers)
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
                    containers = containers,
                    onValueChanged = { index, newValue1, newValue2 ->
                        containers[index] = containers[index].copy(amount = newValue1, result = newValue2)
                    },
                    onCurrencyChanged = { index, fromCurrency, toCurrency ->
                        containers[index] = containers[index].copy(from = fromCurrency, to = toCurrency)
                    },
                    onRemovePair = { index -> removeContainerAtIndex(index, containers, activity, pairCountLocal) },
                    context = activity,
                    pairCount = pairCount
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
                if (containers.all { it.amount.isNotEmpty() && it.result.isNotEmpty() }) {
                    containers.forEachIndexed { index, pair ->
                        Log.d("Pole ${index + 1}", pair.toString())
                    }
                } else {
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
            activity.lifecycleScope.launch(Dispatchers.IO) {
                if (!isNetworkAvailable(activity)) {
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


