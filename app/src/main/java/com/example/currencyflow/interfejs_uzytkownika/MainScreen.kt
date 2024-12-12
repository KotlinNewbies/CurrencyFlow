package com.example.currencyflow.interfejs_uzytkownika

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.UUIDMenadzer
import com.example.currencyflow.dodajKontener
import com.example.currencyflow.dodajKontenerJesliBrak
import com.example.currencyflow.klasy.Nawigacja
import com.example.currencyflow.data.C
import com.example.currencyflow.data.WalutyViewModel
import com.example.currencyflow.data.zarzadzanie_danymi.wczytajDaneKontenerow
import com.example.currencyflow.data.zarzadzanie_danymi.wczytajDane
import com.example.currencyflow.data.zarzadzanie_danymi.wczytajWybraneWaluty
import com.example.currencyflow.data.zarzadzanie_danymi.zapiszDaneKontenerow
import com.example.currencyflow.data.przetworzKontenery
import com.example.currencyflow.siec.zadanieSieci
import com.example.currencyflow.usunWybranyKontener
import com.example.currencyflow.przywrocInterfejs
import com.example.currencyflow.interfejs_uzytkownika.components.ValuePairsInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.currencyflow.haptyka.spowodujSlabaWibracje
import com.example.currencyflow.siec.sprawdzDostepnoscInternetu
import com.example.currencyflow.interfejs_uzytkownika.components.FloatingButtonDown
import com.example.currencyflow.interfejs_uzytkownika.components.FloatingButtonUp
import kotlinx.coroutines.delay

