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
    val systemNavigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val bottomPadding = systemNavigationBarsPadding.calculateBottomPadding()

    val buttonHeight = 48.dp // Standardowa wysokość dotykowa
    val iconSize = 26.dp
    val interButtonSpacing = 16.dp
    val horizontalPaddingForBar = 16.dp // Padding dla całego paska od krawędzi ekranu

    val extraPaddingForHighSystemBars = if (bottomPadding > 30.dp && !czyTelefon(configuration)) {
        16.dp
    } else {
        0.dp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Tło dla całego bottomBar
            // WAŻNE: Dodajemy padding systemowy ORAZ nasz dodatkowy padding
            .padding(bottom = bottomPadding + extraPaddingForHighSystemBars)
            .padding(top = 8.dp) // Mały górny padding dla estetyki
    ) {
        // Przyciski pływające (przewijania) - teraz mniejsze i bardziej zintegrowane
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPaddingForBar)
                .heightIn(min = 24.dp), // Minimalna wysokość, nawet jeśli przyciski są ukryte
            horizontalArrangement = Arrangement.SpaceBetween, // Rozmieść przyciski skrajnie
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
                modifier = Modifier.alpha(if (pokazGornyPrzyciskPrzewijania) 1f else 0f) // Ukryj zamiast AnimatedVisibility dla uproszczenia
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Przewiń w górę")
            }

            IconButton(
                onClick = { zakresKorutyn.launch { if(konteneryUISize > 0) stanListy.animateScrollToItem(konteneryUISize - 1) } },
                enabled = pokazDolnyPrzyciskPrzewijania,
                modifier = Modifier.alpha(if (pokazDolnyPrzyciskPrzewijania) 1f else 0f)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Przewiń w dół")
            }
        }

        // Główne przyciski
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPaddingForBar, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier.height(buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                onClick = {
                    homeViewModel.dodajKontener()
                    spowodujSlabaWibracje()
                    zakresKorutyn.launch {
                        // Czekaj na aktualizację UI lub użyj snapshotFlow, jeśli konteneryUISize nie jest od razu aktualne
                        // W tym przypadku konteneryUISize jest przekazywane, więc powinno być OK
                        if (konteneryUISize > 0) { // Po dodaniu rozmiar powinien być > 0
                            // Przewiń do nowo dodanego elementu (nowy rozmiar - 1)
                            // Może być potrzebne małe opóźnienie lub snapshotFlow jeśli konteneryUISize nie odzwierciedla od razu
                            stanListy.animateScrollToItem(homeViewModel.konteneryUI.value.size -1)
                        }
                    }
                }) {
                Icon(
                    modifier = Modifier.size(iconSize),
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_add_24),
                    contentDescription = "Dodaj walutę", // Lepszy content description
                    tint = MaterialTheme.colorScheme.surface
                )
            }

            Spacer(modifier = Modifier.width(interButtonSpacing))

            Button(
                modifier = Modifier.height(buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                onClick = navigateToUlubione
            ) {
                Icon(
                    modifier = Modifier.size(iconSize),
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