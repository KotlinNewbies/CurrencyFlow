package com.example.currencyflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.currencyflow.data.model.Waluta

@Composable
fun ElementListyWalut(
    waluta: Waluta,
    jestWybrana: Boolean,
    zdarzenieWybranejWaluty: (Boolean) -> Unit
) {
    val zrodloIteracji = remember { MutableInteractionSource() }
    rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                interactionSource = zrodloIteracji,
                indication = null,
                onClick = {
                    zdarzenieWybranejWaluty(!jestWybrana)
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
                painter = painterResource(id = waluta.icon),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = waluta.symbol, color = MaterialTheme.colorScheme.onSurface)
        }
        PoleWyboru(
            zaznaczone = jestWybrana,
            zdarzeniaZmianyZaznaczenia = { checked ->
                zdarzenieWybranejWaluty(checked)
            },
            modifier = Modifier.wrapContentSize()
        )
    }
}