fun czyPoziomo(activity: Activity): Boolean {
    val konfiguracja = activity.resources.configuration
    return konfiguracja.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun czyTelefon(activity: Activity): Boolean {
    val konfiguracja = activity.resources.configuration
    return konfiguracja.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_NORMAL
}

@Composable
fun MainScreen(
    activity: ComponentActivity,
    navController: NavController,
    walutyViewModel: WalutyViewModel,
) {

    val mnoznikiWalut by walutyViewModel.mnoznikiWalut.collectAsState()
    var uplywajacyCzas by remember { mutableLongStateOf(0L) }
    val uuidString = wczytajDane(activity)?.id ?: UUIDMenadzer.zdobadzUUID()
    var networkError by remember { mutableStateOf(false) }
    var rcSuccess by remember { mutableStateOf(false) }
    var dbSuccess by remember { mutableStateOf(false) }
    val selectedCurrencies = remember { wczytajWybraneWaluty(activity) }

    // Snackbar
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSnackbarVisible by remember { mutableStateOf(false) }
    var previousNetworkError by remember { mutableStateOf(false) } // New state to track previous error
    var hasShownNetworkError by remember { mutableStateOf(false) } // Nowy stan do śledzenia wyświetlenia błędu


    // Ustawienie wartości par z pliku
    val pairDataModel = wczytajDaneKontenerow(context = activity)
    val containers = remember { mutableStateListOf<C>() }

    val pacificoRegular = FontFamily(
        Font(R.font.pacifico_regular, FontWeight.Bold)
    )

    // progressIndicator
    var progressIndicatorVisible by remember { mutableStateOf(false) }

    // scrollowanie
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Monitorowanie stanu sieci
    val connectivityManager =
        activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    val networkCallback = remember {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                scope.launch(Dispatchers.IO) {
                    delay(1000)
                    if (sprawdzDostepnoscInternetu(activity)) {
                        val startTime = System.currentTimeMillis()
                        progressIndicatorVisible = true
                        val (rc, db) = zadanieSieci(uuidString, containers, walutyViewModel)
                        rcSuccess = rc
                        dbSuccess = db

                        networkError = !rc
                        val endTime = System.currentTimeMillis()
                        progressIndicatorVisible = false
                        uplywajacyCzas = endTime - startTime
                        Log.d("Czas wykonania", "Czas wykonania: ${uplywajacyCzas}ms")
                        Log.d("Odbiór danych: ", "Odbiór danych: [$rcSuccess]")
                        Log.d("Zapis danych: ", "Zapis danych: [$dbSuccess]")

                        // dane są zapisywane tylko raz po ich pobraniu z sieci
                        if (rcSuccess || dbSuccess) {
                            zapiszDaneKontenerow(activity, containers)
                        } else {
                            networkError = true

                        }
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                scope.launch {
                    previousNetworkError = true
                    networkError = true
                    progressIndicatorVisible = false
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

    LaunchedEffect(Unit) {
        if (!sprawdzDostepnoscInternetu(activity)) {
            networkError = true
            progressIndicatorVisible = false
        } else {
            if (containers.isEmpty()) {
                progressIndicatorVisible = true
                pairDataModel?.kontenery?.forEach { container ->
                    przywrocInterfejs(
                        containers,
                        container.from,
                        container.to,
                        container.amount,
                        container.result
                    )
                }
                dodajKontenerJesliBrak(containers, selectedCurrencies, activity)
            }

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
                        .fillMaxWidth()
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
                        Row(
                            modifier = Modifier
                                .weight(0.2f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                        }
                        Row(
                            modifier = Modifier
                                .weight(0.6f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "CurrencyFlow",
                                fontFamily = pacificoRegular,
                                fontSize = 35.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                        }
                        Row(
                            modifier = Modifier
                                .weight(0.2f),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.End
                        ) {
                            AnimatedVisibility(
                                visible = progressIndicatorVisible,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .padding(top = 10.dp, end = 15.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                // Inicjalizacja kontenerów tylko raz
                if (containers.isEmpty()) {
                    pairDataModel?.kontenery?.forEach { container ->
                        przywrocInterfejs(
                            containers,
                            container.from,
                            container.to,
                            container.amount,
                            container.result
                        )
                    }
                    dodajKontenerJesliBrak(containers, selectedCurrencies, activity)
                }
            }

            Row(
                modifier = Modifier
                    .weight(0.65f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(start = 15.dp, end = 15.dp)
                        .animateContentSize(),
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
                            containers[index] =
                                containers[index].copy(amount = newValue1, result = newValue2)
                        },
                        onCurrencyChanged = { index, fromCurrency, toCurrency ->
                            containers[index] =
                                containers[index].copy(from = fromCurrency, to = toCurrency)
                        },
                        onRemovePair = { index ->
                            usunWybranyKontener(index, containers, activity)
                        },
                        context = activity,
                        selectedCurrencies = selectedCurrencies,
                        walutyViewModel = walutyViewModel
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(if (czyPoziomo(activity) && czyTelefon(activity)) 0.25f else 0.15f)
                    .fillMaxWidth()
            ) {
                if (maxWidth < 600.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.Center)
                                    .size(width = 30.dp, height = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Przyciski pływające
                                val showDownButton by remember {
                                    derivedStateOf {
                                        scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue
                                    }
                                }
                                val showUpButton by remember {
                                    derivedStateOf {
                                        scrollState.maxValue > 0 && scrollState.value > 0
                                    }
                                }

                                Column {
                                    AnimatedVisibility(
                                        visible = showDownButton,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        FloatingButtonDown(scrollState = scrollState)
                                    }
                                }

                                Column {
                                    AnimatedVisibility(
                                        visible = showUpButton,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        FloatingButtonUp(scrollState = scrollState)
                                    }
                                }
                            }
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
                                        dodajKontener(containers, selectedCurrencies)
                                        przetworzKontenery(mnoznikiWalut, containers)
                                        zapiszDaneKontenerow(activity, containers)
                                        spowodujSlabaWibracje(activity)
                                        coroutineScope.launch {
                                            snapshotFlow { scrollState.maxValue }
                                                .collect { maxValue ->
                                                    scrollState.animateScrollTo(maxValue)
                                                }
                                        }
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
                                        navController.navigate(Nawigacja.UlubioneWaluty.route)
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

                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                    ) {
                        Box {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(Modifier.layoutId("floatingButtonsColumn")),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentSize(Alignment.Center)
                                        .size(width = 30.dp, height = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Przyciski pływające
                                    val showDownButton by remember {
                                        derivedStateOf {
                                            scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue
                                        }
                                    }
                                    val showUpButton by remember {
                                        derivedStateOf {
                                            scrollState.maxValue > 0 && scrollState.value > 0
                                        }
                                    }
                                    Column {
                                        AnimatedVisibility(
                                            visible = showDownButton,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            FloatingButtonDown(scrollState = scrollState)
                                        }
                                    }
                                    Column {
                                        AnimatedVisibility(
                                            visible = showUpButton,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            FloatingButtonUp(scrollState = scrollState)
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.Black
                                        ),
                                        onClick = {
                                            dodajKontener(containers, selectedCurrencies)
                                            przetworzKontenery(mnoznikiWalut, containers)
                                            zapiszDaneKontenerow(activity, containers)
                                            spowodujSlabaWibracje(activity)
                                            coroutineScope.launch {
                                                snapshotFlow { scrollState.maxValue }
                                                    .collect { maxValue ->
                                                        scrollState.animateScrollTo(maxValue)
                                                    }
                                            }
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
                                            navController.navigate(Nawigacja.UlubioneWaluty.route)
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
            }
        }
    }

    LaunchedEffect(networkError) {
        if (networkError && !isSnackbarVisible) {
            isSnackbarVisible = true
            hasShownNetworkError = true
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Brak połączenia z siecią"
                )
                if (result == SnackbarResult.ActionPerformed) {
                    networkError = false
                }
                isSnackbarVisible = false
            }
        } else if (!networkError && hasShownNetworkError) {
            // Jeśli połączenie zostało przywrócone, a wcześniej był wyświetlony błąd
            hasShownNetworkError = false
            snackbarHostState.showSnackbar(message = "Połączenie z siecią zostało przywrócone")
        }
    }
}
