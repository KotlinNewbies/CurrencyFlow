package com.example.currencyflow.ui.components

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.currencyflow.R
import com.example.currencyflow.data.model.C
import com.example.currencyflow.data.model.Waluta
import com.example.currencyflow.util.haptics.spowodujPodwojnaSilnaWibracje
import com.example.currencyflow.util.haptics.spowodujSilnaWibracje
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SuspiciousIndentation")
@Composable
fun PojedynczyKontenerWalutyUI(
    modifier: Modifier = Modifier,
    kontener: C,
    onKontenerChanged: (updatedKontener: C) -> Unit,
    zdarzenieUsunieciaKontenera: () -> Unit,
    context: Context,
    wybraneWaluty: List<Waluta>,
    canBeSwipedToDelete: Boolean
) {
    val zakres =
        rememberCoroutineScope()
    val wzorPolaTekstowego =
        remember { "^[0-9]*\\.?[0-9]*\$".toRegex() }

    LaunchedEffect(kontener.id, kontener.amount) {
        if (kontener.amount.isEmpty() && kontener.result.isNotEmpty()) {
            Log.d(TAG, "ID: ${kontener.id} - Amount is empty. Clearing result.")
            onKontenerChanged(kontener.copy(result = ""))
        }
    }

    var widocznoscDlaAnimacjiSwipe by remember(kontener.id) { mutableStateOf(true) }
    val currentDensity = LocalDensity.current // Pobierz aktualną gęstość
    val dismissState = remember(kontener.id, canBeSwipedToDelete) {
        Log.d("DismissStateRecreation", "Tworzę/Resetuję SwipeToDismissBoxState dla ID: ${kontener.id}, canBeSwipedToDelete: $canBeSwipedToDelete")
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            density = currentDensity,
            confirmValueChange = { dismissValue ->
                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                    Log.d(
                        "SwipeConfirmChange",
                        "Kontener ID: ${kontener.id}, WEWNĄTRZ confirmValueChange - canBeSwipedToDelete: $canBeSwipedToDelete. Zwracam: ${if (!canBeSwipedToDelete) "FALSE (blokuję)" else "TRUE (pozwalam)"}"
                    )
                    if (!canBeSwipedToDelete) {
                        spowodujPodwojnaSilnaWibracje(context)
                        false // Nie zezwalaj na swipe
                    } else {
                        widocznoscDlaAnimacjiSwipe = false
                        zakres.launch {
                            spowodujSilnaWibracje(context)
                            delay(400)
                            Log.d("SwipeDebug", "Wywołuję zdarzenieUsunieciaKontenera dla ID: ${kontener.id}")
                            zdarzenieUsunieciaKontenera()
                        }
                        true // Pozwól na swipe
                    }
                } else {
                    Log.d("SwipeConfirmChange", "Kontener ID: ${kontener.id}, dismissValue != EndToStart. Zwracam: false")
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
        targetValue = katObrotu,
        animationSpec = tween(durationMillis = 500),
        label = "SwapIconRotation_${kontener.id}"
    )

    AnimatedVisibility(
        visible = widocznoscDlaAnimacjiSwipe,
        exit = fadeOut(animationSpec = tween(durationMillis = 300, delayMillis = 100)) + shrinkVertically(animationSpec = tween(durationMillis = 300, delayMillis = 100)), // Przykład
        modifier = modifier
    ) {
        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp)),
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true, // lub canBeSwipedToDelete, jeśli ma to kontrolować
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
                    val itemInteractionSource = remember { MutableInteractionSource() }
                    BoxWithConstraints {
                        // Określenie parametrów na podstawie maxWidth
                        val (amountTextFieldWeight, resultTextFieldWeight, currentFontSize) = when {
                            maxWidth < 600.dp -> Triple(0.70f, 0.65f, 26.sp)
                            maxWidth < 840.dp -> Triple(0.75f, 0.75f, 30.sp)
                            else -> Triple(0.80f, 0.80f, 30.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(), // Ten Row jest teraz głównym układem dla dwóch CurrencyRowInput i ikony
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center // lub SpaceBetween, jeśli ikona ma być rozciągnięta
                        ) {
                            CurrencyRowInput(
                                modifier = Modifier.weight(1f),
                                label = "Amount",
                                kontenerId = kontener.id,
                                value = kontener.amount,
                                onValueChange = { nowaWartosc ->
                                    onKontenerChanged(kontener.copy(amount = nowaWartosc))
                                },
                                isEnabled = true,
                                textFieldWeight = amountTextFieldWeight,
                                fontSize = currentFontSize,
                                regexPattern = wzorPolaTekstowego,
                                selectedCurrency = kontener.from,
                                onCurrencySelected = { nowoWybranaWalutaDlaFrom ->
                                    onKontenerChanged(kontener.copy(from = nowoWybranaWalutaDlaFrom))
                                },
                                availableCurrencies = wybraneWaluty
                            )

                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                                contentDescription = null, // Dodaj opis, jeśli potrzebny dla dostępności
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(52.dp) // Rozważ użycie .padding() wokół ikony zamiast sztywnego rozmiaru, jeśli potrzebujesz elastyczności
                                    .graphicsLayer(rotationZ = zanimowanieKataObrotu)
                                    .clickable(
                                        interactionSource = itemInteractionSource,
                                        indication = null, // Rozważ dodanie LocalIndication.current dla domyślnego ripple
                                        onClick = {
                                            katObrotu += 180f
                                            onKontenerChanged(
                                                kontener.copy(
                                                    from = kontener.to,
                                                    to = kontener.from,
                                                )
                                            )
                                        }
                                    )
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