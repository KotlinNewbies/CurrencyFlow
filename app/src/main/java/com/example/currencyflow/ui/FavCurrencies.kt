package com.example.currencyflow.ui

import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.classes.Currency

@Composable
fun FavCurrencies(navController: NavController) {
    val sampleCurrencies = listOf(
        Currency.USD,
        Currency.JPY
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier
            .width(400.dp)
            .height(600.dp)
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {

        }
//        LazyColumn {
//            items(sampleCurrencies) { currency ->
//                CurrencyItem(currency = currency) {
//                    // Handle currency click
//                }
//            }
//        }
        Button(
            onClick = {
                navController.navigateUp() // Go back to the previous screen
            }
        ) {
            Text(text = "Zapisz")
        }
    }
}

@Preview
@Composable
fun FavCurrenciesPreview() {
    val sampleCurrencies = listOf(
        Currency.USD,
        //Currency.EUR,
        Currency.JPY
    )
}
