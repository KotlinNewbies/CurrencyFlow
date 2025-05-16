package com.example.currencyflow.interfejs_uzytkownika.komponenty

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.currencyflow.klasy.Waluta
import com.example.currencyflow.dane.C
import com.example.currencyflow.dane.WalutyViewModel
import com.example.currencyflow.dane.przeliczKonwersjeWalutowe
import com.example.currencyflow.haptyka.spowodujPodwojnaSilnaWibracje
import com.example.currencyflow.haptyka.spowodujSilnaWibracje
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

private const val TAG = "KontenerWalut" // Dodaj TAG
@SuppressLint("SuspiciousIndentation")
@Composable
fun KontenerWalut(
    kontenery: List<C>,
    zdarzenieZmianyWartosci: (Int, String, String) -> Unit,
    zdarzenieZmianyWaluty: (Int, Waluta, Waluta) -> Unit,
    zdarzenieUsunieciaKontenera: (Int) -> Unit,
    context: Context,
    wybraneWaluty: List<Waluta>,
    walutyViewModel: WalutyViewModel,
    zdarzenieZapisuDanych: () -> Unit
) {
// Logowanie otrzymanych propsów
    Log.d(TAG, "KontenerWalut OTRZYMUJE: kontenery: ${kontenery.map { "(${it.from.symbol}-${it.to.symbol} amt:${it.amount})" }}")
    Log.d(TAG, "KontenerWalut OTRZYMUJE: wybraneWaluty (dostępne dla menu): ${wybraneWaluty.map { it.symbol }}")
    val zakres = rememberCoroutineScope()
    val wzorPolaTekstowego = "^[0-9]*\\.?[0-9]*\$".toRegex()
    val mnoznikiWalut by walutyViewModel.mapaWalut.collectAsState() // Obserwowanie kursów walut
    val zrodloInterakcji = remember { MutableInteractionSource() }
    Log.d(TAG, "KontenerWalut OTRZYMUJE: kontenery: ${kontenery.map { "(${it.from.symbol}-${it.to.symbol})" }}")
    Log.d(TAG, "KontenerWalut OTRZYMUJE: wybraneWaluty: ${wybraneWaluty.map { it.symbol }}") // Powinno być puste
    kontenery.forEachIndexed { index, c ->
        // Logowanie dla każdego kontenera i przekazywanych walut do RozwijaneMenu
        Log.d(TAG, "KontenerWalut (index $index): Przekazuję do RozwijaneMenu (from): wybranaWaluta=${c.from.symbol}, dostępneWaluty=${wybraneWaluty.map { it.symbol }}")
        Log.d(TAG, "KontenerWalut (index $index): Przekazuję do RozwijaneMenu (to): wybranaWaluta=${c.to.symbol}, dostępneWaluty=${wybraneWaluty.map { it.symbol }}")

        val czyPoleWejscioweWlaczone by remember { mutableStateOf(true) }
        var czyPoleWyjscioweWlaczone by remember { mutableStateOf(false) }

        LaunchedEffect(c.amount) {
            czyPoleWyjscioweWlaczone = c.amount.isNotEmpty()
            if (c.amount.isEmpty()) {
                zdarzenieZmianyWartosci(index, "", "") // czysznenie pola wyjsciowego kiedy wejsciowe jest puste
            }
        }
        LaunchedEffect(mnoznikiWalut) {
            // Przetwarzanie kontenerów i aktualizacja wyników
            val aktualizacjaKontenerow = przeliczKonwersjeWalutowe(mnoznikiWalut, kontenery)
            aktualizacjaKontenerow.forEachIndexed { index, aktualizacjaKontenera ->
                if (kontenery[index] != aktualizacjaKontenera) {
                    zdarzenieZmianyWartosci(index, aktualizacjaKontenera.amount, aktualizacjaKontenera.result)
                }
            }
        }
        var widocznosc by remember(index) { mutableStateOf(true) }
        val usun = SwipeAction(
            onSwipe = {
                if (kontenery.size > 1) {
                    widocznosc = false
                    zakres.launch {
                        spowodujSilnaWibracje(context)
                        delay(400)
                        widocznosc = true
                        zdarzenieUsunieciaKontenera(index)
                    }
                } else {
                    spowodujPodwojnaSilnaWibracje(context)
                }
            },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = R.drawable.round_delete_24
                    ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .size(30.dp)
                )
            },
            background = Color.Red
        )
        AnimatedVisibility(
            visible = widocznosc,
            enter = fadeIn(animationSpec = spring()) + expandVertically(),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 100,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            SwipeableActionsBox(
                //startActions = listOf(delete), // Akcje po lewej stronie
                endActions = listOf(usun), // Akcje po prawej stronie
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))

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
                                            value = c.amount,
                                            onValueChange = { nowaWartosc ->
                                                Log.d(TAG, "KontenerWalut (index $index) - BasicTextField FROM - onValueChange: '$nowaWartosc'")
                                                if (nowaWartosc.matches(wzorPolaTekstowego)) {
                                                    Log.d(TAG, "KontenerWalut (index $index) - BasicTextField FROM - wzorzec PASUJE. Wywołuję zdarzenieZmianyWartosci.")
                                                    zdarzenieZmianyWartosci(index, nowaWartosc, c.result)

                                                    // Automatyczne przeliczanie wartości po wprowadzeniu ilości
                                                    val aktualizacjaKontenrow =
                                                        przeliczKonwersjeWalutowe(
                                                            mnoznikiWalut,
                                                            kontenery
                                                        )
                                                    zdarzenieZmianyWartosci(
                                                        index,
                                                        aktualizacjaKontenrow[index].amount,
                                                        aktualizacjaKontenrow[index].result
                                                    )
                                                    zdarzenieZapisuDanych()
                                                }
                                            else {
                                            Log.d(TAG, "KontenerWalut (index $index) - BasicTextField FROM - wzorzec NIE PASUJE.")
                                        }
                                            },
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 26.sp // Ustawienie rozmiaru czcionki
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            maxLines = 1,
                                            singleLine = true,
                                            enabled = czyPoleWejscioweWlaczone,
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                        )
                                        Crossfade(targetState = c.from, label = "CurrencyChangeFrom") { walutaWejsciowa ->
                                            RozwijaneMenu(
                                                wybranaWaluta = walutaWejsciowa,
                                                zdarzenieWybraniaWaluty = { nowoWybranaWalutaDlaFrom ->
                                                    Log.d(TAG, "KontenerWalut (index $index) - ZMIANA WALUTY 'FROM':")
                                                    Log.d(TAG, "  Stara c.from: ${c.from.symbol}, Stara c.to: ${c.to.symbol}")
                                                    Log.d(TAG, "  Nowo wybrana dla 'from': ${nowoWybranaWalutaDlaFrom.symbol}")
                                                    Log.d(TAG, "  Wywołuję zdarzenieZmianyWaluty z: index=$index, from=${nowoWybranaWalutaDlaFrom.symbol}, to=${c.to.symbol}")

                                                    // TYLKO TO:
                                                    zdarzenieZmianyWaluty(index, nowoWybranaWalutaDlaFrom, c.to)
                                                    // NIE rób tutaj przeliczeń ani zdarzenieZmianyWartosci
                                                    // NIE rób tutaj zdarzenieZapisuDanych (niech HomeViewModel decyduje kiedy zapisać)
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
                                                interactionSource = zrodloInterakcji,
                                                indication = null
                                            ) {
                                                // Zmiana kąta obrotu o 180 stopni
                                                katObrotu += 180f

                                                // Zamiana walut "from" i "to"
                                                val nowaWalutaWejsciowa = c.to
                                                val nowaWalutaWyjsciowa = c.from
                                                zdarzenieZmianyWaluty(
                                                    index,
                                                    nowaWalutaWejsciowa,
                                                    nowaWalutaWyjsciowa
                                                )
//                                                val aktualizacjaKontenerow =
//                                                    przeliczKonwersjeWalutowe(
//                                                        mnoznikiWalut,
//                                                        kontenery
//                                                    )
//                                                zdarzenieZmianyWartosci(
//                                                    index,
//                                                    aktualizacjaKontenerow[index].amount,
//                                                    aktualizacjaKontenerow[index].result
//                                                )
//                                                zdarzenieZapisuDanych()
                                            }
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
                                                .weight(0.70f)
                                                .fillMaxHeight(),
                                            value = c.result,
                                            onValueChange = { nowaWartosc ->
                                                if (nowaWartosc.matches(wzorPolaTekstowego)) {
                                                    zdarzenieZmianyWartosci(index, c.amount, nowaWartosc)
                                                }
                                            },
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 26.sp
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            maxLines = 1,
                                            singleLine = true,
                                            enabled = false,
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                        )

                                        Crossfade(targetState = c.to, label = "CurrencyChangeTo") { walutaWyjsciowa ->
                                            RozwijaneMenu(
                                                wybranaWaluta = walutaWyjsciowa,
                                                zdarzenieWybraniaWaluty = { nowoWybranaWalutaDlaTo ->
                                                    Log.d(TAG, "KontenerWalut (index $index) - ZMIANA WALUTY 'TO':")
                                                    Log.d(TAG, "  Stara c.from: ${c.from.symbol}, Stara c.to: ${c.to.symbol}")
                                                    Log.d(TAG, "  Nowo wybrana dla 'to': ${nowoWybranaWalutaDlaTo.symbol}")
                                                    Log.d(TAG, "  Wywołuję zdarzenieZmianyWaluty z: index=$index, from=${c.from.symbol}, to=${nowoWybranaWalutaDlaTo.symbol}")

                                                    // TYLKO TO:
                                                    zdarzenieZmianyWaluty(index, c.from, nowoWybranaWalutaDlaTo)
                                                    // NIE rób tutaj przeliczeń ani zdarzenieZmianyWartosci
                                                    // NIE rób tutaj zdarzenieZapisuDanych
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
                                            value = c.amount,
                                            onValueChange = { nowaWartosc ->
                                                if (nowaWartosc.matches(wzorPolaTekstowego)) {
                                                    zdarzenieZmianyWartosci(index, nowaWartosc, c.result)

                                                    // Automatyczne przeliczanie wartości po wprowadzeniu ilości
                                                    val aktualizacjaKontenerow =
                                                        przeliczKonwersjeWalutowe(
                                                            mnoznikiWalut,
                                                            kontenery
                                                        )
                                                    zdarzenieZmianyWartosci(
                                                        index,
                                                        aktualizacjaKontenerow[index].amount,
                                                        aktualizacjaKontenerow[index].result
                                                    )
                                                    zdarzenieZapisuDanych()
                                                }
                                            },
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 30.sp
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            maxLines = 1,
                                            singleLine = true,
                                            enabled = czyPoleWejscioweWlaczone,
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(0.03f)
                                                .background(Color.Transparent)
                                        )
                                        Crossfade(targetState = c.from, label = "CurrencyChange") { walutaWejsciowa ->
                                            RozwijaneMenu(
                                                wybranaWaluta = walutaWejsciowa,
                                                zdarzenieWybraniaWaluty = { waluta ->
                                                    zdarzenieZmianyWaluty(index, waluta, c.to)
                                                    val aktualizacjaKontenerow =
                                                        przeliczKonwersjeWalutowe(
                                                            mnoznikiWalut,
                                                            kontenery
                                                        )
                                                    zdarzenieZmianyWartosci(
                                                        index,
                                                        aktualizacjaKontenerow[index].amount,
                                                        aktualizacjaKontenerow[index].result
                                                    )
                                                    zdarzenieZapisuDanych()
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
                                                interactionSource = zrodloInterakcji,
                                                indication = null
                                            ) {
                                                // Zmiana kąta obrotu o 180 stopni
                                                katObrotu += 180f

                                                // Zamiana walut "from" i "to"
                                                val nowaWalutaWejsciowa = c.to
                                                val nowaWalutaWyjsciowa = c.from
                                                zdarzenieZmianyWaluty(
                                                    index,
                                                    nowaWalutaWejsciowa,
                                                    nowaWalutaWyjsciowa
                                                )
                                                val aktualizacjaKontenerow =
                                                    przeliczKonwersjeWalutowe(
                                                        mnoznikiWalut,
                                                        kontenery
                                                    )
                                                zdarzenieZmianyWartosci(
                                                    index,
                                                    aktualizacjaKontenerow[index].amount,
                                                    aktualizacjaKontenerow[index].result
                                                )
                                                zdarzenieZapisuDanych()
                                            }
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
                                            value = c.result,
                                            onValueChange = { nowaWartosc ->
                                                if (nowaWartosc.matches(wzorPolaTekstowego)) {
                                                    zdarzenieZmianyWartosci(index, c.amount, nowaWartosc)
                                                    //isAmountFieldEnabled = nowaWartosc.isEmpty()
                                                }
                                            },
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
                                        Crossfade(targetState = c.to, label = "CurrencyChange") { walutaWyjsciowa ->
                                            RozwijaneMenu(
                                                wybranaWaluta = walutaWyjsciowa,
                                                zdarzenieWybraniaWaluty = { waluta ->
                                                    zdarzenieZmianyWaluty(index, c.from, waluta)
                                                    val aktualizacjaKontenerow =
                                                        przeliczKonwersjeWalutowe(
                                                            mnoznikiWalut,
                                                            kontenery
                                                        )
                                                    zdarzenieZmianyWartosci(
                                                        index,
                                                        aktualizacjaKontenerow[index].amount,
                                                        aktualizacjaKontenerow[index].result
                                                    )
                                                    zdarzenieZapisuDanych()
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
                                            value = c.amount,
                                            onValueChange = { nowaWartosc ->
                                                if (nowaWartosc.matches(wzorPolaTekstowego)) {
                                                    zdarzenieZmianyWartosci(index, nowaWartosc, c.result)

                                                    // Automatyczne przeliczanie wartości po wprowadzeniu ilości
                                                    val aktualizacjaKontenerow =
                                                        przeliczKonwersjeWalutowe(
                                                            mnoznikiWalut,
                                                            kontenery
                                                        )
                                                    zdarzenieZmianyWartosci(
                                                        index,
                                                        aktualizacjaKontenerow[index].amount,
                                                        aktualizacjaKontenerow[index].result
                                                    )
                                                    zdarzenieZapisuDanych()                                                }
                                            },
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 30.sp
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            maxLines = 1,
                                            singleLine = true,
                                            enabled = czyPoleWejscioweWlaczone,
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(0.03f)
                                                .background(Color.Transparent)
                                        )
                                        Crossfade(targetState = c.from, label = "CurrencyChange") { walutaWyjsciowa ->
                                            RozwijaneMenu(
                                                wybranaWaluta = walutaWyjsciowa,
                                                zdarzenieWybraniaWaluty = { waluta ->
                                                    zdarzenieZmianyWaluty(index, waluta, c.to)
                                                    val aktualizacjaKontenetow =
                                                        przeliczKonwersjeWalutowe(
                                                            mnoznikiWalut,
                                                            kontenery
                                                        )
                                                    zdarzenieZmianyWartosci(
                                                        index,
                                                        aktualizacjaKontenetow[index].amount,
                                                        aktualizacjaKontenetow[index].result
                                                    )
                                                    zdarzenieZapisuDanych()
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
                                                interactionSource = zrodloInterakcji,
                                                indication = null
                                            ) {
                                                // Zmiana kąta obrotu o 180 stopni
                                                katObrotu += 180f

                                                // Zamiana walut "from" i "to"
                                                val nowaWalutaWejsciowa = c.to
                                                val nowaWalutaWyjsciowa = c.from
                                                zdarzenieZmianyWaluty(
                                                    index,
                                                    nowaWalutaWejsciowa,
                                                    nowaWalutaWyjsciowa
                                                )
                                                val aktualizacjaKontenerow =
                                                    przeliczKonwersjeWalutowe(
                                                        mnoznikiWalut,
                                                        kontenery
                                                    )
                                                zdarzenieZmianyWartosci(
                                                    index,
                                                    aktualizacjaKontenerow[index].amount,
                                                    aktualizacjaKontenerow[index].result
                                                )
                                                zdarzenieZapisuDanych()                                            }
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
                                            value = c.result,
                                            onValueChange = { nowaWartosc ->
                                                if (nowaWartosc.matches(wzorPolaTekstowego)) {
                                                    zdarzenieZmianyWartosci(index, c.amount, nowaWartosc)
                                                    //isAmountFieldEnabled = nowaWartosc.isEmpty()
                                                }
                                            },
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
                                        Crossfade(targetState = c.to, label = "CurrencyChange") { walutaWyjsciowa ->
                                            RozwijaneMenu(
                                                wybranaWaluta = walutaWyjsciowa,
                                                zdarzenieWybraniaWaluty = { waluta ->
                                                    zdarzenieZmianyWaluty(index, c.from, waluta)
                                                    val aktualizacjaKontenerow =
                                                        przeliczKonwersjeWalutowe(
                                                            mnoznikiWalut,
                                                            kontenery
                                                        )
                                                    zdarzenieZmianyWartosci(
                                                        index,
                                                        aktualizacjaKontenerow[index].amount,
                                                        aktualizacjaKontenerow[index].result
                                                    )
                                                    zdarzenieZapisuDanych()
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
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        )
    }
}


