package com.example.currencyflow.interfejs_uzytkownika

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.klasy.Waluta
import com.example.currencyflow.dane.WybraneWalutyViewModel
import com.example.currencyflow.dane.zarzadzanie_danymi.WybraneWalutyViewModelFactory
import com.example.currencyflow.haptyka.spowodujPodwojnaSilnaWibracje
import com.example.currencyflow.interfejs_uzytkownika.komponenty.PoleWyboru
import com.example.currencyflow.interfejs_uzytkownika.komponenty.MinIloscWalutDialog

@Composable
fun UlubioneWaluty(navController: NavController) {
    val context = LocalContext.current
    val viewModel: WybraneWalutyViewModel = viewModel(
        factory = WybraneWalutyViewModelFactory(context)
    )
    val wszystkieWaluty = Waluta.entries.toList()

    // Obserwowanie zmian w zaznaczonych walutach
    val wybraneWaluty by viewModel.wybraneWaluty.collectAsState()

    // Inicjalizacja walut w ViewModel tylko raz (w LaunchedEffect)
    LaunchedEffect(key1 = context) {
        val poczatkowoWybraneWaluty = viewModel.zdobadzWybraneWaluty() // Pobierz wybrane waluty z repozytorium (jeśli już były zapisane)
        viewModel.inicjalizacjaWalut(wszystkieWaluty, poczatkowoWybraneWaluty)
    }

    var pokazDialog by remember { mutableStateOf(false) }
    val czcionkaQuicksand = FontFamily(
        Font(R.font.quicksand_variable, FontWeight.Normal)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .weight(0.09f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoxWithConstraints {
                if (maxWidth < 600.dp) {
                    Text(
                        text = "Wybierz ulubioną walutę",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = czcionkaQuicksand
                    )
                } else {
                    Text(
                        text = "Wybierz ulubioną walutę",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = czcionkaQuicksand
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(0.77f)
        ) {
            items(wszystkieWaluty) { waluta ->
                val jestWybrana = wybraneWaluty[waluta] ?: false
                ElementListyWalut(
                    waluta = waluta,
                    jestWybrana = jestWybrana
                ) { wybrana ->
                    viewModel.zaktualizujWybraneWaluty(waluta, wybrana)
                }
            }
        }
        Row(
            modifier = Modifier
                .weight(0.14f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                ),
                onClick = {
                    val listaWybranychWalut = viewModel.zdobadzWybraneWaluty()
                    if (listaWybranychWalut.size >= 2) {
                    viewModel.zapiszWybraneWaluty()
                        navController.navigateUp() // Powrót do poprzedniego ekranu
                    } else {
                        spowodujPodwojnaSilnaWibracje(context)
                        pokazDialog = true
                    }
                }
            ) {
                Icon(
                    modifier = Modifier
                        .size(26.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_save_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
    MinIloscWalutDialog(pokazDialog = pokazDialog, zdarzenieZamkniecia = { pokazDialog = false })
}

@Composable
fun ElementListyWalut(
    waluta: Waluta,
    jestWybrana: Boolean,
    zdarzenieWybranejWaluty: (Boolean) -> Unit
) {
    val zrodloIteracji = remember { MutableInteractionSource() }
    rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                interactionSource = zrodloIteracji,
                indication = null, // Wyłączamy domyślny feedback
                onClick = {
                    zdarzenieWybranejWaluty(!jestWybrana)
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                modifier = Modifier
                    .size(36.dp),
                painter = painterResource(id = waluta.icon),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = waluta.symbol, color = MaterialTheme.colorScheme.onSurface)
        }
        PoleWyboru(
            zaznaczone = jestWybrana,
            zdarzeniaZmianyZaznaczenia = { checked ->
                zdarzenieWybranejWaluty(checked)
            },
            modyfikator = Modifier.wrapContentSize()
        )
    }
}

