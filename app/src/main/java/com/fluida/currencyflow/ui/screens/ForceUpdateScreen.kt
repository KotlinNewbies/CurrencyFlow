package com.fluida.currencyflow.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluida.currencyflow.R
import com.fluida.currencyflow.interfejs_uzytkownika.theme.Black
import com.fluida.currencyflow.interfejs_uzytkownika.theme.Yellow
import com.fluida.currencyflow.interfejs_uzytkownika.theme.coldWhite

@Composable
fun ForceUpdateScreen(
    onUpdateClick: () -> Unit,
    onExitClick: () -> Unit
) {
    BackHandler(enabled = true) {
        onExitClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SystemUpdate,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Yellow
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.force_update_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = coldWhite
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.force_update_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = coldWhite.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onUpdateClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Yellow,
                contentColor = Black
            )
        ) {
            Text(
                text = stringResource(R.string.force_update_button),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onExitClick,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Yellow),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Black,
                contentColor = Yellow
            )
        ) {
            Text(
                text = stringResource(R.string.force_update_exit_button),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
