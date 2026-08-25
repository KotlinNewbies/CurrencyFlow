package com.fluida.currencyflow.ui.components

import android.content.ClipData
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.fluida.currencyflow.data.model.Waluta
import com.fluida.currencyflow.util.haptics.spowodujSlabaWibracje
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurrencyRowInput(
    modifier: Modifier = Modifier,
    label: String,
    kontenerId: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean,
    textFieldWeight: Float,
    fontSize: TextUnit,
    regexPattern: Regex,
    selectedCurrency: Waluta,
    onCurrencySelected: (Waluta) -> Unit,
    availableCurrencies: List<Waluta>,
    onReportPosition: (Rect) -> Unit = {},
    onReportFlagPosition: (Rect) -> Unit = {}
) {
    val clipboardManager = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.onBackground,
                shape = MaterialTheme.shapes.medium
            )
            .background(
                MaterialTheme.colorScheme.background,
                RoundedCornerShape(10.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.05f)
        )
        Box(
            modifier = Modifier
                .weight(textFieldWeight)
                .fillMaxHeight()
                .onGloballyPositioned { coords ->
                    onReportPosition(coords.boundsInRoot())
                },
            contentAlignment = Alignment.CenterStart
        ) {
            if (isEnabled) {
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    value = value,
                    onValueChange = { newValue ->
                        if (newValue.matches(regexPattern) || newValue.isEmpty()) {
                            onValueChange(newValue)
                        }
                    },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = fontSize
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 1,
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            } else {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 2000
                        )
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                if (value.isNotEmpty() && value != "Error") {
                                    scope.launch {
                                        clipboardManager.setClipEntry(
                                            ClipEntry(ClipData.newPlainText("Currency Result", value))
                                        )
                                        spowodujSlabaWibracje(context)
                                    }
                                }
                            }
                        ),
                    text = value,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = fontSize
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.03f)
                .background(Color.Transparent)
        )
        Box(
            modifier = Modifier.onGloballyPositioned { coords ->
                onReportFlagPosition(coords.boundsInRoot())
            }
        ) {
            Crossfade(
                targetState = selectedCurrency,
                label = "CurrencyMenu_${label}_$kontenerId"
            ) { currency ->
                RozwijaneMenu(
                    wybranaWaluta = currency,
                    zdarzenieWybraniaWaluty = { selected ->
                            onCurrencySelected(selected)
                    },
                    wybraneWaluty = availableCurrencies
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.03f)
        )
    }
}
