package com.fluida.currencyflow.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.fluida.currencyflow.R
import com.fluida.currencyflow.util.UiText

@Composable
fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val buttonIconSize = 20.dp

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiText.StringResource(R.string.logout_dialog_title).asString()) },
        text = { Text(UiText.StringResource(R.string.logout_dialog_message).asString()) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
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
                    imageVector = ImageVector.vectorResource(R.drawable.round_close_25),
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
