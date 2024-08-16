package com.example.currencyflow.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.currencyflow.classes.Currency
import com.example.currencyflow.data.data_management.loadSelectedCurrencies
import com.example.currencyflow.data.data_management.saveSelectedCurrencies
import com.example.currencyflow.ui.components.CustomCheckbox

@Composable
fun FavCurrencies(navController: NavController) {
    val context = LocalContext.current
    val allCurrencies = Currency.entries.toList()
    val defaultSelectedCurrencies = listOf(Currency.EUR, Currency.USD)
    val initialSelectedCurrencies = loadSelectedCurrencies(context)
    val selectedCurrencies = remember { mutableStateMapOf<Currency, Boolean>().apply {
        // Domyślnie kliknięte checkboxy
        allCurrencies.forEach { currency ->
            put(currency, defaultSelectedCurrencies.contains(currency) || initialSelectedCurrencies.contains(currency))
        }
    }}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .weight(0.1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wybierz ulubioną walutę",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        LazyColumn(
            modifier = Modifier
                .weight(0.8f)
        ) {
            items(allCurrencies) { currency ->
                val isSelected = selectedCurrencies[currency] ?: false
                CurrencyItem(currency = currency, isSelected = isSelected) { selected ->
                    selectedCurrencies[currency] = selected
                }
            }
        }
        Row(
            modifier = Modifier
                .weight(0.1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                ),
                onClick = {
                    val selectedCurrencyList = selectedCurrencies.filterValues { it }.keys.toList()
                    saveSelectedCurrencies(context, selectedCurrencyList)
                    navController.navigateUp() // Powrót do poprzedniego ekranu
                }
            ) {
                Text(text = "Zapisz")
            }
        }
    }
}

@Composable
fun CurrencyItem(
    currency: Currency,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Wyłączamy domyślny feedback
                onClick = {
                    onCheckedChange(!isSelected)
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                modifier = Modifier
                    .size(36.dp),
                painter = painterResource(id = currency.icon),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = currency.symbol, color = MaterialTheme.colorScheme.onSurface)
        }
        CustomCheckbox(
            checked = isSelected,
            onCheckedChange = { checked ->
                onCheckedChange(checked) // Przekazujemy zmianę stanu dalej
            },
            modifier = Modifier.wrapContentSize()
        )
    }
}
