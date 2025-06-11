package com.example.currencyflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.ui.components.ElementListyWalut
import com.example.currencyflow.viewmodel.FavoriteCurrenciesViewModel
import com.example.currencyflow.util.haptics.spowodujPodwojnaSilnaWibracje
import com.example.currencyflow.ui.components.MinIloscWalutDialog
import com.example.currencyflow.ui.components.UlubioneScreenBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UlubioneWaluty(
    navController: NavController,
    viewModel: FavoriteCurrenciesViewModel
) {
    val context = LocalContext.current
    val wszystkieWaluty by viewModel.wszystkieWaluty.collectAsState()
    val aktualnyWyborWalut by viewModel.aktualnyWyborWalut.collectAsState()
    var pokazDialog by remember { mutableStateOf(false) }
    val czcionkaQuicksand = FontFamily(Font(R.font.quicksand_variable, FontWeight.Normal)) // Można wynieść poza funkcję, jeśli stała

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.select_favorite_currencies_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = czcionkaQuicksand, // Możesz chcieć ujednolicić czcionkę lub zostawić specyficzną
                            textAlign = TextAlign.Center,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            UlubioneScreenBottomBar(
                navController = navController,
                viewModel = viewModel,
                pokazDialogUpdate = { pokazDialog = it },
                spowodujPodwojnaSilnaWibracje = { spowodujPodwojnaSilnaWibracje(context) }
            )
        }
    ) { paddingValues -> // paddingValues dostarczone przez Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                items(wszystkieWaluty) { waluta ->
                    val jestWybrana = aktualnyWyborWalut[waluta] ?: false
                    ElementListyWalut(
                        waluta = waluta,
                        jestWybrana = jestWybrana
                    ) { wybrana ->
                        viewModel.toggleWalutaWybrana(waluta, wybrana)
                    }
                }
            }
        }
    }
    MinIloscWalutDialog(pokazDialog = pokazDialog, zdarzenieZamkniecia = { pokazDialog = false })
}