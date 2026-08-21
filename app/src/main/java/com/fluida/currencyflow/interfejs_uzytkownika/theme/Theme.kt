package com.fluida.currencyflow.interfejs_uzytkownika.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = Yellow,
    secondary = LightGrey,
    background = DarkGrey,
    surface = Black, // Tło główne aplikacji, używane dla paska statusu
    onPrimary = TintedGrey,
    onSecondary = DarkYellow,
    onBackground = LightGrey,
    onSurface = coldWhite // Kolor tekstu/ikon na tle 'surface'
)

@Composable
fun CurrencyFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
