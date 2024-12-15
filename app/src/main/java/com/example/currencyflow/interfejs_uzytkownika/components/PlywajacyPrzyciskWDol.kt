package com.example.currencyflow.interfejs_uzytkownika.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.currencyflow.R
import kotlinx.coroutines.launch

@Composable
fun PlywajacyPrzyciskWDol(stanPrzesuniecia: ScrollState) {
    val zakresKorutyny = rememberCoroutineScope()
    FloatingActionButton(
        onClick = {
            zakresKorutyny.launch {
                if (stanPrzesuniecia.value >= stanPrzesuniecia.maxValue / 2) {
                    stanPrzesuniecia.animateScrollTo(0)
                } else {
                    stanPrzesuniecia.animateScrollTo(stanPrzesuniecia.maxValue)
                }
            }
        },
        modifier = Modifier
            .size(30.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Icon(
            modifier = Modifier
                .size(30.dp),
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_keyboard_arrow_down_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surface
        )
    }
}