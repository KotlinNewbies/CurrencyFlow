package com.example.currencyflow.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.classes.Currency
import com.example.currencyflow.data.SelectedCurrenciesViewModel
import com.example.currencyflow.data.data_management.loadSelectedCurrencies
import com.example.currencyflow.data.data_management.saveSelectedCurrencies
import com.example.currencyflow.haptics.triggerDoubleHardVibration
import com.example.currencyflow.ui.components.CustomCheckbox
import com.example.currencyflow.ui.components.MinCurrenciesAlertDialog

@Composable
fun FavCurrencies(navController: NavController) {
    val viewModel: SelectedCurrenciesViewModel = viewModel()
    val context = LocalContext.current
    val allCurrencies = Currency.entries.toList()
    val initialSelectedCurrencies = loadSelectedCurrencies(context)

    // Inicjalizacja walut w ViewModelu (tylko raz, gdy ViewModel jest tworzony)
    viewModel.initializeCurrencies(allCurrencies, initialSelectedCurrencies)

    // Obserwowanie zmian w zaznaczonych walutach
    val selectedCurrencies by viewModel.selectedCurrencies.collectAsState()

    val quicksandVariable = FontFamily(
        Font(R.font.quicksand_variable, FontWeight.Normal)
    )

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .weight(0.09f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoxWithConstraints {
                if (maxWidth < 600.dp) {
                    Text(
                        text = "Wybierz ulubioną walutę",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = quicksandVariable
                    )
                } else {
                    Text(
                        text = "Wybierz ulubioną walutę",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = quicksandVariable
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(0.77f)
        ) {
            items(allCurrencies) { currency ->
                val isSelected = selectedCurrencies[currency] ?: false
                CurrencyItem(
                    currency = currency,
                    isSelected = isSelected
                ) { selected ->
                    viewModel.updateCurrencySelection(currency, selected)
                }
            }
        }
        Row(
            modifier = Modifier
                .weight(0.14f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                ),
                onClick = {
                    val selectedCurrencyList = viewModel.getSelectedCurrencies()
                    if (selectedCurrencyList.size >= 2) {
                        saveSelectedCurrencies(context, selectedCurrencyList)
                        navController.navigateUp() // Powrót do poprzedniego ekranu
                    } else {
                        triggerDoubleHardVibration(context)
                        showDialog = true
                    }
                }
            ) {
                Icon(
                    modifier = Modifier
                        .size(26.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_save_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
    MinCurrenciesAlertDialog(showDialog = showDialog, onDismiss = { showDialog = false })
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
                onCheckedChange(checked)
            },
            modifier = Modifier.wrapContentSize()
        )
    }
}

