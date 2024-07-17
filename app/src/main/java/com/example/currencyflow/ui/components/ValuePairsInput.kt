package com.example.currencyflow.ui.components

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.currencyflow.R
import com.example.currencyflow.classes.Currency
import com.example.currencyflow.data.C
import com.example.currencyflow.data.data_management.saveContainerData

@Composable
fun ValuePairsInput(
    containers: List<C>,
    onValueChanged: (Int, String, String) -> Unit,
    onCurrencyChanged: (Int, Currency, Currency) -> Unit,
    onRemovePair: (Int) -> Unit,
    context: Context,
    pairCount: Int,
    selectedCurrencies: List<Currency>
) {
    val numberPattern = "^[0-9]*\\.?[0-9]*\$".toRegex()

    containers.forEachIndexed { index, c ->
        var isAmountFieldEnabled by remember { mutableStateOf(true) }
        var isResultFieldEnabled by remember { mutableStateOf(true) }

        // Initialize field enablement based on current values
        if (c.amount.isNotEmpty()) {
            isResultFieldEnabled = false
        }
        if (c.result.isNotEmpty()) {
            isAmountFieldEnabled = false
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column {
                        CurrencyDropDownMenuL(
                            selectedCurrency = c.from,
                            onCurrencySelected = { currency ->
                                onCurrencyChanged(index, currency, c.to)
                                saveContainerData(context, pairCount, containers)
                            },
                            selectedCurrencies = selectedCurrencies
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(55.dp)
                    )
                    Column {
                        CurrencyDropDownMenuR(
                            selectedCurrency = c.to,
                            onCurrencySelected = { currency ->
                                onCurrencyChanged(index, c.from, currency)
                                saveContainerData(context, pairCount, containers)
                            },
                            selectedCurrencies = selectedCurrencies
                        )
                    }
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                )
                HorizontalDivider()
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                )
                BoxWithConstraints{
                    if ( maxWidth < 600.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.width(140.dp),
                                value = c.amount,
                                onValueChange = { newValue ->
                                    if (newValue.matches(numberPattern)) {
                                        onValueChanged(index, newValue, c.result)
                                        isResultFieldEnabled = newValue.isEmpty()
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                maxLines = 1,
                                singleLine = true,
                                enabled = isAmountFieldEnabled
                            )
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                                contentDescription = null
                            )
                            OutlinedTextField(
                                modifier = Modifier.width(140.dp),
                                value = c.result,
                                onValueChange = { newValue ->
                                    if (newValue.matches(numberPattern)) {
                                        onValueChanged(index, c.amount, newValue)
                                        isAmountFieldEnabled = newValue.isEmpty()
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                maxLines = 1,
                                singleLine = true,
                                enabled = isResultFieldEnabled
                            )
                        }
                    }
                    else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.width(200.dp),
                                value = c.amount,
                                onValueChange = { newValue ->
                                    if (newValue.matches(numberPattern)) {
                                        onValueChanged(index, newValue, c.result)
                                        isResultFieldEnabled = newValue.isEmpty()
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                maxLines = 1,
                                singleLine = true,
                                enabled = isAmountFieldEnabled
                            )
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                                contentDescription = null
                            )
                            OutlinedTextField(
                                modifier = Modifier.width(200.dp),
                                value = c.result,
                                onValueChange = { newValue ->
                                    if (newValue.matches(numberPattern)) {
                                        onValueChanged(index, c.amount, newValue)
                                        isAmountFieldEnabled = newValue.isEmpty()
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                maxLines = 1,
                                singleLine = true,
                                enabled = isResultFieldEnabled
                            )
                        }
                    }

                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )
                if (containers.size > 1) {
                    Icon(
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {
                                onRemovePair(index)
                            },
                        imageVector = ImageVector.vectorResource(id = R.drawable.round_close_25),
                        contentDescription = null,
                    )
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
