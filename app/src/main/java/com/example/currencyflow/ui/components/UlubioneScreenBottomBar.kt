package com.example.currencyflow.ui.components

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
import com.example.currencyflow.viewmodel.FavoriteCurrenciesViewModel

@Composable
fun UlubioneScreenBottomBar(
    navController: NavController, // Potrzebne do nawigacji
    viewModel: FavoriteCurrenciesViewModel, // Potrzebne do logiki zapisu i pokazywania dialogu
    pokazDialogUpdate: (Boolean) -> Unit, // Lambda do aktualizacji stanu dialogu w UlubioneWalutyScreen
    spowodujPodwojnaSilnaWibracje: () -> Unit // Lambda do wibracji
) {
    val configuration = LocalConfiguration.current
    val jestPoziomo = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val rozmiarEkranu = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

    val systemNavigationBarsPaddingValues = WindowInsets.navigationBars.asPaddingValues()
    val bottomPaddingSystemowy = systemNavigationBarsPaddingValues.calculateBottomPadding()

    val baseButtonHeight = 48.dp
    // Wysokość przycisku dostosowana do orientacji
    val currentButtonHeight = if (jestPoziomo && czyTelefon(configuration)) 36.dp else baseButtonHeight


    // Logika adaptacyjnych paddingów skopiowana z AdaptacyjnyBottomBar
    val jestPrawdopodobnieSkladakiemRozlozonym =
        (rozmiarEkranu >= Configuration.SCREENLAYOUT_SIZE_LARGE) && bottomPaddingSystemowy > 60.dp
    val stalyDodatkowyPaddingOdDolu = if (jestPoziomo && czyTelefon(configuration)) {
        4.dp
    } else if (jestPrawdopodobnieSkladakiemRozlozonym) {
        8.dp
    } else {
        30.dp
    }

    val verticalPaddingDlaPojedynczegoRzedu = if (jestPoziomo && czyTelefon(configuration)) {
        4.dp
    } else {
        8.dp
    }
    val gornyPaddingCalegoPaska = 1.dp
    val dodatkowyHorizontalPaddingForBar = 16.dp // Padding boczny dla zawartości w Row

    val extraPaddingDlaWysokichPaskow = if (bottomPaddingSystemowy > 30.dp && !czyTelefon(configuration) && !jestPrawdopodobnieSkladakiemRozlozonym) {
        16.dp
    } else {
        0.dp
    }

    // Struktura Column -> Row podobna do AdaptacyjnyBottomBar, aby zachować sposób aplikacji paddingów
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Tło dla całego obszaru paska
            .padding( // Systemowe paddingi boczne
                start = systemNavigationBarsPaddingValues.calculateLeftPadding(LocalLayoutDirection.current),
                end = systemNavigationBarsPaddingValues.calculateRightPadding(LocalLayoutDirection.current)
            )
            .padding( // Główne paddingi pionowe dla całego Column
                top = gornyPaddingCalegoPaska,
                bottom = bottomPaddingSystemowy + extraPaddingDlaWysokichPaskow + stalyDodatkowyPaddingOdDolu
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding( // Paddingi dla wewnętrznego Row z przyciskiem
                    horizontal = dodatkowyHorizontalPaddingForBar,
                    vertical = verticalPaddingDlaPojedynczegoRzedu
                )
                .heightIn(min = currentButtonHeight), // Minimalna wysokość paska na podstawie przycisku
            horizontalArrangement = Arrangement.Center, // Wycentrujemy pojedynczy przycisk
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier.height(currentButtonHeight), // Używamy obliczonej wysokości
                onClick = {
                    val listaWybranychWalut = viewModel.getCurrentlySelectedCurrencies()
                    if (listaWybranychWalut.size >= 2) {
                        viewModel.zapiszWybraneUlubioneWaluty()
                        navController.navigateUp()
                    } else {
                        spowodujPodwojnaSilnaWibracje()
                        pokazDialogUpdate(true) // Aktualizuj stan dialogu w ekranie nadrzędnym
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_save_24),
                    contentDescription = "Zapisz ulubione waluty"
                )
            }
        }
    }
}