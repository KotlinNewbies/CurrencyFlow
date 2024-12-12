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
fun FloatingButtonUp(scrollState: ScrollState) {
    val coroutineScope = rememberCoroutineScope()
    FloatingActionButton(
        onClick = {
            coroutineScope.launch {
                scrollState.animateScrollTo(0)
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
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_keyboard_arrow_up_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surface
        )
    }
}