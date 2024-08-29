package com.example.currencyflow.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AppColorScheme.surface.toArgb() // Set status bar color to black

            // Use WindowInsetsControllerCompat for compatibility
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false // Set icons to be light (white) for dark background
        }
    }
}
