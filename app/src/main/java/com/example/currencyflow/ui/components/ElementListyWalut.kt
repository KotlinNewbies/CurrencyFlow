package com.example.currencyflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.currencyflow.data.model.Waluta

@Composable
fun ElementListyWalut(
    waluta: Waluta,
    jestWybrana: Boolean,
    zdarzenieWybranejWaluty: (Boolean) -> Unit
) {
    val zrodloIteracji = remember { MutableInteractionSource() }
    val preferowanaWysokoscFlagi = 48.dp // Zdefiniuj preferowaną wysokość
    rememberCoroutineScope()

    Row(
        modifier = Modifier // Użyj przekazanego modyfikatora
            .fillMaxWidth()
            .clickable(
                interactionSource = zrodloIteracji,
                indication = null, // Rozważ dodanie wskazania kliknięcia dla lepszego UX
                onClick = { zdarzenieWybranejWaluty(!jestWybrana) }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp), // Zwiększony padding dla lepszego wyglądu
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Lewa strona z informacjami o walucie
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = waluta.icon),
                contentDescription = stringResource(id = waluta.nazwaResId),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(preferowanaWysokoscFlagi) // Użyj tej samej wysokości tutaj
                    .padding(end = 12.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = preferowanaWysokoscFlagi) // <--- KLUCZOWA ZMIANA
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = waluta.symbol,
                    style = MaterialTheme.typography.titleMedium, // Wyraźniejszy styl dla symbolu
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp)) // Mały odstęp
                Text(
                    text = stringResource(id = waluta.nazwaResId),
                    style = MaterialTheme.typography.bodyMedium, // Standardowy styl dla pełnej nazwy
                    color = MaterialTheme.colorScheme.primary, // Subtelniejszy kolor
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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