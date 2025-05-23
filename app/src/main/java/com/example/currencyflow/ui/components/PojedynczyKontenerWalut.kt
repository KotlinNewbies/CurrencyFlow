package com.example.currencyflow.ui.components

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.currencyflow.R
import com.example.currencyflow.data.model.C
import com.example.currencyflow.data.model.Waluta
import com.example.currencyflow.util.haptics.spowodujPodwojnaSilnaWibracje
import com.example.currencyflow.util.haptics.spowodujSilnaWibracje
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.text.matches

@SuppressLint("SuspiciousIndentation") // Jeśli nadal potrzebne
@Composable
fun PojedynczyKontenerWalutyUI(
    modifier: Modifier = Modifier,
    kontener: C,
    onKontenerChanged: (updatedKontener: C) -> Unit,
    zdarzenieUsunieciaKontenera: () -> Unit,
    context: Context, // Zmienione z ComponentActivity na Context, bo to przekazujesz
    wybraneWaluty: List<Waluta>,       // <<<< Upewnij się, że ten parametr istnieje
    canBeSwipedToDelete: Boolean       // <<<< Upewnij się, że ten parametr istnieje
) {
    val zakres =
        rememberCoroutineScope() // Ten zakres jest OK, jeśli jest tylko dla logiki SwipeToDismiss
    val wzorPolaTekstowego =
        remember { "^[0-9]*\\.?[0-9]*\$".toRegex() } // `remember` dla wydajności

    LaunchedEffect(kontener.id, kontener.amount) {
        if (kontener.amount.isEmpty() && kontener.result.isNotEmpty()) {
            Log.d(TAG, "ID: ${kontener.id} - Amount is empty. Clearing result.")
            onKontenerChanged(kontener.copy(result = ""))
        }
    }

    var widocznoscDlaAnimacjiSwipe by remember(kontener.id) { mutableStateOf(true) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                if (!canBeSwipedToDelete) { // Dodaj tę weryfikację
                    spowodujPodwojnaSilnaWibracje(context) // Tak jak miałeś wcześniej
                    return@rememberSwipeToDismissBoxState false // Nie zezwalaj na swipe
                }

                widocznoscDlaAnimacjiSwipe = false // Jeśli używasz AnimatedVisibility do animacji zniknięcia
                zakres.launch {
                    spowodujSilnaWibracje(context)
                    delay(400)
                    Log.d(
                        "SwipeDebug",
                        "Wywołuję zdarzenieUsunieciaKontenera dla ID: ${kontener.id}"
                    )
                    zdarzenieUsunieciaKontenera()
                }
                true
            } else {
                false
            }
        }
    )

    AnimatedVisibility(
        visible = widocznoscDlaAnimacjiSwipe,
        exit = fadeOut(animationSpec = tween(durationMillis = 300, delayMillis = 100)) + shrinkVertically(animationSpec = tween(durationMillis = 300, delayMillis = 100)), // Przykład
        modifier = modifier // WAŻNE: Przekaż modifier z LazyColumn tutaj!
    ) {
        SwipeToDismissBox(
            state = dismissState,
            // Modifier.fillMaxWidth().clip() powinien być tutaj, jeśli nie jest na AnimatedVisibility
            // ALE PAMIĘTAJ, że modifier z LazyColumn (zawierający .animateItem()) musi być zastosowany
            // do najbardziej zewnętrznego elementu, który LazyColumn "widzi".
            // Czyli jeśli AnimatedVisibility jest na zewnątrz, to on dostaje ten główny modifier.
            // Jeśli SwipeToDismissBox jest na zewnątrz, to on.
            modifier = Modifier // Ten modifier jest dla SwipeToDismissBox, jeśli AnimatedVisibility jest wyżej
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp)),
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true, // lub canBeSwipedToDelete, jeśli ma to kontrolować
            backgroundContent = {
                // Twoja logika backgroundContent (czerwone tło z ikoną kosza)
                // ... tak jak miałeś ...
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
                        if (maxWidth < 600.dp) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.05f)
                                    )
                                    BasicTextField(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.background)
                                            .weight(0.70f)
                                            .fillMaxHeight(),
                                        value = kontener.amount,
                                        onValueChange = { nowaWartosc ->
                                            if (nowaWartosc.matches(wzorPolaTekstowego) || nowaWartosc.isEmpty()) {
                                                onKontenerChanged(kontener.copy(amount = nowaWartosc))
                                            }
                                        },
                                        textStyle = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 26.sp // Ustawienie rozmiaru czcionki
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        maxLines = 1,
                                        singleLine = true,
                                        enabled = true,
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                    )
                                    Crossfade(
                                        targetState = kontener.from,
                                        label = "CurrencyChangeFrom_${kontener.id}"
                                    ) { walutaWejsciowa ->
                                        RozwijaneMenu(
                                            wybranaWaluta = walutaWejsciowa,
                                            zdarzenieWybraniaWaluty = { nowoWybranaWalutaDlaFrom ->
                                                onKontenerChanged(kontener.copy(from = nowoWybranaWalutaDlaFrom))
                                            },
                                            wybraneWaluty = wybraneWaluty
                                        )
                                    }
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(0.03f)
                                    )
                                }
                                // Stan przechowujący kąt obrotu
                                var katObrotu by remember { mutableFloatStateOf(0f) }

                                // Animacja obrotu o 180 stopni
                                val zanimowanieKataObrotu by animateFloatAsState(
                                    targetValue = katObrotu,
                                    animationSpec = tween(durationMillis = 500), // Czas trwania animacji
                                    label = ""
                                )

                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .graphicsLayer(rotationZ = zanimowanieKataObrotu) // Zastosowanie animacji obrotu
                                        .clickable(
                                            interactionSource = itemInteractionSource,
                                            indication = null,
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
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.05f)
                                    )
                                    BasicTextField(
                                        modifier = Modifier
                                            .weight(0.65f)
                                            .background(MaterialTheme.colorScheme.background),
                                        value = kontener.result, // Wynik jest teraz wyświetlany bezpośrednio z obiektu C
                                        onValueChange = { /* Pole wyniku jest tylko do odczytu */ },
                                        textStyle = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 26.sp
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Mimo że disabled, warto zachować
                                        maxLines = 1,
                                        singleLine = true,
                                        enabled = false, // Pole wyniku jest zazwyczaj nieedytowalne przez użytkownika bezpośrednio
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                    )

                                    Crossfade(
                                        targetState = kontener.to,
                                        label = "CurrencyChangeTo_${kontener.id}"
                                    ) { walutaWyjsciowa ->
                                        RozwijaneMenu(
                                            wybranaWaluta = walutaWyjsciowa,
                                            zdarzenieWybraniaWaluty = { nowoWybranaWalutaDlaTo ->
                                                Log.d(TAG, "KontenerWalut (ID: ${kontener.id}) - RozwijaneMenu TO - nowa waluta: $nowoWybranaWalutaDlaTo.")
                                                onKontenerChanged(
                                                    kontener.copy(to = nowoWybranaWalutaDlaTo)
                                                )
                                            },
                                            wybraneWaluty = wybraneWaluty
                                        )
                                    }
                                }
                            }
                        } else if (maxWidth >= 600.dp && maxWidth < 840.dp) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.05f)
                                    )
                                    BasicTextField(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.background)
                                            .weight(0.75f)
                                            .fillMaxHeight(),
                                        value = kontener.amount,
                                        onValueChange = { nowaWartosc ->
                                            if (nowaWartosc.matches(wzorPolaTekstowego) || nowaWartosc.isEmpty()) { // Zezwól na puste pole                                                    Log.d(TAG, "KontenerWalut (index $index) - BasicTextField FROM - wzorzec PASUJE. Wywołuję zdarzenieZmianyWartosci.")
                                                onKontenerChanged(
                                                    kontener.copy(amount = nowaWartosc)
                                                )
                                            }
                                        },
                                        textStyle = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 30.sp
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        maxLines = 1,
                                        singleLine = true,
                                        enabled = true,
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.03f)
                                            .background(Color.Transparent)
                                    )
                                    Crossfade(
                                        targetState = kontener.from,
                                        label = "CurrencyChangeFrom_${kontener.id}"
                                    ) { walutaWejsciowa ->
                                        RozwijaneMenu(
                                            wybranaWaluta = walutaWejsciowa,
                                            zdarzenieWybraniaWaluty = { nowoWybranaWalutaDlaFrom ->
                                                onKontenerChanged(
                                                    kontener.copy(from = nowoWybranaWalutaDlaFrom)
                                                )
                                            },
                                            wybraneWaluty = wybraneWaluty
                                        )
                                    }
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.03f)
                                            .background(Color.Transparent)
                                    )

                                }

                                // Stan przechowujący kąt obrotu
                                var katObrotu by remember { mutableFloatStateOf(0f) }

                                // Animacja obrotu o 180 stopni
                                val zanimowanieKataObrotu by animateFloatAsState(
                                    targetValue = katObrotu,
                                    animationSpec = tween(durationMillis = 500),
                                    label = ""
                                )

                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .graphicsLayer(rotationZ = zanimowanieKataObrotu) // Zastosowanie animacji obrotu
                                        .clickable(
                                            interactionSource = itemInteractionSource,
                                            indication = null, // Można dodać domyślną indykację Ripple
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
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.05f)
                                    )
                                    BasicTextField(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.background)
                                            .weight(0.75f)
                                            .fillMaxHeight(),
                                        value = kontener.result, // Wynik jest teraz wyświetlany bezpośrednio z obiektu C
                                        onValueChange = { /* Pole wyniku jest tylko do odczytu */ },
                                        textStyle = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 30.sp
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        maxLines = 1,
                                        singleLine = true,
                                        enabled = false,
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.03f)
                                    )
                                    Crossfade(
                                        targetState = kontener.to,
                                        label = "CurrencyChangeTo_${kontener.id}"
                                    ) { walutaWyjsciowa ->
                                        RozwijaneMenu(
                                            wybranaWaluta = walutaWyjsciowa,
                                            zdarzenieWybraniaWaluty = { nowoWybranaWalutaDlaTo ->
                                                onKontenerChanged(
                                                    kontener.copy(to = nowoWybranaWalutaDlaTo)
                                                )
                                            },
                                            wybraneWaluty = wybraneWaluty
                                        )
                                    }
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(0.03f)
                                    )
                                }
                            }
                        } else if (maxWidth > 840.dp) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.05f)
                                    )
                                    BasicTextField(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.background)
                                            .weight(0.80f)
                                            .fillMaxHeight(),
                                        value = kontener.amount,
                                        onValueChange = { nowaWartosc ->
                                            if (nowaWartosc.matches(wzorPolaTekstowego) || nowaWartosc.isEmpty()) { // Zezwól na puste pole                                                    Log.d(TAG, "KontenerWalut (index $index) - BasicTextField FROM - wzorzec PASUJE. Wywołuję zdarzenieZmianyWartosci.")
                                                onKontenerChanged(
                                                     kontener.copy(amount = nowaWartosc)
                                                )
                                                //zdarzenieZapisuDanych()
                                            }
                                        },
                                        textStyle = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 30.sp
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        maxLines = 1,
                                        singleLine = true,
                                        enabled = true,
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.03f)
                                            .background(Color.Transparent)
                                    )
                                    Crossfade(
                                        targetState = kontener.from,
                                        label = "CurrencyChange"
                                    ) { walutaWyjsciowa ->
                                        RozwijaneMenu(
                                            wybranaWaluta = walutaWyjsciowa,
                                            zdarzenieWybraniaWaluty = { nowoWybranaWalutaDlaFrom ->
                                                onKontenerChanged(
                                                    kontener.copy(from = nowoWybranaWalutaDlaFrom)
                                                )
                                            },
                                            wybraneWaluty = wybraneWaluty
                                        )
                                    }
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.03f)
                                            .background(Color.Transparent)
                                    )

                                }

                                // Stan przechowujący kąt obrotu
                                var katObrotu by remember { mutableFloatStateOf(0f) }

                                // Animacja obrotu o 180 stopni
                                val zanimowanieKataObrotu by animateFloatAsState(
                                    targetValue = katObrotu,
                                    animationSpec = tween(durationMillis = 500),
                                    label = "" // Czas trwania animacji
                                )

                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .graphicsLayer(rotationZ = zanimowanieKataObrotu) // Zastosowanie animacji obrotu
                                        .clickable(
                                            interactionSource = itemInteractionSource,
                                            indication = null, // Można dodać domyślną indykację Ripple
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
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.05f)
                                    )
                                    BasicTextField(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.background)
                                            .weight(0.80f)
                                            .fillMaxHeight(),
                                        value = kontener.result, // Wynik jest teraz wyświetlany bezpośrednio z obiektu C
                                        onValueChange = { /* Pole wyniku jest tylko do odczytu */ },
                                        textStyle = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 30.sp
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        maxLines = 1,
                                        singleLine = true,
                                        enabled = false,
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(0.03f)
                                    )
                                    Crossfade(
                                        targetState = kontener.to,
                                        label = "CurrencyChange"
                                    ) { walutaWyjsciowa ->
                                        RozwijaneMenu(
                                            wybranaWaluta = walutaWyjsciowa,
                                            zdarzenieWybraniaWaluty = { nowoWybranaWalutaDlaTo ->
                                                onKontenerChanged(
                                                    kontener.copy(to = nowoWybranaWalutaDlaTo)
                                                )
                                            },
                                            wybraneWaluty = wybraneWaluty
                                        )
                                    }
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(0.03f)
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