package com.example.currencyflow.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.currencyflow.data.model.Waluta
import kotlin.text.matches


@Composable
fun CurrencyRowInput(
    modifier: Modifier = Modifier,
    label: String,
    kontenerId: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean,
    textFieldWeight: Float,
    fontSize: TextUnit,
    regexPattern: Regex,
    selectedCurrency: Waluta,
    onCurrencySelected: (Waluta) -> Unit,
    availableCurrencies: List<Waluta>
) {
    Row(
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground,
                shape = MaterialTheme.shapes.medium
            )
            .background(
                MaterialTheme.colorScheme.background,
                RoundedCornerShape(10.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.05f)
        )
        BasicTextField(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .weight(textFieldWeight)
                .fillMaxHeight(),
            value = value,
            onValueChange = { newValue ->
                if (isEnabled && (newValue.matches(regexPattern) || newValue.isEmpty())) {
                    onValueChange(newValue)
                }
            },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = fontSize
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            maxLines = 1,
            singleLine = true,
            enabled = isEnabled,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.03f)
                .background(Color.Transparent)
        )
        Crossfade(
            targetState = selectedCurrency,
            label = "CurrencyMenu_${label}_${kontenerId}"
        ) { currency ->
            RozwijaneMenu(
                wybranaWaluta = currency,
                zdarzenieWybraniaWaluty = { selected ->
                        onCurrencySelected(selected)
                },
                wybraneWaluty = availableCurrencies
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.03f)
        )
    }
}