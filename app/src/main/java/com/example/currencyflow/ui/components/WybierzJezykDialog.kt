package com.example.currencyflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.currencyflow.R
import com.example.currencyflow.data.LanguageOption

@Composable
fun LanguageSelectionDialog(
    availableLanguages: List<LanguageOption>,
    initiallySelectedTag: String,
    onApply: (selectedTag: String) -> Unit,
    onDismiss: () -> Unit
) {
    // Stan do przechowywania tymczasowo wybranego języka w dialogu
    var temporarilySelectedTag by remember { mutableStateOf(initiallySelectedTag) }
    val buttonIconSize = 20.dp

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.language_selection_dialog_title)) },
        text = {
            val listState = rememberLazyListState()
            val showBottomFade by remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    if (layoutInfo.visibleItemsInfo.isEmpty() || layoutInfo.totalItemsCount == 0) {
                        false
                    } else {
                        val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
                        val isLastItemTheActualLast = lastVisibleItem.index == layoutInfo.totalItemsCount - 1
                        val isLastItemFullyVisible = (lastVisibleItem.offset + lastVisibleItem.size) <= layoutInfo.viewportEndOffset + 5
                        listState.canScrollForward || (isLastItemTheActualLast && !isLastItemFullyVisible)
                    }
                }
            }

            Box {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(availableLanguages, key = { _, langOption -> langOption.tag }) { index, langOption ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally // Aby wyśrodkować Box z dividerem
                        ) {
                            LanguageDialogRow(
                                languageName = stringResource(id = langOption.displayNameResId),
                                isSelected = langOption.tag == temporarilySelectedTag,
                                onClick = {
                                    temporarilySelectedTag = langOption.tag
                                }
                            )

                            // Dodaj HorizontalDivider, jeśli to nie jest ostatni element
                            if (index < availableLanguages.lastIndex) {
                                // Box do centrowania dividera, jeśli jest węższy niż pełna szerokość
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    HorizontalDivider(
                                        modifier = Modifier.fillMaxWidth(0.7f),
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Warunkowo wyświetl gradient na dole, jeśli jest więcej elementów do przewinięcia
                if (showBottomFade) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                // Zastąp IconButton na Button
                onClick = {
                    onApply(temporarilySelectedTag)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Tło przycisku
                    contentColor = MaterialTheme.colorScheme.surface    // Kolor ikony
                ),

                ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.round_check_24),
                    contentDescription = "OK"/*stringResource(R.string.action_apply)*/, // Użyj zasobu string dla "OK" lub "Zastosuj"
                    modifier = Modifier.size(buttonIconSize) // Ustaw rozmiar ikony
                )
            }
        },
        dismissButton = {
            Button(
                // Zastąp IconButton na Button
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Tło przycisku
                    contentColor = MaterialTheme.colorScheme.surface    // Kolor ikony
                ),

                ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_close_24),
                    contentDescription = stringResource(R.string.action_cancel), // Użyj zasobu string dla "Close" lub "Anuluj"
                    modifier = Modifier.size(buttonIconSize) // Ustaw rozmiar ikony
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun LanguageDialogRow(
    languageName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconSize = 24.dp
    val minRowHeight = 36.dp
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick)
            .padding(horizontal = 24.dp)
            .defaultMinSize(minHeight = minRowHeight)
            .padding(vertical = (minRowHeight - iconSize) / 2)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = languageName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(id = R.string.selected_language_indicator),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(iconSize) // Upewnij się, że ikona ma określony rozmiar
            )
        } else {
            // Dodaj Spacer o takiej samej szerokości i wysokości jak ikona + jej padding,
            // aby utrzymać stały układ.
            // Padding startowy ikony to 8.dp, a sama ikona ma 24.dp szerokości.
            Spacer(modifier = Modifier.width(8.dp + iconSize))
            // Jeśli chcesz też zagwarantować wysokość, chociaż CenterVertically powinno sobie poradzić:
            // Spacer(modifier = Modifier.size(width = 8.dp + iconSize, height = iconSize))
            // Lub jeśli tylko szerokość jest problemem:
            // Spacer(modifier = Modifier.width(8.dp + iconSize))
        }
    }
}