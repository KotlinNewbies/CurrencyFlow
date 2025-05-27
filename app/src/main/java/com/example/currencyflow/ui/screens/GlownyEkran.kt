package com.example.currencyflow.ui.screens

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.ui.navigation.Nawigacja
import com.example.currencyflow.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import com.example.currencyflow.util.haptics.spowodujSlabaWibracje
import com.example.currencyflow.ui.components.PlywajacyPrzyciskWDol
import com.example.currencyflow.ui.components.PlywajacyPrzyciskWGore
import com.example.currencyflow.ui.components.PojedynczyKontenerWalutyUI
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

private val czcionkaPacificoRegular = FontFamily(
    Font(R.font.pacifico_regular, FontWeight.Bold)
)

@Composable
fun GlownyEkran(
    homeViewModel: HomeViewModel = hiltViewModel(), // Używaj tej instancji dostarczonej przez Hilt
    aktywnosc: ComponentActivity,
    kontrolerNawigacji: NavController,
) {

    // Snackbar
    val stanSnackbara = remember { SnackbarHostState() }
    val snackbarMessage by homeViewModel.snackbarMessage.collectAsState()
    // Obserwujemy stany z NOWEGO HomeViewModel
    val konteneryUI by homeViewModel.konteneryUI.collectAsState()
    val dostepneWalutyDlaKontenerow by homeViewModel.dostepneWalutyDlaKontenerow.collectAsState()
    val czyLadowanie by homeViewModel.czyLadowanieKursow.collectAsState()

    val canDeleteAnyContainer by homeViewModel.canDeleteAnyContainer.collectAsStateWithLifecycle()


    // scrollowanie
    val stanListy = rememberLazyListState() // Dla LazyColumn
    val zakresKorutyn = rememberCoroutineScope()

    val isViewModelInitialized by homeViewModel.isInitialized.collectAsStateWithLifecycle() // Użyj collectAsStateWithLifecycle

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(isViewModelInitialized, lifecycleOwner) { // Klucz: isViewModelInitialized
        // Ten blok uruchomi się, gdy isViewModelInitialized się zmieni LUB lifecycleOwner
        // Dla pewności, że reagujemy na RESUMED, zostawiamy repeatOnLifecycle
        if (isViewModelInitialized) {
            Log.d("GlownyEkran", "ViewModel is initialized. Setting up RESUMED listener for odswiezDostepneWaluty.")
            // Uruchom logikę odświeżania DOPIERO GDY ViewModel jest gotowy
            // I tylko gdy ekran jest RESUMED
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                // Ten blok korutyny będzie aktywny tylko gdy jesteśmy w RESUMED
                // i ViewModel jest zainicjalizowany
                val currentRoute = kontrolerNawigacji.currentBackStackEntry?.destination?.route
                if (currentRoute == Nawigacja.Dom.route) {
                    Log.i("GlownyEkran", "RESUMED and on Dom route. Calling odswiezDostepneWaluty.")
                    homeViewModel.odswiezDostepneWaluty()
                } else {
                    Log.d("GlownyEkran", "RESUMED but not on Dom route ($currentRoute). Not calling odswiezDostepneWaluty.")
                }
            }
        } else {
            Log.d("GlownyEkran", "ViewModel not yet initialized. Waiting to set up RESUMED listener.")
        }
    }
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            stanSnackbara.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short // Możesz dać .Long dla ważniejszych komunikatów
            )
            homeViewModel.snackbarMessageShown() // Resetuj wiadomość w ViewModelu
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
                LazyColumn(
                    state = stanListy,
                    modifier = Modifier.fillMaxWidth(), // lub inny odpowiedni modifier
                    contentPadding = PaddingValues(vertical = 8.dp) // Opcjonalny padding dla całej listy
                ) {
                    itemsIndexed(
                        items = konteneryUI,
                        key = { _, itemC -> itemC.id } // Klucz dla stabilności i wydajności LazyColumn
                    ) { _, pojedynczyKontener -> // 'index' jest dostępny, jeśli go potrzebujesz
                        PojedynczyKontenerWalutyUI(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                                .animateItem(), // Animacja przy zmianach listy
                            kontener = pojedynczyKontener,      // << Pojedynczy element z listy
                            onKontenerChanged = { zaktualizowanyKontener ->
                                // Zakładając, że Twoja klasa C ma unikalne 'id'
                                // i ViewModel zaktualizuje go na podstawie tego ID.
                                // Twój ViewModel.zaktualizujKontenerIPrzelicz przyjmuje (id, zaktualizowanyKontener)
                                homeViewModel.zaktualizujKontenerIPrzelicz(
                                    pojedynczyKontener.id, // Przekazujesz ID oryginalnego kontenera
                                    zaktualizowanyKontener  // Przekazujesz cały obiekt zaktualizowanego kontenera
                                )
                            },
                            zdarzenieUsunieciaKontenera = {
                                Log.d(
                                    "GlownyEkranLazyColumn",
                                    "Zdarzenie usunięcia dla ID: ${pojedynczyKontener.id}"
                                )
                                // Przekazujesz ID kontenera, który ma zostać usunięty
                                homeViewModel.usunKontenerPoId(pojedynczyKontener.id)
                            },
                            context = aktywnosc, // Lub LocalContext.current
                            wybraneWaluty = dostepneWalutyDlaKontenerow,
                            canBeSwipedToDelete = canDeleteAnyContainer // << NOWA FLAGA
                        )
                    }
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
                                    val layoutInfo = stanListy.layoutInfo
                                    val calkowitaLiczbaElementow = layoutInfo.totalItemsCount
                                    if (calkowitaLiczbaElementow == 0) return@derivedStateOf false
                                    val ostatniWidocznyElement = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                                    ostatniWidocznyElement != null && ostatniWidocznyElement < calkowitaLiczbaElementow - 1
                                }
                            }
                            val pokazGornyPrzyciskPrzewijania by remember {
                                derivedStateOf {
                                    val layoutInfo = stanListy.layoutInfo
                                    val calkowitaLiczbaElementow = layoutInfo.totalItemsCount
                                    if (calkowitaLiczbaElementow == 0) return@derivedStateOf false
                                    val pierwszyWidocznyElement = layoutInfo.visibleItemsInfo.firstOrNull()?.index
                                    pierwszyWidocznyElement != null && pierwszyWidocznyElement > 0
                                }
                            }

                            Column {
                                AnimatedVisibility(
                                    visible = pokazDolnyPrzyciskPrzewijania,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    PlywajacyPrzyciskWDol(coroutineScope = zakresKorutyn, lazyListState = stanListy)
                                }
                            }

                            Column {
                                AnimatedVisibility(
                                    visible = pokazGornyPrzyciskPrzewijania,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    PlywajacyPrzyciskWGore(coroutineScope = zakresKorutyn, lazyListState = stanListy)
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
                                    spowodujSlabaWibracje(aktywnosc)
                                    zakresKorutyn.launch {
                                        snapshotFlow { konteneryUI.size }
                                            .distinctUntilChanged() // Reaguj tylko na zmianę rozmiaru
                                            .collectLatest { size -> // Użyj collectLatest, aby anulować poprzednie, jeśli szybko klikamy
                                                if (size > 0) {
                                                    stanListy.animateScrollToItem(size - 1) // Przewiń do ostatniego elementu
                                                }
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
                                    // Logika dla przycisku "przewiń na dół"
                                    val pokazDolnyPrzyciskPrzewijania by remember {
                                        derivedStateOf {
                                            val layoutInfo = stanListy.layoutInfo
                                            val calkowitaLiczbaElementow = layoutInfo.totalItemsCount
                                            if (calkowitaLiczbaElementow == 0) return@derivedStateOf false
                                            val ostatniWidocznyElement = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                                            ostatniWidocznyElement != null && ostatniWidocznyElement < calkowitaLiczbaElementow - 1
                                        }
                                    }

                                    // Logika dla przycisku "przewiń na górę"
                                    val pokazGornyPrzyciskPrzewijania by remember {
                                        derivedStateOf {
                                            val layoutInfo = stanListy.layoutInfo
                                            val calkowitaLiczbaElementow = layoutInfo.totalItemsCount
                                            if (calkowitaLiczbaElementow == 0) return@derivedStateOf false
                                            val pierwszyWidocznyElement = layoutInfo.visibleItemsInfo.firstOrNull()?.index
                                            pierwszyWidocznyElement != null && pierwszyWidocznyElement > 0
                                        }
                                    }
                                    Column {
                                        AnimatedVisibility(
                                            visible = pokazDolnyPrzyciskPrzewijania,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            PlywajacyPrzyciskWDol(coroutineScope = zakresKorutyn, lazyListState = stanListy)
                                        }
                                    }
                                    Column {
                                        AnimatedVisibility(
                                            visible = pokazGornyPrzyciskPrzewijania,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            PlywajacyPrzyciskWGore(coroutineScope = zakresKorutyn, lazyListState = stanListy)
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
                                                snapshotFlow { konteneryUI.size }
                                                    .distinctUntilChanged()
                                                    .collectLatest { size ->
                                                        if (size > 0) {
                                                            stanListy.animateScrollToItem(size - 1)
                                                        }
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
}

fun czyPoziomo(aktywnosc: Activity): Boolean {
    val konfiguracja = aktywnosc.resources.configuration
    return konfiguracja.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun czyTelefon(aktywnosc: Activity): Boolean {
    val konfiguracja = aktywnosc.resources.configuration
    return konfiguracja.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_NORMAL
}