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

@Composable
fun AdaptacyjnyBottomBar(
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

    // Pobierz pełne PaddingValues dla systemowych pasków nawigacyjnych
    val systemNavigationBarsPaddingValues = WindowInsets.navigationBars.asPaddingValues()
    // Nadal potrzebujemy osobno bottomPadding dla specyficznej logiki, ale będziemy też używać całego obiektu
    val bottomPaddingSystemowy = systemNavigationBarsPaddingValues.calculateBottomPadding()


    val buttonHeight = 48.dp
    val iconSize = 26.dp
    val interButtonSpacing = 16.dp
    // Ten padding będzie teraz DODATKIEM do systemowych paddingów bocznych
    val dodatkowyHorizontalPaddingForBar = 16.dp

    val jestPrawdopodobnieSkladakiemRozlozonym =
        (rozmiarEkranu >= Configuration.SCREENLAYOUT_SIZE_LARGE) && bottomPaddingSystemowy > 60.dp
    val stalyDodatkowyPaddingOdDolu = if (jestPoziomo && czyTelefon(configuration)) {
        4.dp
    } else if (jestPrawdopodobnieSkladakiemRozlozonym) {
        8.dp
    } else {
        24.dp
    }

    val gornyPaddingCalegoPaska = if (jestPoziomo && czyTelefon(configuration)) {
        4.dp
    } else {
        8.dp
    }

    val verticalPaddingGlownePrzyciski = if (jestPoziomo && czyTelefon(configuration)) {
        4.dp
    } else {
        8.dp
    }

    val extraPaddingDlaWysokichPaskow = if (bottomPaddingSystemowy > 30.dp && !czyTelefon(configuration) && !jestPrawdopodobnieSkladakiemRozlozonym) {
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
            // Następnie dodajemy nasze specyficzne paddingi pionowe
            .padding(
                top = gornyPaddingCalegoPaska,
                bottom = bottomPaddingSystemowy + extraPaddingDlaWysokichPaskow + stalyDodatkowyPaddingOdDolu
            )
    ) {
        // Przyciski pływające
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Ten padding jest teraz DODATKIEM do systemowych paddingów bocznych już zastosowanych na Column
                .padding(horizontal = dodatkowyHorizontalPaddingForBar)
                .heightIn(min = if (jestPoziomo && czyTelefon(configuration)) 12.dp else 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val pokazGornyPrzyciskPrzewijania by remember {
                derivedStateOf {
                    val layoutInfo = stanListy.layoutInfo
                    if (layoutInfo.totalItemsCount == 0) return@derivedStateOf false
                    (layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0) > 0
                }
            }
            val pokazDolnyPrzyciskPrzewijania by remember {
                derivedStateOf {
                    val layoutInfo = stanListy.layoutInfo
                    if (layoutInfo.totalItemsCount == 0) return@derivedStateOf false
                    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                    lastVisible != null && lastVisible < layoutInfo.totalItemsCount - 1
                }
            }

            // Użyj IconButton dla mniejszych, bardziej kompaktowych przycisków przewijania
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

            IconButton(
                onClick = {
                    zakresKorutyn.launch {
                        if (konteneryUISize > 0) stanListy.animateScrollToItem(
                            konteneryUISize - 1
                        )
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

        // Główne przyciski
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dodatkowyHorizontalPaddingForBar, vertical = verticalPaddingGlownePrzyciski),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = if (jestPoziomo && czyTelefon(configuration)) Alignment.CenterVertically else Alignment.Top
        ) {
            Button(
                modifier = Modifier.height(if (jestPoziomo && czyTelefon(configuration)) 36.dp else buttonHeight),
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
                    modifier = Modifier.size(if (jestPoziomo && czyTelefon(configuration)) 20.dp else iconSize),
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_add_24),
                    contentDescription = "Dodaj walutę",
                    tint = MaterialTheme.colorScheme.surface
                )
            }

            Spacer(modifier = Modifier.width(interButtonSpacing))

            Button(
                modifier = Modifier.height(if (jestPoziomo && czyTelefon(configuration)) 36.dp else buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                onClick = navigateToUlubione
            ) {
                Icon(
                    modifier = Modifier.size(if (jestPoziomo && czyTelefon(configuration)) 20.dp else iconSize),
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_favorite_border_24),
                    contentDescription = "Ulubione waluty", // Lepszy content description
                    tint = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

fun czyTelefon(aktywnosc: Configuration): Boolean {
    val screenLayoutInt = aktywnosc.screenLayout
    return screenLayoutInt and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_NORMAL

}