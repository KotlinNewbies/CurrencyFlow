package com.example.currencyflow.interfejs_uzytkownika.komponenty

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
fun LeweRozwijaneMenu(
    wybranaWaluta: Waluta,
    zdarzenieWybraniaWaluty: (Waluta) -> Unit,
    wybraneWaluty: List<Waluta>,
) {
    var rozwiniety by remember { mutableStateOf(false) }
    var waluty = wybraneWaluty.toTypedArray()  // Pobieranie listy krajów z klasy wyliczającej

    if (!waluty.contains(wybranaWaluta)) {
        // wybiera dostepne waluty
        waluty = wybraneWaluty.toTypedArray()
        if (waluty.isNotEmpty()) {
            zdarzenieWybraniaWaluty(waluty.first())
        }
        else{
            waluty = arrayOf(Waluta.EUR, Waluta.USD)
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
                    rozwiniety = !rozwiniety
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
                    painter = painterResource(id = wybranaWaluta.icon),
                    contentDescription = null
                )

            }
        }
        DropdownMenu(
            modifier = Modifier
                .heightIn(max = 400.dp)
                .background(MaterialTheme.colorScheme.onBackground),
            expanded = rozwiniety,
            onDismissRequest = { rozwiniety = false }
        ) {
            waluty.forEach { currency ->
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
                        zdarzenieWybraniaWaluty(currency) // Aktualizacja wybranej waluty po kliknięciu
                        rozwiniety = false // Schowanie menu po kliknięciu
                    }
                )
            }
        }
    }
}

@Composable
fun PraweRozwijaneMenu(
    wybranaWaluta: Waluta,
    zdarzenieWybraniaWaluty: (Waluta) -> Unit,
    wybraneWaluty: List<Waluta>
) {
    var rozwiniety by remember { mutableStateOf(false) }
    var waluty = wybraneWaluty.toTypedArray()  // Pobranie listy krajów z listy wyliczającej

    if (!waluty.contains(wybranaWaluta)) {
        // wybiera dostepną walutę
        waluty = wybraneWaluty.toTypedArray()
        if (waluty.isNotEmpty()) {
            zdarzenieWybraniaWaluty(waluty.first())
        }
        else{
            waluty = arrayOf(Waluta.EUR, Waluta.USD)
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
                    rozwiniety = !rozwiniety
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
                    painter = painterResource(id = wybranaWaluta.icon),
                    contentDescription = null
                )

            }
        }
        DropdownMenu(
            modifier = Modifier
                .heightIn(max = 400.dp)
                .background(MaterialTheme.colorScheme.onBackground),
            expanded = rozwiniety,
            onDismissRequest = { rozwiniety = false }
        ) {
            waluty.forEach { currency ->
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
                        zdarzenieWybraniaWaluty(currency) // Aktualizacja wybranej waluty po kliknięciu
                        rozwiniety = false // Schowanie menu po kliknięciu
                    }
                )
            }
        }
    }
}
