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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.currencyflow.R
import com.example.currencyflow.klasy.Nawigacja
import com.example.currencyflow.dane.WalutyViewModel
import com.example.currencyflow.dane.zarzadzanie_danymi.HomeViewModel
import com.example.currencyflow.dane.zarzadzanie_danymi.wczytajDane
import com.example.currencyflow.siec.zadanieSieci
import com.example.currencyflow.interfejs_uzytkownika.komponenty.KontenerWalut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.currencyflow.haptyka.spowodujSlabaWibracje
import com.example.currencyflow.siec.sprawdzDostepnoscInternetu
import com.example.currencyflow.interfejs_uzytkownika.komponenty.PlywajacyPrzyciskWDol
import com.example.currencyflow.interfejs_uzytkownika.komponenty.PlywajacyPrzyciskWGore
import kotlinx.coroutines.delay


private const val TAG = "GlownyEkran" // Dodaj TAG
@Composable
fun GlownyEkran(
    homeViewModel: HomeViewModel = hiltViewModel(), // Używaj tej instancji dostarczonej przez Hilt
    aktywnosc: ComponentActivity,
    kontrolerNawigacji: NavController,
    walutyViewModel: WalutyViewModel = hiltViewModel() // Ta instancja również jest poprawnie dostarczana przez Hilt
) {
    // Dodaj ten DisposableEffect tutaj
    DisposableEffect(Unit) {
        Log.d(TAG, "GlownyEkran: entered composition")
        onDispose {
            Log.d(TAG, "GlownyEkran: onDispose - Destynacja nawigacji jest usuwana.")
        }
    }


    var uplywajacyCzas by remember { mutableLongStateOf(0L) }
    val ciagUUID = wczytajDane(aktywnosc)!!.id
    var bladSieci by remember { mutableStateOf(false) }
    var rcSuccess by remember { mutableStateOf(false) }
    var dbSuccess by remember { mutableStateOf(false) }

    // Snackbar
    val zakres = rememberCoroutineScope()
    val stanSnackbara = remember { SnackbarHostState() }
    var czyWidocznySnackbar by remember { mutableStateOf(false) }
    var poprzedniBladSieci by remember { mutableStateOf(false) }
    var czyPokazanyBladSieci by remember { mutableStateOf(false) } // Nowy stan do śledzenia wyświetlenia błędu


    // Obserwujemy stany z NOWEGO HomeViewModel
    val konteneryUI by homeViewModel.konteneryUI.collectAsState() // Obserwujemy kontenery z ViewModelu
    val dostepneWalutyDlaKontenerow by homeViewModel.dostepneWalutyDlaKontenerow.collectAsState() // Obserwujemy dostępne waluty z ViewModelu

    // --- TUTAJ BĘDZIE POTRZEBNY MECHANIZM ODŚWIEŻANIA DOSTĘPNYCH WALUT PO POWROCIE ---
    // Jednym ze sposobów jest użycie currentBackStackEntryAsState()
    val navBackStackEntry by kontrolerNawigacji.currentBackStackEntryAsState()

    val czcionkaPacificoRegular = FontFamily(
        Font(R.font.pacifico_regular, FontWeight.Bold)
    )

    // wskaznik ladowania
    var widocznoscWskaznikaLadowania by remember { mutableStateOf(false) }

    // scrollowanie
    val stanPrzesuniecia = rememberScrollState()
    val zakresKorutyn = rememberCoroutineScope()

    // Monitorowanie stanu sieci
    val menadzerLacznosci =
        aktywnosc.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    LaunchedEffect(navBackStackEntry) {
        // Sprawdzamy, czy wracamy z ekranu Ulubionych Walut
        // i czy destynacja jest ekranem głównym
        if (navBackStackEntry?.destination?.route == Nawigacja.Dom.route) {
            // To oznacza, że ekran główny jest na szczycie stosu nawigacji
            // Jeśli poprzednia destynacja była ekranem Ulubionych, odświeżamy dane
            val previousRoute = kontrolerNawigacji.previousBackStackEntry?.destination?.route
            if (previousRoute == Nawigacja.UlubioneWaluty.route) {
                Log.d(TAG, "Wrócono z ekranu Ulubionych Walut, odświeżanie dostępnych walut w ViewModelu")
                homeViewModel.odswiezDostepneWaluty() // Wywołujemy metodę odświeżającą dostępne waluty
                // Opcjonalnie: usuń poprzedni entry ze stosu, jeśli chcesz zapobiec wielokrotnemu odświeżaniu
                // navController.popBackStack(previousRoute, inclusive = true)
            }
        }
    }

    val wywolanieZwrotneSieci = remember {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                zakres.launch(Dispatchers.IO) {
                    delay(1000)
                    if (sprawdzDostepnoscInternetu(aktywnosc)) {
                        val poczatekCzas = System.currentTimeMillis()
                        widocznoscWskaznikaLadowania = true
                        val (rc, db) = zadanieSieci(ciagUUID, konteneryUI, walutyViewModel)
                        rcSuccess = rc
                        dbSuccess = db

                        bladSieci = !rc
                        val koniecCzas = System.currentTimeMillis()
                        widocznoscWskaznikaLadowania = false
                        uplywajacyCzas = koniecCzas - poczatekCzas
                        Log.d("Czas wykonania", "Czas wykonania: ${uplywajacyCzas}ms")
                        Log.d("Odbiór danych: ", "Odbiór danych: [$rcSuccess]")
                        Log.d("Zapis danych: ", "Zapis danych: [$dbSuccess]")

                        // dane są zapisywane tylko raz po ich pobraniu z sieci
                        if (rcSuccess || dbSuccess) {
                            //zapiszDaneKontenerow(aktywnosc, kontenery)
                            Log.d("sukces", "Operacja sieciowa sie powiodla")
                        } else {
                            bladSieci = true

                        }
                    }
                }
            }

            override fun onLost(siec: Network) {
                super.onLost(siec)
                zakres.launch {
                    poprzedniBladSieci = true
                    bladSieci = true
                    widocznoscWskaznikaLadowania = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val zadanie = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        menadzerLacznosci.registerNetworkCallback(zadanie, wywolanieZwrotneSieci)

        onDispose {
            menadzerLacznosci.unregisterNetworkCallback(wywolanieZwrotneSieci)
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = stanSnackbara) }
    ) { wypelnienieZawartosci ->
        Column(
            modifier = Modifier
                .padding(wypelnienieZawartosci)
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
                        ) {}
                        Row(
                            modifier = Modifier
                                .weight(0.6f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "CurrencyFlow",
                                fontFamily = czcionkaPacificoRegular,
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
                                visible = widocznoscWskaznikaLadowania,
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
            Row(
                modifier = Modifier
                    .weight(0.65f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(stanPrzesuniecia)
                        .padding(start = 15.dp, end = 15.dp)
                        .animateContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                    )
                    KontenerWalut(
                        kontenery = konteneryUI, // Obserwujemy StateFlow z ViewModelu
                        // Zmieniamy lambdy na wywołania metod ViewModelu
                        zdarzenieZmianyWartosci = { index, nowaWartosc1, nowaWartosc2 ->
                            val aktualnyKontener = konteneryUI[index]
                            val zaktualizowany = aktualnyKontener.copy(amount = nowaWartosc1, result = nowaWartosc2)
                            homeViewModel.zaktualizujKontener(index, zaktualizowany) // Wywołanie metody ViewModelu
                        },
                        zdarzenieZmianyWaluty = { index, zWaluty, naWalute ->
                            val aktualnyKontener = konteneryUI[index]
                            val zaktualizowany = aktualnyKontener.copy(from = zWaluty, to = naWalute)
                            homeViewModel.zaktualizujKontener(index, zaktualizowany) // Wywołanie metody ViewModelu
                        },
                        zdarzenieUsunieciaKontenera = { index ->
                            homeViewModel.usunKontener(index) // Wywołanie metody ViewModelu
                            // Usuń wywołanie usunWybranyKontener() z funkcji pomocniczej
                        },
                        context = aktywnosc,
                        wybraneWaluty = dostepneWalutyDlaKontenerow, // Obserwujemy StateFlow z ViewModelu
                        walutyViewModel = walutyViewModel, // ViewModel do kursów walut (jeśli potrzebny)
                        zdarzenieZapisuDanych = {
                            // TUTAJ WYWOŁUJEMY FUNKCJĘ Z VM, KTÓRA ZAPISZE DANE
                            homeViewModel.zapiszAktualneKontenery() // Ta funkcja musi zostać dodana do HomeViewModel
                        }
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(if (czyPoziomo(aktywnosc) && czyTelefon(aktywnosc)) 0.25f else 0.15f)
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
                                val pokazDolnyPrzyciskPrzewijania by remember {
                                    derivedStateOf {
                                        stanPrzesuniecia.maxValue > 0 && stanPrzesuniecia.value < stanPrzesuniecia.maxValue
                                    }
                                }
                                val pokazGornyPrzyciskPrzewijania by remember {
                                    derivedStateOf {
                                        stanPrzesuniecia.maxValue > 0 && stanPrzesuniecia.value > 0
                                    }
                                }

                                Column {
                                    AnimatedVisibility(
                                        visible = pokazDolnyPrzyciskPrzewijania,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        PlywajacyPrzyciskWDol(stanPrzesuniecia = stanPrzesuniecia)
                                    }
                                }

                                Column {
                                    AnimatedVisibility(
                                        visible = pokazGornyPrzyciskPrzewijania,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        PlywajacyPrzyciskWGore(stanPrzesuniecia = stanPrzesuniecia)
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
                                        homeViewModel.dodajKontener() // Wywołanie metody ViewModelu
                                        // Usuń wywołanie dodajKontener() z funkcji pomocniczej
                                        // Usuń wywołanie zapiszDaneKontenerow() stąd, bo zapis jest w ViewModelu
                                        spowodujSlabaWibracje(aktywnosc)
                                        zakresKorutyn.launch {
                                            snapshotFlow { stanPrzesuniecia.maxValue }
                                                .collect { maksymalnaWartosc ->
                                                    stanPrzesuniecia.animateScrollTo(maksymalnaWartosc)
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
                                        kontrolerNawigacji.navigate(Nawigacja.UlubioneWaluty.route)
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
                                    .then(Modifier.layoutId("kolumnaPlywajacychPrzyciskow")),
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
                                    val pokazDolnyPrzyciskPrzewijania by remember {
                                        derivedStateOf {
                                            stanPrzesuniecia.maxValue > 0 && stanPrzesuniecia.value < stanPrzesuniecia.maxValue
                                        }
                                    }
                                    val pokazGornyPrzyciskPrzewijania by remember {
                                        derivedStateOf {
                                            stanPrzesuniecia.maxValue > 0 && stanPrzesuniecia.value > 0
                                        }
                                    }
                                    Column {
                                        AnimatedVisibility(
                                            visible = pokazDolnyPrzyciskPrzewijania,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            PlywajacyPrzyciskWDol(stanPrzesuniecia = stanPrzesuniecia)
                                        }
                                    }
                                    Column {
                                        AnimatedVisibility(
                                            visible = pokazGornyPrzyciskPrzewijania,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            PlywajacyPrzyciskWGore(stanPrzesuniecia = stanPrzesuniecia)
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
                                            homeViewModel.dodajKontener() // Wywołanie metody ViewModelu
                                            // Usuń wywołanie dodajKontener() z funkcji pomocniczej
                                            // Usuń wywołanie zapiszDaneKontenerow() stąd, bo zapis jest w ViewModelu
                                            spowodujSlabaWibracje(aktywnosc)
                                            zakresKorutyn.launch {
                                                snapshotFlow { stanPrzesuniecia.maxValue }
                                                    .collect { maksymalnaWartosc ->
                                                        stanPrzesuniecia.animateScrollTo(maksymalnaWartosc)
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
                                            kontrolerNawigacji.navigate(Nawigacja.UlubioneWaluty.route)
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

    LaunchedEffect(bladSieci) {
        if (bladSieci && !czyWidocznySnackbar) {
            czyWidocznySnackbar = true
            czyPokazanyBladSieci = true
            zakres.launch {
                val result = stanSnackbara.showSnackbar(
                    message = "Brak połączenia z siecią"
                )
                if (result == SnackbarResult.ActionPerformed) {
                    bladSieci = false
                }
                czyWidocznySnackbar = false
            }
        } else if (!bladSieci && czyPokazanyBladSieci) {
            czyPokazanyBladSieci = false
            stanSnackbara.showSnackbar(message = "Połączenie z siecią zostało przywrócone")
        }
    }
}
fun czyPoziomo(aktywnosc: Activity): Boolean {
    val konfiguracja = aktywnosc.resources.configuration
    return konfiguracja.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun czyTelefon(aktywnosc: Activity): Boolean {
    val konfiguracja = aktywnosc.resources.configuration
    return konfiguracja.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_NORMAL
}