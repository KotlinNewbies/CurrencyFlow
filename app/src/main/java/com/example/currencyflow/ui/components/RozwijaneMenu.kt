package com.example.currencyflow.ui.components

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
import com.example.currencyflow.data.model.Waluta

@Composable
fun RozwijaneMenu(
    wybranaWaluta: Waluta,
    zdarzenieWybraniaWaluty: (Waluta) -> Unit,
    wybraneWaluty: List<Waluta>, // Zawsze używaj tej listy do wyświetlania opcji
) {
    var rozwiniety by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clickable {
                    if (wybraneWaluty.isNotEmpty()) {
                        rozwiniety = !rozwiniety
                    }
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
                    contentDescription = wybranaWaluta.symbol
                )
            }
        }

        DropdownMenu(
            modifier = Modifier
                .heightIn(max = 400.dp) // Ogranicz wysokość menu
                .background(MaterialTheme.colorScheme.onBackground),
            expanded = rozwiniety,
            onDismissRequest = { rozwiniety = false }
        ) {
            wybraneWaluty.forEach { currency ->
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
                            Text(
                                text = currency.symbol,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        zdarzenieWybraniaWaluty(currency)
                        rozwiniety = false
                    }
                )
            }
        }

    }
}
