package com.example.currencyflow.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.UUIDManager
import com.example.currencyflow.addContainer
import com.example.currencyflow.addContainerIfEmpty
import com.example.currencyflow.classes.Navigation
import com.example.currencyflow.data.C
import com.example.currencyflow.data.CurrencyViewModel
import com.example.currencyflow.data.data_management.loadContainerData
import com.example.currencyflow.data.data_management.loadData
import com.example.currencyflow.data.data_management.loadSelectedCurrencies
import com.example.currencyflow.data.data_management.saveContainerData
import com.example.currencyflow.data.processContainers
import com.example.currencyflow.network.isNetworkAvailable
import com.example.currencyflow.network.networking
import com.example.currencyflow.removeContainerAtIndex
import com.example.currencyflow.restoreInterface
import com.example.currencyflow.ui.components.ValuePairsInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.currencyflow.haptics.triggerSoftVibration

@Composable
fun MainScreen(
    activity: ComponentActivity,
    navController: NavController,
    currencyViewModel: CurrencyViewModel,
) {

    val currencyRates by currencyViewModel.currencyRates.collectAsState() // Obserwowanie kursów walut
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

    val pacificoRegular = FontFamily(
        Font(R.font.pacifico_regular, FontWeight.Bold)
    )
    // Monitorowanie stanu sieci
    val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkCallback = remember {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                scope.launch(Dispatchers.IO) {  // Zmiana tutaj na IO dispatcher, aby wykonać operację sieciową w tle
                    if (isNetworkAvailable(activity)) {
                        val startTime = System.currentTimeMillis()
                        val (rc, db) = networking(uuidString, containers, currencyViewModel)
                        rcSuccess = rc
                        dbSuccess = db

                        networkError = !rc
                        val endTime = System.currentTimeMillis()
                        elapsedTime = endTime - startTime
                        Log.d("Czas wykonania", "Czas wykonania: ${elapsedTime}ms")
                        Log.d("Odbiór danych: ", "Odbiór danych: [$rcSuccess]")
                        Log.d("Zapis danych: ", "Zapis danych: [$dbSuccess]")
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                scope.launch {
                    networkError = true
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "CurrencyFlow", fontFamily = pacificoRegular, fontSize = 35.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            LaunchedEffect(Unit) {

                // Inicjalizacja kontenerów przy pierwszym renderowaniu
                pairDataModel?.containers?.forEach/*Indexed*/ { /*index,*/ container ->
                    restoreInterface(containers, container.from, container.to, container.amount, container.result)
                }
                    addContainerIfEmpty(containers, selectedCurrencies, activity)
            }

            Row(
                modifier = Modifier
                    .weight(0.65f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 15.dp, end = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                    )
                    ValuePairsInput(
                        containers = containers,
                        onValueChanged = { index, newValue1, newValue2 ->
                            containers[index] = containers[index].copy(amount = newValue1, result = newValue2)
                        },
                        onCurrencyChanged = { index, fromCurrency, toCurrency ->
                            containers[index] = containers[index].copy(from = fromCurrency, to = toCurrency)
                        },
                        onRemovePair = { index ->
                            removeContainerAtIndex(index, containers, activity)
                        },
                        context = activity,
                        selectedCurrencies = selectedCurrencies,
                        currencyViewModel = currencyViewModel
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
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
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.Black
                                ),
                                onClick = {
                                    println("Ilość kontenerów przed L dodaniem: ${containers.size}")
                                    addContainer(containers, selectedCurrencies)
                                    processContainers(currencyRates, containers)
                                    saveContainerData(activity, containers)
                                    triggerSoftVibration(activity)
                                    println("Ilość kontenerów po L dodaniu: ${containers.size}")
                                }) {
                                Icon(
                                    modifier = Modifier
                                        .size(26.dp),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.round_add_24),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )

                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.Black
                                ),
                                onClick = {
                                    navController.navigate(Navigation.Favorites.route)
                                }) {
                                Icon(
                                    modifier = Modifier
                                        .size(26.dp),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.round_favorite_border_24),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
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
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black
                            ),
                            onClick = {
                            println("Ilość kontenerów przed L dodaniem: ${containers.size}")
                            addContainer(containers, selectedCurrencies)
                            processContainers(currencyRates, containers)
                            saveContainerData(activity, containers)
                            triggerSoftVibration(activity)
                            println("Ilość kontenerów po L dodaniu: ${containers.size}")
                        }) {
                            Icon(
                                modifier = Modifier
                                    .size(26.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.round_add_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary
                            )

                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black
                        ),
                            onClick = {
                            navController.navigate(Navigation.Favorites.route)
                        }) {
                            Icon(
                                modifier = Modifier
                                    .size(26.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.round_favorite_border_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
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
                    networkError = false
                }
            }
        }
    }
}
