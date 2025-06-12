
package com.example.currencyflow.ui.components // lub inny odpowiedni pakiet

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.util.isDeviceProbablyPhone


@Composable
fun SettingsScreenBottomBar(
    navController: NavController
) {
    val configuration = LocalConfiguration.current
    val jestPoziomo = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val rozmiarEkranu = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

    val systemNavigationBarsPaddingValues = WindowInsets.navigationBars.asPaddingValues()
    val bottomPaddingSystemowy = systemNavigationBarsPaddingValues.calculateBottomPadding()

    val baseButtonHeight = 48.dp
    val currentButtonHeight = if (jestPoziomo && isDeviceProbablyPhone(configuration)) 36.dp else baseButtonHeight

    val jestPrawdopodobnieSkladakiemRozlozonym =
        (rozmiarEkranu >= Configuration.SCREENLAYOUT_SIZE_LARGE) && bottomPaddingSystemowy > 60.dp
    val stalyDodatkowyPaddingOdDolu = if (jestPoziomo && isDeviceProbablyPhone(configuration)) {
        4.dp
    } else if (jestPrawdopodobnieSkladakiemRozlozonym) {
        8.dp
    } else {
        30.dp
    }

    val verticalPaddingDlaPojedynczegoRzedu = if (jestPoziomo && isDeviceProbablyPhone(configuration)) {
        4.dp
    } else {
        8.dp
    }
    val gornyPaddingCalegoPaska = 1.dp
    val dodatkowyHorizontalPaddingForBar = 16.dp

    val extraPaddingDlaWysokichPaskow = if (bottomPaddingSystemowy > 30.dp && !isDeviceProbablyPhone(configuration) && !jestPrawdopodobnieSkladakiemRozlozonym) {
        16.dp
    } else {
        0.dp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                start = systemNavigationBarsPaddingValues.calculateLeftPadding(LocalLayoutDirection.current),
                end = systemNavigationBarsPaddingValues.calculateRightPadding(LocalLayoutDirection.current)
            )
            .padding(
                top = gornyPaddingCalegoPaska,
                bottom = bottomPaddingSystemowy + extraPaddingDlaWysokichPaskow + stalyDodatkowyPaddingOdDolu
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dodatkowyHorizontalPaddingForBar,
                    vertical = verticalPaddingDlaPojedynczegoRzedu
                )
                .heightIn(min = currentButtonHeight),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier.height(currentButtonHeight),
                onClick = {
                    navController.navigateUp()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_save_24),
                    contentDescription = "Zapisz i wróć do ekranu głównego"
                )
            }
        }
    }
}