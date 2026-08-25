package com.fluida.currencyflow.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.fluida.currencyflow.R
import com.fluida.currencyflow.util.UiText

@Composable
fun DecimalPlacesDialog(
    initialValue: Int,
    onApply: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var temporarilySelectedValue by remember { mutableStateOf(initialValue) }
    val buttonIconSize = 20.dp

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiText.StringResource(R.string.decimal_places_setting_title).asString()) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DecimalPlacesRow(
                    label = UiText.StringResource(R.string.decimal_places_2).asString(),
                    isSelected = temporarilySelectedValue == 2,
                    onClick = { temporarilySelectedValue = 2 }
                )
                DecimalPlacesRow(
                    label = UiText.StringResource(R.string.decimal_places_4).asString(),
                    isSelected = temporarilySelectedValue == 4,
                    onClick = { temporarilySelectedValue = 4 }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(temporarilySelectedValue)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.round_check_24),
                    contentDescription = "OK",
                    modifier = Modifier.size(buttonIconSize)
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_close_24),
                    contentDescription = UiText.StringResource(R.string.action_cancel).asString(),
                    modifier = Modifier.size(buttonIconSize)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun DecimalPlacesRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}
