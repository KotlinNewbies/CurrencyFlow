package com.example.currencyflow.ui.components

import android.annotation.SuppressLint
import android.content.Context
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
import com.example.currencyflow.classes.Currency
import com.example.currencyflow.data.C
import com.example.currencyflow.data.CurrencyViewModel
import com.example.currencyflow.data.calculateCurrencyConversions
import com.example.currencyflow.data.data_management.saveContainerData
import com.example.currencyflow.data.processContainers
import com.example.currencyflow.haptics.triggerDoubleHardVibration
import com.example.currencyflow.haptics.triggerHardVibration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@SuppressLint("SuspiciousIndentation")
@Composable
fun ValuePairsInput(
    containers: List<C>,
    onValueChanged: (Int, String, String) -> Unit,
    onCurrencyChanged: (Int, Currency, Currency) -> Unit,
    onRemovePair: (Int) -> Unit,
    context: Context,
    selectedCurrencies: List<Currency>,
    currencyViewModel: CurrencyViewModel
) {
    val scope = rememberCoroutineScope()
    val numberPattern = "^[0-9]*\\.?[0-9]*\$".toRegex()
    val currencyRates by currencyViewModel.currencyRates.collectAsState() // Obserwowanie kursów walut
    val interactionSource = remember { MutableInteractionSource() }

    containers.forEachIndexed { index, c ->
        val isAmountFieldEnabled by remember { mutableStateOf(true) }
        var isResultFieldEnabled by remember { mutableStateOf(false) }

        LaunchedEffect(c.amount) {
            isResultFieldEnabled = c.amount.isNotEmpty()
            if (c.amount.isEmpty()) {
                onValueChanged(index, "", "") // Clear result when amount is empty
            }
        }
        LaunchedEffect(currencyRates) {
            // Przetwarzanie kontenerów i aktualizacja wyników
            val updatedContainers = calculateCurrencyConversions(currencyRates, containers)
            updatedContainers.forEachIndexed { index, updatedContainer ->
                if (containers[index] != updatedContainer) {
                    onValueChanged(index, updatedContainer.amount, updatedContainer.result)
                }
            }
        }
        var visible by remember(index) { mutableStateOf(true) }
        val delete = SwipeAction(
            onSwipe = {
                if (containers.size > 1) {
                    visible = false
                    scope.launch {
                        triggerHardVibration(context)
                        delay(400) // Adjust this delay to match the animation duration
                        visible = true
                        onRemovePair(index)
                        processContainers(currencyRates, containers)
                    }
                } else {
                    triggerDoubleHardVibration(context)

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
            visible = visible,
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
                endActions = listOf(delete), // Akcje po prawej stronie
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
                                            onValueChange = { newValue ->
                                                if (newValue.matches(numberPattern)) {
                                                    onValueChanged(index, newValue, c.result)

                                                    // Automatyczne przeliczanie wartości po wprowadzeniu ilości
                                                    val updatedContainers =
                                                        calculateCurrencyConversions(
                                                            currencyRates,
                                                            containers
                                                        )
                                                    onValueChanged(
                                                        index,
                                                        updatedContainers[index].amount,
                                                        updatedContainers[index].result
                                                    )
                                                    saveContainerData(context, containers)
                                                }
                                            },
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 26.sp // Ustawienie rozmiaru czcionki
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            maxLines = 1,
                                            singleLine = true,
                                            enabled = isAmountFieldEnabled,
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                        )

                                        Crossfade(targetState = c.from, label = "CurrencyChange") { fromCurrency ->
                                            CurrencyDropDownMenuL(
                                                selectedCurrency = fromCurrency,
                                                onCurrencySelected = { currency ->
                                                    onCurrencyChanged(index, currency, c.to)
                                                    processContainers(currencyRates, containers)
                                                    val updatedContainers =
                                                        calculateCurrencyConversions(
                                                            currencyRates,
                                                            containers
                                                        )
                                                    onValueChanged(
                                                        index,
                                                        updatedContainers[index].amount,
                                                        updatedContainers[index].result
                                                    )
                                                    saveContainerData(
                                                        context,
                                                        containers
                                                    )
                                                },
                                                selectedCurrencies = selectedCurrencies
                                            )
                                        }
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .weight(0.03f)
                                        )
                                    }
                                    // Stan przechowujący kąt obrotu
                                    var rotationAngle by remember { mutableFloatStateOf(0f) }

                                    // Animacja obrotu o 180 stopni
                                    val animatedRotationAngle by animateFloatAsState(
                                        targetValue = rotationAngle,
                                        animationSpec = tween(durationMillis = 500), // Czas trwania animacji
                                        label = ""
                                    )

                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .graphicsLayer(rotationZ = animatedRotationAngle) // Zastosowanie animacji obrotu
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null // Wyłączamy domyślny feedback
                                            ) {
                                                // Zmiana kąta obrotu o 180 stopni
                                                rotationAngle += 180f

                                                // Zamiana walut "from" i "to"
                                                val newFrom = c.to
                                                val newTo = c.from
                                                onCurrencyChanged(index, newFrom, newTo)
                                                processContainers(currencyRates, containers)
                                                val updatedContainers =
                                                    calculateCurrencyConversions(
                                                        currencyRates,
                                                        containers
                                                    )
                                                onValueChanged(
                                                    index,
                                                    updatedContainers[index].amount,
                                                    updatedContainers[index].result
                                                )
                                                saveContainerData(context, containers)
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
                                            onValueChange = { newValue ->
                                                if (newValue.matches(numberPattern)) {
                                                    onValueChanged(index, c.amount, newValue)
                                                    //isAmountFieldEnabled = newValue.isEmpty()
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

                                        Crossfade(targetState = c.to, label = "CurrencyChange") { toCurrency ->
                                            CurrencyDropDownMenuR(
                                                selectedCurrency = toCurrency,
                                                onCurrencySelected = { currency ->
                                                    onCurrencyChanged(index, c.from, currency)
                                                    processContainers(currencyRates, containers)
                                                    val updatedContainers =
                                                        calculateCurrencyConversions(
                                                            currencyRates,
                                                            containers
                                                        )
                                                    onValueChanged(
                                                        index,
                                                        updatedContainers[index].amount,
                                                        updatedContainers[index].result
                                                    )
                                                    saveContainerData(
                                                        context,
                                                        containers
                                                    )
                                                },
                                                selectedCurrencies = selectedCurrencies
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
                                            onValueChange = { newValue ->
                                                if (newValue.matches(numberPattern)) {
                                                    onValueChanged(index, newValue, c.result)

                                                    // Automatyczne przeliczanie wartości po wprowadzeniu ilości
                                                    val updatedContainers =
                                                        calculateCurrencyConversions(
                                                            currencyRates,
                                                            containers
                                                        )
                                                    onValueChanged(
                                                        index,
                                                        updatedContainers[index].amount,
                                                        updatedContainers[index].result
                                                    )
                                                    saveContainerData(context, containers)
                                                }
                                            },
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 30.sp
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            maxLines = 1,
                                            singleLine = true,
                                            enabled = isAmountFieldEnabled,
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(0.03f)
                                                .background(Color.Transparent)
                                        )
                                        Crossfade(targetState = c.from, label = "CurrencyChange") { fromCurrency ->
                                            CurrencyDropDownMenuL(
                                                selectedCurrency = fromCurrency,
                                                onCurrencySelected = { currency ->
                                                    onCurrencyChanged(index, currency, c.to)
                                                    processContainers(currencyRates, containers)
                                                    val updatedContainers =
                                                        calculateCurrencyConversions(
                                                            currencyRates,
                                                            containers
                                                        )
                                                    onValueChanged(
                                                        index,
                                                        updatedContainers[index].amount,
                                                        updatedContainers[index].result
                                                    )
                                                    saveContainerData(
                                                        context,
                                                        containers
                                                    )
                                                },
                                                selectedCurrencies = selectedCurrencies
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
                                    var rotationAngle by remember { mutableFloatStateOf(0f) }

                                    // Animacja obrotu o 180 stopni
                                    val animatedRotationAngle by animateFloatAsState(
                                        targetValue = rotationAngle,
                                        animationSpec = tween(durationMillis = 500),
                                        label = "" // Czas trwania animacji
                                    )

                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(52.dp)  // Upewnienie się, że ikona ma wystarczająco miejsca
                                            .graphicsLayer(rotationZ = animatedRotationAngle) // Zastosowanie animacji obrotu
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null // Wyłączamy domyślny feedback
                                            ) {
                                                // Zmiana kąta obrotu o 180 stopni
                                                rotationAngle += 180f

                                                // Zamiana walut "from" i "to"
                                                val newFrom = c.to
                                                val newTo = c.from
                                                onCurrencyChanged(index, newFrom, newTo)
                                                processContainers(currencyRates, containers)
                                                val updatedContainers =
                                                    calculateCurrencyConversions(
                                                        currencyRates,
                                                        containers
                                                    )
                                                onValueChanged(
                                                    index,
                                                    updatedContainers[index].amount,
                                                    updatedContainers[index].result
                                                )
                                                saveContainerData(context, containers)
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
                                            onValueChange = { newValue ->
                                                if (newValue.matches(numberPattern)) {
                                                    onValueChanged(index, c.amount, newValue)
                                                    //isAmountFieldEnabled = newValue.isEmpty()
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
                                        Crossfade(targetState = c.to, label = "CurrencyChange") { toCurrency ->
                                            CurrencyDropDownMenuR(
                                                selectedCurrency = toCurrency,
                                                onCurrencySelected = { currency ->
                                                    onCurrencyChanged(index, c.from, currency)
                                                    processContainers(currencyRates, containers)
                                                    val updatedContainers =
                                                        calculateCurrencyConversions(
                                                            currencyRates,
                                                            containers
                                                        )
                                                    onValueChanged(
                                                        index,
                                                        updatedContainers[index].amount,
                                                        updatedContainers[index].result
                                                    )
                                                    saveContainerData(
                                                        context,
                                                        containers
                                                    )
                                                },
                                                selectedCurrencies = selectedCurrencies
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
                                            onValueChange = { newValue ->
                                                if (newValue.matches(numberPattern)) {
                                                    onValueChanged(index, newValue, c.result)

                                                    // Automatyczne przeliczanie wartości po wprowadzeniu ilości
                                                    val updatedContainers =
                                                        calculateCurrencyConversions(
                                                            currencyRates,
                                                            containers
                                                        )
                                                    onValueChanged(
                                                        index,
                                                        updatedContainers[index].amount,
                                                        updatedContainers[index].result
                                                    )
                                                    saveContainerData(context, containers)
                                                }
                                            },
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 30.sp
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            maxLines = 1,
                                            singleLine = true,
                                            enabled = isAmountFieldEnabled,
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(0.03f)
                                                .background(Color.Transparent)
                                        )
                                        Crossfade(targetState = c.from, label = "CurrencyChange") { fromCurrency ->
                                            CurrencyDropDownMenuL(
                                                selectedCurrency = fromCurrency,
                                                onCurrencySelected = { currency ->
                                                    onCurrencyChanged(index, currency, c.to)
                                                    processContainers(currencyRates, containers)
                                                    val updatedContainers =
                                                        calculateCurrencyConversions(
                                                            currencyRates,
                                                            containers
                                                        )
                                                    onValueChanged(
                                                        index,
                                                        updatedContainers[index].amount,
                                                        updatedContainers[index].result
                                                    )
                                                    saveContainerData(
                                                        context,
                                                        containers
                                                    )
                                                },
                                                selectedCurrencies = selectedCurrencies
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
                                    var rotationAngle by remember { mutableFloatStateOf(0f) }

                                    // Animacja obrotu o 180 stopni
                                    val animatedRotationAngle by animateFloatAsState(
                                        targetValue = rotationAngle,
                                        animationSpec = tween(durationMillis = 500),
                                        label = "" // Czas trwania animacji
                                    )

                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(52.dp)  // Upewnienie się, że ikona ma wystarczająco miejsca
                                            .graphicsLayer(rotationZ = animatedRotationAngle) // Zastosowanie animacji obrotu
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null // Wyłączamy domyślny feedback
                                            ) {
                                                // Zmiana kąta obrotu o 180 stopni
                                                rotationAngle += 180f

                                                // Zamiana walut "from" i "to"
                                                val newFrom = c.to
                                                val newTo = c.from
                                                onCurrencyChanged(index, newFrom, newTo)
                                                processContainers(currencyRates, containers)
                                                val updatedContainers =
                                                    calculateCurrencyConversions(
                                                        currencyRates,
                                                        containers
                                                    )
                                                onValueChanged(
                                                    index,
                                                    updatedContainers[index].amount,
                                                    updatedContainers[index].result
                                                )
                                                saveContainerData(context, containers)
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
                                                .weight(0.80f)
                                                .fillMaxHeight(),
                                            value = c.result,
                                            onValueChange = { newValue ->
                                                if (newValue.matches(numberPattern)) {
                                                    onValueChanged(index, c.amount, newValue)
                                                    //isAmountFieldEnabled = newValue.isEmpty()
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
                                        Crossfade(targetState = c.to, label = "CurrencyChange") { toCurrency ->
                                            CurrencyDropDownMenuR(
                                                selectedCurrency = toCurrency,
                                                onCurrencySelected = { currency ->
                                                    onCurrencyChanged(index, c.from, currency)
                                                    processContainers(currencyRates, containers)
                                                    val updatedContainers =
                                                        calculateCurrencyConversions(
                                                            currencyRates,
                                                            containers
                                                        )
                                                    onValueChanged(
                                                        index,
                                                        updatedContainers[index].amount,
                                                        updatedContainers[index].result
                                                    )
                                                    saveContainerData(
                                                        context,
                                                        containers
                                                    )
                                                },
                                                selectedCurrencies = selectedCurrencies
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


