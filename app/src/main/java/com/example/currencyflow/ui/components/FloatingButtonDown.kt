package com.example.currencyflow.ui.components

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
fun FloatingButtonDown(scrollState: ScrollState) {
    val coroutineScope = rememberCoroutineScope()
    FloatingActionButton(
        onClick = {
            coroutineScope.launch {
                if (scrollState.value >= scrollState.maxValue / 2) {
                    scrollState.animateScrollTo(0)
                } else {
                    scrollState.animateScrollTo(scrollState.maxValue)
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