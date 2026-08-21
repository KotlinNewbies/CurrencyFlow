package com.fluida.currencyflow.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.fluida.currencyflow.CurrencyFlowApplication

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @param:StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    @Composable
    fun asString(): String {
        val context = LocalContext.current
        val languageManager = CurrencyFlowApplication.getLanguageManager(context)
        val localizedContext = languageManager.getContextWithLocale(context)
        
        return when (this) {
            is DynamicString -> value
            is StringResource -> localizedContext.getString(resId, *args)
        }
    }

    fun asString(context: android.content.Context): String {
        val languageManager = CurrencyFlowApplication.getLanguageManager(context)
        val localizedContext = languageManager.getContextWithLocale(context)

        return when (this) {
            is DynamicString -> value
            is StringResource -> localizedContext.getString(resId, *args)
        }
    }
}
