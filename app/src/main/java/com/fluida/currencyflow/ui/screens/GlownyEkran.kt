package com.fluida.currencyflow.ui.screens

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import com.fluida.currencyflow.R
import com.fluida.currencyflow.data.model.C
import com.fluida.currencyflow.ui.components.AdmobBanner
import com.fluida.currencyflow.ui.components.GlownyEkranBottomBar
import com.fluida.currencyflow.ui.navigation.Nawigacja
import com.fluida.currencyflow.viewmodel.HomeViewModel
import com.fluida.currencyflow.util.haptics.spowodujSilnaWibracje
import com.fluida.currencyflow.util.haptics.spowodujSlabaWibracje
import com.fluida.currencyflow.ui.components.PojedynczyKontenerWalutyUI
import com.fluida.currencyflow.ui.tutorial.TutorialOverlay
import com.fluida.currencyflow.ui.tutorial.TutorialStep
import androidx.compose.ui.layout.boundsInRoot

private val czcionkaPacificoRegular = FontFamily(
    Font(R.font.pacifico_regular, FontWeight.Bold)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlownyEkran(
    homeViewModel: HomeViewModel = hiltViewModel(),
    aktywnosc: ComponentActivity,
    kontrolerNawigacji: NavController,
) {

    // Obserwujemy stan UI z ViewModelu
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val tutorialState by homeViewModel.tutorialState.collectAsStateWithLifecycle()
    val snackbarMessage by homeViewModel.snackbarMessage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val stanSnackbara = remember { SnackbarHostState() }
    val stanListy = rememberLazyListState()
    val zakresKorutyn = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current


    LaunchedEffect(uiState.isInitialized, lifecycleOwner) {
        if (uiState.isInitialized) {
            Log.d("GlownyEkran", "ViewModel is initialized. Setting up RESUMED listener for odswiezDostepneWaluty.")
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val currentRoute = kontrolerNawigacji.currentBackStackEntry?.destination?.route
                if (currentRoute == Nawigacja.Dom.route) {
                    Log.i("GlownyEkran", "RESUMED and on Dom route. Waiting 300ms then calling odswiezDostepneWaluty.")
                    delay(300.milliseconds) // Dajemy moment na stabilizację UI
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
        snackbarMessage?.let { uiText ->
            stanSnackbara.showSnackbar(
                message = uiText.asString(context),
                duration = SnackbarDuration.Short
            )
            homeViewModel.snackbarMessageShown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = stanSnackbara) },
            containerColor = MaterialTheme.colorScheme.surface,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    kontrolerNawigacji.navigate(Nawigacja.Ustawenia.route)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = R.drawable.rounded_settings_24
                                    ),
                                    contentDescription = "Ustawienia",
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier.size(48.dp) // Rozważ 48dp dla spójności z IconButton
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = uiState.czyLadowanieKursow,
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AdmobBanner(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    )

                    GlownyEkranBottomBar(
                        homeViewModel = homeViewModel,
                        stanListy = stanListy,
                        zakresKorutyn = zakresKorutyn,
                        spowodujSlabaWibracje = { spowodujSlabaWibracje(context = aktywnosc) },
                        navigateToUlubione = { kontrolerNawigacji.navigate(Nawigacja.UlubioneWaluty.route) },
                        konteneryUISize = uiState.konteneryUI.size,
                        onReportPosition = { rect ->
                            homeViewModel.updateTutorialHighlight(rect, TutorialStep.BOTTOM_ACTIONS)
                        }
                    )
                }
            }
        ) { wypelnienieZawartosci ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(wypelnienieZawartosci),
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
                        userScrollEnabled = !uiState.isEditMode // Blokujemy przewijanie listy, aby nie walczyło z przesuwaniem
                    ) {
                        itemsIndexed(
                            items = uiState.konteneryUI,
                            key = { _, itemC -> itemC.id }
                        ) { index, pojedynczyKontener ->
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

                            // Bardziej zaawansowana logika przesuwania
                            var accumulatedDrag by remember(currentKontenerId) { mutableFloatStateOf(0f) }
                            var isDraggingThisItem by remember(currentKontenerId) { mutableStateOf(false) }
                            var lastSwapTime by remember { mutableLongStateOf(0L) }

                            val itemScale by animateFloatAsState(
                                targetValue = if (isDraggingThisItem) 1.05f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "ItemScale_$currentKontenerId"
                            )
                            
                            val itemElevation by animateFloatAsState(
                                targetValue = if (isDraggingThisItem) 16f else 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "ItemElevation_$currentKontenerId"
                            )
                            
                            val backgroundColor by animateColorAsState(
                                targetValue = androidx.compose.ui.graphics.Color.Transparent,
                                label = "ItemBgColor_$currentKontenerId"
                            )

                            PojedynczyKontenerWalutyUI(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                                    .zIndex(if (isDraggingThisItem) 1f else 0f)
                                    .graphicsLayer {
                                        scaleX = itemScale
                                        scaleY = itemScale
                                        shadowElevation = itemElevation
                                        shape = RoundedCornerShape(11.dp)
                                        clip = true
                                    }
                                    .background(backgroundColor, RoundedCornerShape(11.dp))
                                    .animateItem(),
                                kontener = pojedynczyKontener,
                                onKontenerChanged = onItemChanged,
                                zdarzenieUsunieciaKontenera = onItemDeleted,
                                context = aktywnosc,
                                wybraneWaluty = uiState.dostepneWalutyDlaKontenerow,
                                canBeSwipedToDelete = uiState.canDeleteAnyContainer && !uiState.isEditMode,
                                isEditMode = uiState.isEditMode,
                                onToggleEditMode = { homeViewModel.toggleEditMode() },
                                onDragStart = {
                                    isDraggingThisItem = true
                                    accumulatedDrag = 0f
                                    spowodujSilnaWibracje(aktywnosc)
                                },
                                onDragEnd = {
                                    isDraggingThisItem = false
                                    accumulatedDrag = 0f
                                    homeViewModel.zapiszKolejnoscPoPrzesunieciu()
                                    if (uiState.isEditMode) homeViewModel.toggleEditMode() // Automatyczne wyjście po puszczeniu
                                },
                                onMove = { dragAmount ->
                                    if (isDraggingThisItem) {
                                        val currentTime = System.currentTimeMillis()
                                        // Krótka blokada (100ms) dla stabilności przy wysokiej czułości
                                        if (currentTime - lastSwapTime < 100) return@PojedynczyKontenerWalutyUI
                                        
                                        accumulatedDrag += dragAmount
                                        val threshold = 150f // Zwiększony opór dla lepszej kontroli
                                        
                                        if (accumulatedDrag > threshold) {
                                            if (index < uiState.konteneryUI.lastIndex) {
                                                homeViewModel.moveContainerById(currentKontenerId, 1)
                                                accumulatedDrag = 0f 
                                                lastSwapTime = currentTime
                                                spowodujSlabaWibracje(aktywnosc)
                                            }
                                        } else if (accumulatedDrag < -threshold) {
                                            if (index > 0) {
                                                homeViewModel.moveContainerById(currentKontenerId, -1)
                                                accumulatedDrag = 0f 
                                                lastSwapTime = currentTime
                                                spowodujSlabaWibracje(aktywnosc)
                                            }
                                        }
                                    }
                                },
                                isFirstContainer = index == 0,
                                onReportPosition = { rect, step ->
                                    homeViewModel.updateTutorialHighlight(rect, step)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Warstwa samouczka - teraz na samym wierzchu, nad całą strukturą Scaffold
        TutorialOverlay(
            tutorialState = tutorialState,
            onNext = { homeViewModel.nextTutorialStep() },
            onDismiss = { homeViewModel.dismissTutorial() }
        )
    }
}
