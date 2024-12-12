package com.example.currencyflow.interfejs_uzytkownika.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.currencyflow.klasy.Waluta

@Composable
fun CurrencyDropDownMenuL(
    selectedWaluta: Waluta,
    onCurrencySelected: (Waluta) -> Unit,
    selectedCurrencies: List<Waluta>,
) {
    var expanded by remember { mutableStateOf(false) }
    var currencies = selectedCurrencies.toTypedArray()  /* Pobieranie listy krajów z enum Currency */

    if (!currencies.contains(selectedWaluta)) {
        // Jeśli nie jest, wybierz pierwszą dostępną walutę
        currencies = selectedCurrencies.toTypedArray()
        if (currencies.isNotEmpty()) {
            onCurrencySelected(currencies.first())
        }
        else{
            currencies = arrayOf(Waluta.EUR, Waluta.USD)
        }
    }
    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clickable {
                    expanded = !expanded
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier.size(50.dp),
                    painter = painterResource(id = selectedWaluta.icon),
                    contentDescription = null
                )

            }
        }
        DropdownMenu(
            modifier = Modifier
                .heightIn(max = 400.dp)
                .background(MaterialTheme.colorScheme.onBackground),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                modifier = Modifier.size(26.dp),
                                painter = painterResource(id = currency.icon),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = currency.symbol)
                        }
                    },
                    onClick = {
                        onCurrencySelected(currency) // Aktualizacja wybranej waluty po kliknięciu
                        expanded = false // Schowanie menu po kliknięciu
                    }
                )
            }
        }
    }
}

@Composable
fun CurrencyDropDownMenuR(
    selectedWaluta: Waluta,
    onCurrencySelected: (Waluta) -> Unit,
    selectedCurrencies: List<Waluta>
) {
    var expanded by remember { mutableStateOf(false) }
    var currencies = selectedCurrencies.toTypedArray()  // Pobranie listy krajów z enum Currency

    if (!currencies.contains(selectedWaluta)) {
        // Jeśli nie jest, wybierz pierwszą dostępną walutę
        currencies = selectedCurrencies.toTypedArray()
        if (currencies.isNotEmpty()) {
            onCurrencySelected(currencies.first())
        }
        else{
            currencies = arrayOf(Waluta.EUR, Waluta.USD)
        }
    }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clickable {
                    expanded = !expanded
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier.size(50.dp),
                    painter = painterResource(id = selectedWaluta.icon),
                    contentDescription = null
                )

            }
        }
        DropdownMenu(
            modifier = Modifier
                .heightIn(max = 400.dp)
                .background(MaterialTheme.colorScheme.onBackground),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                modifier = Modifier.size(26.dp),
                                painter = painterResource(id = currency.icon),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = currency.symbol)
                        }
                    },
                    onClick = {
                        onCurrencySelected(currency) // Aktualizacja wybranej waluty po kliknięciu
                        //Toast.makeText(context, currency.symbol, Toast.LENGTH_SHORT).show()
                        expanded = false // Schowanie menu po kliknięciu
                    }
                )
            }
        }
    }
}
