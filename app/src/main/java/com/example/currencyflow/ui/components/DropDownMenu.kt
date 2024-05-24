package com.example.currencyflow.ui.components

import android.media.Image
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.currencyflow.R
import com.example.currencyflow.classes.Currency

@Composable
fun CurrencyDropDownMenuL() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val currencies = Currency.entries.toTypedArray()  // Pobierz listę krajów z enum Currency
    var selectedCurrency by remember { mutableStateOf(Currency.GBP) } // Domyślnie ustaw GBP jako wybraną walutę

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(60.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.medium
                )
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
                    modifier = Modifier.size(24.dp),
                    imageVector = ImageVector.vectorResource(id = selectedCurrency.icon),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = selectedCurrency.symbol)
            }
        }
        DropdownMenu(
            modifier = Modifier
                .height(400.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon( modifier = Modifier.size(24.dp),
                                imageVector = ImageVector.vectorResource(id = currency.icon), contentDescription = null,
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = currency.symbol)
                        }
                    },
                    onClick = {
                        selectedCurrency = currency // Aktualizuj wybraną walutę po kliknięciu
                        Toast.makeText(context, currency.symbol, Toast.LENGTH_SHORT).show()
                        expanded = false // Schowaj menu po kliknięciu
                    }
                )
            }
        }
    }
}

@Composable
fun CurrencyDropDownMenuR() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val currencies = Currency.entries.toTypedArray()  // Pobierz listę krajów z enum Currency
    var selectedCurrency by remember { mutableStateOf(Currency.GBP) } // Domyślnie ustaw GBP jako wybraną walutę

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(60.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.medium
                )
                .clickable {
                    expanded = !expanded
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon( modifier = Modifier.size(24.dp),
                    imageVector = ImageVector.vectorResource(id = selectedCurrency.icon), contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = selectedCurrency.symbol)
            }
        }
        DropdownMenu(
            modifier = Modifier
                .height(400.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon( modifier = Modifier.size(24.dp),
                                imageVector = ImageVector.vectorResource(id = currency.icon), contentDescription = null,
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = currency.symbol)
                        }
                    },
                    onClick = {
                        selectedCurrency = currency // Aktualizuj wybraną walutę po kliknięciu
                        Toast.makeText(context, currency.symbol, Toast.LENGTH_SHORT).show()
                        expanded = false // Schowaj menu po kliknięciu
                    }
                )
            }
        }
    }
}
