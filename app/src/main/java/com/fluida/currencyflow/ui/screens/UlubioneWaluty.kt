package com.fluida.currencyflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import com.fluida.currencyflow.util.UiText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fluida.currencyflow.R
import com.fluida.currencyflow.data.model.Waluta
import com.fluida.currencyflow.data.model.CurrencyType
import com.fluida.currencyflow.ui.components.ElementListyWalut
import com.fluida.currencyflow.viewmodel.FavoriteCurrenciesViewModel
import com.fluida.currencyflow.util.haptics.spowodujPodwojnaSilnaWibracje
import com.fluida.currencyflow.ui.components.MinIloscWalutDialog
import com.fluida.currencyflow.ui.components.UlubioneScreenBottomBar
import androidx.compose.foundation.lazy.items

@Composable
fun CategoryHeader(title: String, fontFamily: FontFamily) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            fontFamily = fontFamily,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

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

    val pogrupowaneWaluty = remember(wszystkieWaluty) {
        wszystkieWaluty.groupBy { it.type }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = UiText.StringResource(R.string.select_favorite_currencies_title).asString(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = czcionkaQuicksand,
                            textAlign = TextAlign.Center,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.rounded_arrow_back_24),
                            contentDescription = UiText.StringResource(R.string.action_back).asString(),
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
    ) { paddingValues ->
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
                pogrupowaneWaluty.forEach { (typ, walutyWKategorii) ->
                    item(key = "header_$typ") {
                        val headerTitle = when (typ) {
                            CurrencyType.FIAT -> UiText.StringResource(R.string.category_fiat).asString()
                            CurrencyType.CRYPTO -> UiText.StringResource(R.string.category_crypto).asString()
                            CurrencyType.METAL -> UiText.StringResource(R.string.category_metals).asString()
                        }
                        CategoryHeader(title = headerTitle, fontFamily = czcionkaQuicksand)
                    }

                    items(
                        items = walutyWKategorii,
                        key = { it.symbol }
                    ) { waluta ->
                        val jestWybrana = aktualnyWyborWalut[waluta] ?: false
                        ElementListyWalut(
                            waluta = waluta,
                            jestWybrana = jestWybrana
                        ) { wybrana ->
                            viewModel.toggleWalutaWybrana(waluta, wybrana)
                        }

                        if (waluta != walutyWKategorii.last()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.background
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    MinIloscWalutDialog(pokazDialog = pokazDialog, zdarzenieZamkniecia = { pokazDialog = false })
}
