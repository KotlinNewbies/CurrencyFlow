package com.fluida.currencyflow.ui.components

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluida.currencyflow.R
import com.fluida.currencyflow.data.model.C
import com.fluida.currencyflow.data.model.Waluta
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import com.fluida.currencyflow.ui.tutorial.TutorialStep
import com.fluida.currencyflow.util.haptics.spowodujPodwojnaSilnaWibracje
import com.fluida.currencyflow.util.haptics.spowodujSilnaWibracje
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("SuspiciousIndentation")
@Composable
fun PojedynczyKontenerWalutyUI(
    modifier: Modifier = Modifier,
    kontener: C,
    onKontenerChanged: (updatedKontener: C) -> Unit,
    zdarzenieUsunieciaKontenera: () -> Unit,
    context: Context,
    wybraneWaluty: List<Waluta>,
    canBeSwipedToDelete: Boolean,
    isEditMode: Boolean = false,
    onToggleEditMode: () -> Unit = {},
    onMove: (dragAmount: Float) -> Unit = {},
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    isFirstContainer: Boolean = false,
    onReportPosition: (Rect, TutorialStep) -> Unit = { _, _ -> }
) {
    val zakres =
        rememberCoroutineScope()
    val wzorPolaTekstowego =
        remember { """^[0-9]*\.?[0-9]*$""".toRegex() }

    val currentIsEditMode = rememberUpdatedState(isEditMode)
    val currentOnToggleEditMode = rememberUpdatedState(onToggleEditMode)
    val currentOnMove = rememberUpdatedState(onMove)
    val currentOnDragStart = rememberUpdatedState(onDragStart)
    val currentOnDragEnd = rememberUpdatedState(onDragEnd)
    val currentKontener = rememberUpdatedState(kontener)
    val currentOnKontenerChanged = rememberUpdatedState(onKontenerChanged)

    LaunchedEffect(kontener.id, kontener.amount) {
        if (kontener.amount.isEmpty() && kontener.result.isNotEmpty()) {
            Log.d(TAG, "ID: ${kontener.id} - Amount is empty. Clearing result.")
            onKontenerChanged(kontener.copy(result = ""))
        }
    }

    var widocznoscDlaAnimacjiSwipe by remember(kontener.id) { mutableStateOf(true) }
    var isDeleteTriggered by remember(kontener.id) { mutableStateOf(false) }

    val currentDensity = LocalDensity.current // Pobierz aktualną gęstość
    val dismissState = remember(kontener.id, canBeSwipedToDelete) {
        Log.d("DismissStateRecreation", "Tworzę/Resetuję SwipeToDismissBoxState dla ID: ${kontener.id}, canBeSwipedToDelete: $canBeSwipedToDelete")
        @Suppress("DEPRECATION")
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            density = currentDensity,
            confirmValueChange = { dismissValue ->
                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                    if (isDeleteTriggered) return@SwipeToDismissBoxState false
                    
                    if (!canBeSwipedToDelete) {
                        spowodujPodwojnaSilnaWibracje(context)
                        zdarzenieUsunieciaKontenera() // Wywołaj, aby ViewModel mógł pokazać Snackbar
                        false // Nie zezwalaj na fizyczne usunięcie (swipe wróci na miejsce)
                    } else {
                        isDeleteTriggered = true
                        widocznoscDlaAnimacjiSwipe = false
                        zakres.launch {
                            spowodujSilnaWibracje(context)
                            delay(400.milliseconds)
                            Log.d("SwipeDebug", "Wywołuję zdarzenieUsunieciaKontenera dla ID: ${kontener.id}")
                            zdarzenieUsunieciaKontenera()
                        }
                        true // Pozwól na swipe
                    }
                } else {
                    false
                }
            },
            positionalThreshold = { totalDistance -> totalDistance * 0.5f }
            // Możesz potrzebować dodać positionalThreshold, jeśli go używałeś:
            // positionalThreshold = { totalDistance -> totalDistance * 0.5f } // Przykładowy próg
        )
    }
    var katObrotu by remember(kontener.id) { mutableFloatStateOf(0f) }
    val zanimowanieKataObrotu by animateFloatAsState(
        targetValue = katObrotu + if (isEditMode) 90f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "SwapIconRotation_${kontener.id}"
    )

    AnimatedVisibility(
        visible = widocznoscDlaAnimacjiSwipe,
        exit = fadeOut(animationSpec = tween(durationMillis = 300, delayMillis = 100)) + shrinkVertically(animationSpec = tween(durationMillis = 300, delayMillis = 100)), // Przykład
        modifier = modifier.onGloballyPositioned { coords ->
            if (isFirstContainer) {
                onReportPosition(coords.boundsInRoot(), TutorialStep.DELETE)
            }
        }
    ) {
        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp)),
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = canBeSwipedToDelete, // Aktywne tylko gdy można usuwać i NIE ma trybu edycji
            backgroundContent = {
                val color = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                    else -> Color.Transparent
                }
                val alignment = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.round_delete_24),
                            contentDescription = "Usuń",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BoxWithConstraints {
                        // Określenie parametrów na podstawie maxWidth
                        val (amountTextFieldWeight, resultTextFieldWeight, currentFontSize) = when {
                            maxWidth < 600.dp -> Triple(0.70f, 0.65f, 25.sp)
                            maxWidth < 840.dp -> Triple(0.75f, 0.75f, 29.sp)
                            else -> Triple(0.80f, 0.80f, 29.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(), // Ten Row jest teraz głównym układem dla dwóch CurrencyRowInput i ikony
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center // lub SpaceBetween, jeśli ikona ma być rozciągnięta
                        ) {
                            CurrencyRowInput(
                                modifier = Modifier
                                    .weight(1f)
                                    .onGloballyPositioned { coords ->
                                        if (isFirstContainer) {
                                            onReportPosition(coords.boundsInRoot(), TutorialStep.INPUT_FIELD)
                                        }
                                    },
                                label = "Amount",
                                kontenerId = kontener.id,
                                value = kontener.amount,
                                onValueChange = { nowaWartosc ->
                                    onKontenerChanged(kontener.copy(amount = nowaWartosc))
                                },
                                isEnabled = !isEditMode,
                                textFieldWeight = amountTextFieldWeight,
                                fontSize = currentFontSize,
                                regexPattern = wzorPolaTekstowego,
                                selectedCurrency = kontener.from,
                                onCurrencySelected = { nowoWybranaWalutaDlaFrom ->
                                    onKontenerChanged(kontener.copy(from = nowoWybranaWalutaDlaFrom))
                                },
                                availableCurrencies = wybraneWaluty
                            )

                            // Ujednolicona ikona obsługująca oba tryby gestów płynnie
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                                contentDescription = "Interakcja",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(52.dp)
                                    .onGloballyPositioned { coords ->
                                        if (isFirstContainer) {
                                            onReportPosition(coords.boundsInRoot(), TutorialStep.SWAP_DRAG)
                                        }
                                    }
                                    .pointerInput(kontener.id) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val down = awaitFirstDown(requireUnconsumed = false)
                                                
                                                if (currentIsEditMode.value) {
                                                    // TRYB EDYCJI: Przesuwanie natychmiastowe
                                                    currentOnDragStart.value()
                                                    verticalDrag(down.id) { change ->
                                                        val delta = change.positionChange().y
                                                        if (delta != 0f) {
                                                            change.consume()
                                                            currentOnMove.value(delta)
                                                        }
                                                    }
                                                    currentOnDragEnd.value()
                                                } else {
                                                    // TRYB ZWYKŁY: Czekaj na Tap lub LongPress
                                                    val up = withTimeoutOrNull(400) {
                                                        waitForUpOrCancellation()
                                                    }
                                                    
                                                    if (up != null) {
                                                        // TO BYŁ SZYBKI TAP -> Swap walut
                                                        katObrotu += 180f
                                                        val k = currentKontener.value
                                                        currentOnKontenerChanged.value(k.copy(from = k.to, to = k.from))
                                                    } else {
                                                        // TO BYŁ LONG PRESS -> Start edycji i magnetyczny drag
                                                        if (down.pressed) {
                                                            currentOnToggleEditMode.value()
                                                            currentOnDragStart.value()
                                                            verticalDrag(down.id) { change ->
                                                                val delta = change.positionChange().y
                                                                if (delta != 0f) {
                                                                    change.consume()
                                                                    currentOnMove.value(delta)
                                                                }
                                                            }
                                                            currentOnDragEnd.value()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .graphicsLayer(rotationZ = zanimowanieKataObrotu)
                            )

                            CurrencyRowInput(
                                modifier = Modifier.weight(1f),
                                label = "Result",
                                kontenerId = kontener.id,
                                value = kontener.result,
                                onValueChange = { /* Pole wyniku jest tylko do odczytu */ },
                                isEnabled = false,
                                textFieldWeight = resultTextFieldWeight,
                                fontSize = currentFontSize,
                                regexPattern = wzorPolaTekstowego, // Może nie być potrzebne, bo isEnabled=false
                                selectedCurrency = kontener.to,
                                onCurrencySelected = { nowoWybranaWalutaDlaTo ->
                                    onKontenerChanged(kontener.copy(to = nowoWybranaWalutaDlaTo))
                                },
                                availableCurrencies = wybraneWaluty
                            )
                        }
                    }
                }
            }
        }
    }
}