package com.fluida.currencyflow.ui.tutorial

import androidx.compose.ui.geometry.Rect

enum class TutorialStep {
    INPUT_FIELD,
    CURRENCY_SELECTION,
    COPY_RESULT,
    SWAP_DRAG,
    DELETE,
    BOTTOM_ACTIONS
}

data class TutorialUiState(
    val currentStep: TutorialStep? = null,
    val isVisible: Boolean = false,
    val positions: Map<TutorialStep, List<Rect>> = emptyMap()
) {
    val highlightRects: List<Rect> get() = currentStep?.let { positions[it] } ?: emptyList()
}
