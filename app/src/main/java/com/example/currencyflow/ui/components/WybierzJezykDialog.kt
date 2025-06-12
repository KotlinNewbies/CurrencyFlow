package com.example.currencyflow.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.currencyflow.R
import com.example.currencyflow.data.LanguageOption

@Composable
fun LanguageSelectionDialog(
    availableLanguages: List<LanguageOption>,
    currentLanguageTag: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.language_selection_dialog_title)) },
        text = {
            LazyColumn {
                items(availableLanguages) { langOption ->
                    LanguageDialogRow(
                        languageName = stringResource(id = langOption.displayNameResId),
                        isSelected = langOption.tag == currentLanguageTag,
                        onClick = { onLanguageSelected(langOption.tag) }
                    )
                }
            }
        },
        confirmButton = {
            // Pusty, zgodnie z Twoim kodem
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(id = R.string.action_cancel).uppercase(),
                    color = MaterialTheme.colorScheme.primary
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = languageName,
            modifier = Modifier.weight(1f),
            style = if (isSelected) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(id = R.string.selected_language_indicator),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}