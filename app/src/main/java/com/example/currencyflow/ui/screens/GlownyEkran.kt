package com.example.currencyflow.ui.screens

import android.app.Activity
import android.content.res.Configuration
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.ui.navigation.Nawigacja
import com.example.currencyflow.viewmodel.HomeViewModel
import com.example.currencyflow.ui.components.KontenerWalut
import kotlinx.coroutines.launch
import com.example.currencyflow.util.haptics.spowodujSlabaWibracje
import com.example.currencyflow.ui.components.PlywajacyPrzyciskWDol
import com.example.currencyflow.ui.components.PlywajacyPrzyciskWGore

@Composable
fun GlownyEkran(
    homeViewModel: HomeViewModel = hiltViewModel(), // Używaj tej instancji dostarczonej przez Hilt
    aktywnosc: ComponentActivity,
    kontrolerNawigacji: NavController,
) {
    val bladPobieraniaKursow by homeViewModel.bladPobieraniaKursow.collectAsState()

    // Snackbar
    val stanSnackbara = remember { SnackbarHostState() }
    // Obserwujemy stany z NOWEGO HomeViewModel
    val konteneryUI by homeViewModel.konteneryUI.collectAsState()
    val dostepneWalutyDlaKontenerow by homeViewModel.dostepneWalutyDlaKontenerow.collectAsState()
    val czyLadowanie by homeViewModel.czyLadowanieKursow.collectAsState()

    val czcionkaPacificoRegular = FontFamily(
        Font(R.font.pacifico_regular, FontWeight.Bold)
    )

    // scrollowanie
    val stanPrzesuniecia = rememberScrollState()
    val zakresKorutyn = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) { // Uruchom raz przy wejściu do kompozycji GlownyEkran
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val currentRoute = kontrolerNawigacji.currentBackStackEntry?.destination?.route
            if (currentRoute == Nawigacja.Dom.route) {
                homeViewModel.odswiezDostepneWaluty()
            }
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
                                visible = czyLadowanie,
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
                        kontenery = konteneryUI, // Przekazanie listy kontenerów z HomeViewModel
                        onKontenerChanged = { index, zaktualizowanyKontener ->
                            homeViewModel.zaktualizujKontenerIPrzelicz(
                                index,
                                zaktualizowanyKontener
                            )
                        },
                        zdarzenieUsunieciaKontenera = { index ->
                            homeViewModel.usunKontener(index)
                        },
                        context = aktywnosc, // Przekazanie kontekstu
                        wybraneWaluty = dostepneWalutyDlaKontenerow, // Przekazanie listy dostępnych walut

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
                                                        stanPrzesuniecia.animateScrollTo(
                                                            maksymalnaWartosc
                                                        )
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


    // Nowy LaunchedEffect do obsługi Snackbarów na podstawie stanu błędu z ViewModelu
    LaunchedEffect(bladPobieraniaKursow) {
        bladPobieraniaKursow?.let { komunikatBledu ->
            zakresKorutyn.launch {
                val rezultat = stanSnackbara.showSnackbar(
                    message = komunikatBledu, // Użyj komunikatu błędu z ViewModelu
                    actionLabel = if (komunikatBledu.contains("Brak ID użytkownika")) null else "Odśwież", // Opcjonalnie: inny actionLabel w zależności od błędu
                    duration = SnackbarDuration.Long
                )
                if (rezultat == SnackbarResult.ActionPerformed) {
                    homeViewModel.odswiezKursyWalut() // Poprawne wywołanie, jeśli VM używa wewnętrznego ID
                }
                homeViewModel.wyczyscBladPobieraniaKursow()
            }
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