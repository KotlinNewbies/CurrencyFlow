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
    surface = Black,
    onPrimary = TintedGrey,
    onSecondary = Black,
    onBackground = LightGrey,
    onSurface = coldWhite
)

@Composable
fun CurrencyFlowTheme(
    content: @Composable () -> Unit
) {
    // Apply theme to the composable content
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,  // Ensure Typography is also defined somewhere
        content = content
    )

    // Set status bar color and icons color
    val view = LocalView.current
    val systemUiController = rememberSystemUiController()
    if (!view.isInEditMode) {
        SideEffect {
            systemUiController.setStatusBarColor(
                color = AppColorScheme.surface, // Use your desired color
                darkIcons = false // Set to false for light icons on a dark background
            )
        }
    }
}
