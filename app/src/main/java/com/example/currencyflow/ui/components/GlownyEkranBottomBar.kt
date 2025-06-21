package com.example.currencyflow.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.currencyflow.R
import com.example.currencyflow.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.currencyflow.util.isDeviceProbablyPhone

@Composable
fun GlownyEkranBottomBar(
    homeViewModel: HomeViewModel, // Przekaż ViewModel lub odpowiednie lambdy
    stanListy: LazyListState,
    zakresKorutyn: CoroutineScope,
    spowodujSlabaWibracje: () -> Unit, // Zamiast przekazywać całą aktywność
    navigateToUlubione: () -> Unit,
    konteneryUISize: Int // Potrzebne do przewijania
) {
    val configuration = LocalConfiguration.current
    val jestPoziomo = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val rozmiarEkranu = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

    val systemNavigationBarsPaddingValues = WindowInsets.navigationBars.asPaddingValues()
    val bottomPaddingSystemowy = systemNavigationBarsPaddingValues.calculateBottomPadding()

    val buttonHeight = 48.dp
    val iconSize = 26.dp
    val interButtonSpacing = 16.dp // Odstęp między głównymi przyciskami
    val dodatkowyHorizontalPaddingForBar = 16.dp

    val jestPrawdopodobnieSkladakiemRozlozonym =
        (rozmiarEkranu >= Configuration.SCREENLAYOUT_SIZE_LARGE) && bottomPaddingSystemowy > 60.dp
    val stalyDodatkowyPaddingOdDolu = if (jestPoziomo && isDeviceProbablyPhone(configuration)) {
        4.dp
    } else if (jestPrawdopodobnieSkladakiemRozlozonym) {
        8.dp
    } else {
        30.dp
    }

    // Uprościmy paddingi pionowe, bo mamy teraz tylko jeden rząd
    val verticalPaddingDlaPojedynczegoRzedu = if (jestPoziomo && isDeviceProbablyPhone(configuration)) {
        4.dp // Mniejszy padding w trybie poziomym
    } else {
        8.dp // Standardowy padding w trybie pionowym
    }
    // Górny padding całego paska, jeśli potrzebny niezależnie od systemowego
    val gornyPaddingCalegoPaska = 1.dp

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
                    vertical = verticalPaddingDlaPojedynczegoRzedu // Używamy nowego paddingu
                )
                .heightIn(min = buttonHeight), // Minimalna wysokość całego rzędu, np. wysokość największego przycisku
            horizontalArrangement = Arrangement.SpaceBetween, // Rozmieści skrajne i środkową grupę
            verticalAlignment = Alignment.CenterVertically // Wyśrodkuj wszystko wertykalnie
        ) {
            // Przycisk przewijania w GÓRĘ (lewa strona)
            val pokazGornyPrzyciskPrzewijania by remember(konteneryUISize) { // Główny klucz to rozmiar danych
                derivedStateOf {
                    if (konteneryUISize == 0) return@derivedStateOf false
                    stanListy.firstVisibleItemIndex > 0
                }
            }
            IconButton(
                onClick = { zakresKorutyn.launch { stanListy.animateScrollToItem(0) } },
                enabled = pokazGornyPrzyciskPrzewijania,
                modifier = Modifier.alpha(if (pokazGornyPrzyciskPrzewijania) 1f else 0f),
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Przewiń w górę",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // GRUPA GŁÓWNYCH PRZYCISKÓW (na środku)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically // Upewnij się, że są wyśrodkowane względem siebie
            ) {
                Button(
                    modifier = Modifier.height(if (jestPoziomo && isDeviceProbablyPhone(configuration)) 36.dp else buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = {
                        homeViewModel.dodajKontener()
                        spowodujSlabaWibracje()
                        zakresKorutyn.launch {
                            if (konteneryUISize > 0) {
                                stanListy.animateScrollToItem(homeViewModel.konteneryUI.value.size - 1)
                            }
                        }
                    }) {
                    Icon(
                        modifier = Modifier.size(if (jestPoziomo && isDeviceProbablyPhone(configuration)) 20.dp else iconSize),
                        imageVector = ImageVector.vectorResource(id = R.drawable.round_add_24),
                        contentDescription = "Dodaj walutę",
                        tint = MaterialTheme.colorScheme.surface
                    )
                }

                Spacer(modifier = Modifier.width(interButtonSpacing))

                Button(
                    modifier = Modifier.height(if (jestPoziomo && isDeviceProbablyPhone(configuration)) 36.dp else buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = navigateToUlubione
                ) {
                    Icon(
                        modifier = Modifier.size(if (jestPoziomo && isDeviceProbablyPhone(configuration)) 20.dp else iconSize),
                        imageVector = ImageVector.vectorResource(id = R.drawable.round_favorite_border_24),
                        contentDescription = "Ulubione waluty",
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // Przycisk przewijania w DÓŁ (prawa strona)
            val pokazDolnyPrzyciskPrzewijania by remember(konteneryUISize) {
                derivedStateOf {
                    if (konteneryUISize == 0) return@derivedStateOf false
                    if (stanListy.isScrollInProgress) return@derivedStateOf false // <--- NOWY WARUNEK

                    val layoutInfo = stanListy.layoutInfo
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false

                    lastVisibleItem.index < konteneryUISize - 1
                }
            }
            IconButton(
                onClick = {
                    zakresKorutyn.launch {
                        if (konteneryUISize > 0) stanListy.animateScrollToItem(konteneryUISize - 1)
                    }
                },
                enabled = pokazDolnyPrzyciskPrzewijania,
                modifier = Modifier.alpha(if (pokazDolnyPrzyciskPrzewijania) 1f else 0f)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Przewiń w dół",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}