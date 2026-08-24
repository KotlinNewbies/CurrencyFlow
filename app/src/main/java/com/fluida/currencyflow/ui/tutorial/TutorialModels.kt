package com.fluida.currencyflow.ui.tutorial

import androidx.compose.ui.geometry.Rect

enum class TutorialStep {
    INPUT_FIELD,
    SWAP_DRAG,
    DELETE,
    BOTTOM_ACTIONS
}

data class TutorialUiState(
    val currentStep: TutorialStep? = null,
    val isVisible: Boolean = false,
    val positions: Map<TutorialStep, Rect> = emptyMap()
) {
    val highlightRect: Rect get() = currentStep?.let { positions[it] } ?: Rect.Zero
}
