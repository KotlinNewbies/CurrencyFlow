package com.example.currencyflow.ui.screens

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.data.model.C
import com.example.currencyflow.ui.components.AdaptacyjnyBottomBar
import com.example.currencyflow.ui.navigation.Nawigacja
import com.example.currencyflow.viewmodel.HomeViewModel
import com.example.currencyflow.util.haptics.spowodujSlabaWibracje
import com.example.currencyflow.ui.components.PojedynczyKontenerWalutyUI

private val czcionkaPacificoRegular = FontFamily(
    Font(R.font.pacifico_regular, FontWeight.Bold)
)

@OptIn(ExperimentalMaterial3Api::class)
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

    val isViewModelInitialized by homeViewModel.isInitialized.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(isViewModelInitialized, lifecycleOwner) { // Klucz: isViewModelInitialized
        if (isViewModelInitialized) {
            Log.d(
                "GlownyEkran",
                "ViewModel is initialized. Setting up RESUMED listener for odswiezDostepneWaluty."
            )
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val currentRoute = kontrolerNawigacji.currentBackStackEntry?.destination?.route
                if (currentRoute == Nawigacja.Dom.route) {
                    Log.i("GlownyEkran", "RESUMED and on Dom route. Calling odswiezDostepneWaluty.")
                    homeViewModel.odswiezDostepneWaluty()
                } else {
                    Log.d(
                        "GlownyEkran",
                        "RESUMED but not on Dom route ($currentRoute). Not calling odswiezDostepneWaluty."
                    )
                }
            }
        } else {
            Log.d(
                "GlownyEkran",
                "ViewModel not yet initialized. Waiting to set up RESUMED listener."
            )
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
        snackbarHost = { SnackbarHost(hostState = stanSnackbara) },
        topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "CurrencyFlow",
                            fontFamily = czcionkaPacificoRegular,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 35.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                kontrolerNawigacji.navigate(Nawigacja.Ustawenia.route)
                            }
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.rounded_settings_b_roll_24
                                ),
                                contentDescription = "Ustawienia",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier.size(48.dp) // Rozważ 48dp dla spójności z IconButton
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = czyLadowanie,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(24.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
        bottomBar = {
            AdaptacyjnyBottomBar(
                homeViewModel = homeViewModel,
                stanListy = stanListy,
                zakresKorutyn = zakresKorutyn,
                spowodujSlabaWibracje = {
                    spowodujSlabaWibracje(context = aktywnosc)
                },
                navigateToUlubione = { kontrolerNawigacji.navigate(Nawigacja.UlubioneWaluty.route) },
                konteneryUISize = konteneryUI.size // Przekaż aktualny rozmiar listy
            )
        }
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
                    .weight(0.65f)
            ) {
                LazyColumn(
                    state = stanListy,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 25.dp),
                ) {
                    itemsIndexed(
                        items = konteneryUI, // Zakładamy, że konteneryUI to State<List<C>> lub podobnie stabilna lista
                        key = { _, itemC -> itemC.id } // Klucz dla stabilności i wydajności LazyColumn
                    ) { _, pojedynczyKontener -> // pojedynczyKontener to element C z listy
                        val currentKontenerId = pojedynczyKontener.id
                        val onItemChanged = remember(currentKontenerId, homeViewModel) {
                            { zaktualizowanyKontener: C ->
                                homeViewModel.zaktualizujKontenerIPrzelicz(
                                    currentKontenerId,
                                    zaktualizowanyKontener
                                )
                            }
                        }
                        val onItemDeleted = remember(currentKontenerId, homeViewModel) {
                            {
                                homeViewModel.usunKontenerPoId(currentKontenerId)
                            }
                        }

                        PojedynczyKontenerWalutyUI(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                                .animateItem(),
                            kontener = pojedynczyKontener,
                            onKontenerChanged = onItemChanged,
                            zdarzenieUsunieciaKontenera = onItemDeleted,
                            context = aktywnosc,
                            wybraneWaluty = dostepneWalutyDlaKontenerow,
                            canBeSwipedToDelete = canDeleteAnyContainer
                        )
                    }
                }
            }
        }
    }
}

