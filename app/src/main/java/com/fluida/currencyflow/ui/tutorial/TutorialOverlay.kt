package com.fluida.currencyflow.ui.tutorial

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.fluida.currencyflow.R

@Composable
fun TutorialOverlay(
    tutorialState: TutorialUiState,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current

    if (!tutorialState.isVisible) return

    val rect = tutorialState.highlightRect

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
            .zIndex(100f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onNext
            )
    ) {
        // 1. Warstwa przyciemniająca
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color.Black.copy(alpha = 0.85f))
            
            if (rect != Rect.Zero) {
                // Precyzyjne dopasowanie dla każdego kroku
                val inflatedRect = when (tutorialState.currentStep) {
                    TutorialStep.INPUT_FIELD -> rect.inflate(-2.dp.toPx()) // Lekko zwężone, by nie nachodzić na strzałkę
                    TutorialStep.SWAP_DRAG -> rect.inflate(-6.dp.toPx()) // Zmniejszone, by pasowało do samej ikony
                    TutorialStep.DELETE -> rect.inflate(4.dp.toPx()) // Cały kontener z lekkim marginesem
                    TutorialStep.BOTTOM_ACTIONS -> rect.inflate(8.dp.toPx())
                    else -> rect.inflate(8.dp.toPx())
                }
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = inflatedRect.topLeft,
                    size = inflatedRect.size,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    blendMode = BlendMode.Clear
                )
            }
        }

        // 2. Dymek informacyjny
        if (tutorialState.currentStep != null) {
            val description = when (tutorialState.currentStep) {
                TutorialStep.INPUT_FIELD -> stringResource(R.string.tutorial_step_input_desc)
                TutorialStep.SWAP_DRAG -> stringResource(R.string.tutorial_step_swap_desc)
                TutorialStep.DELETE -> stringResource(R.string.tutorial_step_delete_desc)
                TutorialStep.BOTTOM_ACTIONS -> stringResource(R.string.tutorial_step_actions_desc)
            }

            // Obliczamy pozycję dymka w DP
            val tooltipY = remember(rect, tutorialState.currentStep) {
                if (rect == Rect.Zero) {
                    // Jeśli brak pozycji, środek ekranu
                    (config.screenHeightDp / 2 - 100).dp
                } else {
                    val topDp = with(density) { rect.top.toDp() }
                    val bottomDp = with(density) { rect.bottom.toDp() }
                    
                    if (topDp.value > config.screenHeightDp / 2) {
                        // Element jest w dolnej połowie -> Dymek NAD nim
                        topDp - 220.dp // Przykładowa wysokość dymka z marginesem
                    } else {
                        // Element jest w górnej połowie -> Dymek POD nim
                        bottomDp + 25.dp
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = tooltipY)
                        .widthIn(max = 340.dp)
                        .clickable(enabled = false) {},
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(
                                    text = stringResource(R.string.action_back),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onNext,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (tutorialState.currentStep == TutorialStep.BOTTOM_ACTIONS) 
                                        stringResource(R.string.tutorial_btn_done) 
                                    else 
                                        stringResource(R.string.tutorial_btn_next),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
