package com.example.currencyflow.interfejs_uzytkownika.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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
    val view = LocalView.current
    val systemUiController = rememberSystemUiController()

    val useDarkIconsForStatusBar = false // Ikony paska statusu jasne, bo AppColorScheme.surface jest Black
    val statusBarColor = AppColorScheme.surface

    val useDarkIconsForNavigationBar = false // Ikony paska nawigacyjnego jasne
    val navigationBarColor = AppColorScheme.surface // Lub Color.Transparent dla pełnego edge-to-edge

    if (!view.isInEditMode) {
        SideEffect {
            // Konfiguracja paska statusu
            systemUiController.setStatusBarColor(
                color = statusBarColor,
                darkIcons = useDarkIconsForStatusBar
            )

            // Konfiguracja paska nawigacyjnego
            systemUiController.setNavigationBarColor(
                color = navigationBarColor,
                darkIcons = useDarkIconsForNavigationBar
            )
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}