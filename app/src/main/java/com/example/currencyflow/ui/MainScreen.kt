package com.example.currencyflow.ui

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.currencyflow.data.data_management.loadSelectedCurrencies
import com.example.currencyflow.data.data_management.saveContainerData
import com.example.currencyflow.network.isNetworkAvailable
import com.example.currencyflow.network.networking
import com.example.currencyflow.removeContainerAtIndex
import com.example.currencyflow.restoreInterface
import com.example.currencyflow.ui.components.ValuePairsInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainScreen(activity: ComponentActivity, pairCount: Int, navController: NavController) {
    var elapsedTime by remember { mutableLongStateOf(0L) }
    val uuidString = loadData(activity)?.id ?: UUIDManager.getUUID()
    var networkError by remember { mutableStateOf(false) }
    var rcSuccess by remember { mutableStateOf(false) }
    var dbSuccess by remember { mutableStateOf(false) }
    val selectedCurrencies = remember { loadSelectedCurrencies(activity) }

    // Snackbar
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Ustawienie wartości par z pliku
    val pairDataModel = loadContainerData(context = activity)
    val containers = remember { mutableStateListOf<C>() }
    var pairCountLocal = pairDataModel?.pairCount ?: pairCount // Ustawienie pairCountLocal na wartość z pliku lub domyślną

    val pacificoRegular = FontFamily(
        Font(R.font.pacifico_regular, FontWeight.Bold)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.15f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "CurrencyFlow", fontFamily = pacificoRegular, fontSize = 35.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            LaunchedEffect(Unit) {
                // Inicjalizacja kontenerów przy pierwszym renderowaniu
                pairDataModel?.containers?.forEach { container ->
                    restoreInterface(containers, container.from, container.to, container.amount, container.result)
                }
                repeat(pairCount - containers.size) {
                    addContainer(containers, selectedCurrencies)
                }
            }

            Row(
                modifier = Modifier
                    .weight(0.6f)
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
                            // Sprawdzamy, czy którykolwiek kontener ma wprowadzone dane
                            //checkContainersForData(containers, scope, snackbarHostState)
                        },
                        onCurrencyChanged = { index, fromCurrency, toCurrency ->
                            containers[index] = containers[index].copy(from = fromCurrency, to = toCurrency)
                            // Sprawdzamy, czy którykolwiek kontener ma wprowadzone dane
                            //checkContainersForData(containers, scope, snackbarHostState)
                        },
                        onRemovePair = { index -> removeContainerAtIndex(index, containers, activity, pairCountLocal) },
                        context = activity,
                        pairCount = pairCount,
                        selectedCurrencies = selectedCurrencies
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            )
            BoxWithConstraints(
                modifier = Modifier
                    .weight(0.15f)
                    .fillMaxWidth()
            ) {
                if (maxWidth < 600.dp) {
                    Column(
                        modifier = Modifier
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSurface,  // Ustawia tło przycisku jako przezroczyste
                                    contentColor = Color.Black // Ustawia kolor tekstu przycisku na czerwony
                                ),
                                onClick = {
                                    // Sprawdzamy, czy którykolwiek kontener ma wprowadzone dane
                                    checkContainersForData(containers, scope, snackbarHostState)
                                }
                            ) {
                                Text("Przelicz")
                            }
                            Spacer(modifier = Modifier
                                .width(20.dp))
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSurface,  // Ustawia tło przycisku jako przezroczyste
                                    contentColor = Color.Black // Ustawia kolor tekstu przycisku na czerwony
                                ),
                                onClick = {
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
                            Spacer(modifier = Modifier
                                .width(20.dp)
                            )
                            Icon(
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(Navigation.Favorites.route)
                                    },
                                imageVector = ImageVector.vectorResource(id = R.drawable.round_favorite_border_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSurface,  // Ustawia tło przycisku jako przezroczyste
                                    contentColor = Color.Black // Ustawia kolor tekstu przycisku na czerwony
                                ),
                                onClick = {
                                println("Ilość kontenerów przed dodaniem: $pairCountLocal")
                                addContainer(containers, selectedCurrencies)
                                pairCountLocal += 1
                                saveContainerData(activity, pairCountLocal, containers)
                                println("Ilość par po dodaniu: $pairCountLocal")
                            }) {
                                Text(text = "Dodaj")
                            }
                        }
                    }
                }
                else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,  // Ustawia tło przycisku jako przezroczyste
                            contentColor = Color.Black // Ustawia kolor tekstu przycisku na czerwony
                        ),
                            onClick = {
                                // Sprawdzamy, czy którykolwiek kontener ma wprowadzone dane
                                checkContainersForData(containers, scope, snackbarHostState)
                            }
                        ) {
                            Text("Przelicz")
                        }
                        Spacer(
                            modifier = Modifier
                                .width(20.dp)
                        )
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface,  // Ustawia tło przycisku jako przezroczyste
                                contentColor = Color.Black // Ustawia kolor tekstu przycisku na czerwony
                            ),
                            onClick = {
                            activity.lifecycleScope.launch(Dispatchers.IO) {
                                if (!isNetworkAvailable(activity)) {
                                    networkError = true
                                    return@launch
                                }

                                val startTime =
                                    System.currentTimeMillis() // początek mierzenia czasu
                                val (rc, db) = networking(uuidString)
                                rcSuccess = rc
                                dbSuccess = db

                                networkError =
                                    !rc // kontrola zmiennej w przypadku dostępności i braku neta
                                val endTime = System.currentTimeMillis() // koniec mierzenia czasu
                                elapsedTime = endTime - startTime
                                Log.d("Czas wykonania", "Czas wykonania: ${elapsedTime}ms")
                                Log.d("Odbiór danych: ", "Odbiór danych: [$rcSuccess]")
                                Log.d("Zapis danych: ", "Zapis danych: [$dbSuccess]")
                            }
                        }) {
                            Text("Wysyłanie danych")
                        }
                        Spacer(
                            modifier = Modifier
                                .width(20.dp)
                        )
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface,
                                contentColor = Color.Black
                            ),
                            onClick = {
                            println("Ilość kontenerów przed L dodaniem: $pairCount")
                            addContainer(containers, selectedCurrencies)
                            pairCountLocal += 1
                            saveContainerData(activity,pairCount, containers)
                            println("Ilość par po L dodaniu: $pairCount")
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
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(networkError) {
        if (networkError) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Brak połączenia z siecią",
                    actionLabel = "Zamknij"
                )
                if (result == SnackbarResult.ActionPerformed) {
                    // Dodatkowa logika po kliknięciu przycisku "Zamknij" (jeśli potrzebna)
                    networkError = false
                }
            }
        }
    }
}

// Funkcja sprawdzająca, czy którykolwiek kontener ma wprowadzone dane
private fun checkContainersForData(containers: List<C>, scope: CoroutineScope, snackbarHostState: SnackbarHostState) {
    var anyContainerHasData = false
    containers.forEach { pair ->

        if (pair.amount.isNotEmpty() || pair.result.isNotEmpty()) {
            anyContainerHasData = true
            Log.d("Dane kontenera", "From: ${pair.from}, To: ${pair.to}, Amount: ${pair.amount}, Result: ${pair.result}")
            return@forEach
        }
    }
    if (!anyContainerHasData) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = "Nie ma wprowadzonych danych w żadnym kontenerze",
                actionLabel = "Zamknij"
            )
        }
    }
}
