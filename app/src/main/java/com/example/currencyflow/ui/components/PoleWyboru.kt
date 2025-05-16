package com.example.currencyflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PoleWyboru(
    zaznaczone: Boolean,
    zdarzeniaZmianyZaznaczenia: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    kolorZaznaczenia: Color = MaterialTheme.colorScheme.primary,
    kolorBrakuZaznaczenia: Color = MaterialTheme.colorScheme.background,
    kolorZnakuZaznaczenia: Color = Color.Black
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(if (zaznaczone) kolorZaznaczenia else kolorBrakuZaznaczenia, shape = CircleShape)
            .clickable(
                indication = null, // wyłącza efekt fali
                interactionSource = remember { MutableInteractionSource() }
            ) { zdarzeniaZmianyZaznaczenia?.invoke(!zaznaczone) },
        contentAlignment = Alignment.Center,
    ) {
        if (zaznaczone) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = kolorZnakuZaznaczenia,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}