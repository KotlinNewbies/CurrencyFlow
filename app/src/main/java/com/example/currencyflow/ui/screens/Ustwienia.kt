package com.example.currencyflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.viewmodel.SettingsViewModel

private val czcionkaQuicksand = FontFamily(
    Font(R.font.quicksand_variable, FontWeight.Normal)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ustawienia(
    navController: NavController,
    vieModel: SettingsViewModel = hiltViewModel()
){
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = "Ustawienia",
                    fontFamily = czcionkaQuicksand,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 35.sp
                ) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Wróć"
                        )
                    }
                }
            )
        },
        bottomBar = {

        }
    ) { paddingValues ->
        LazyColumn( // Użyj LazyColumn, jeśli lista ustawień może być długa
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            item {
                //SettingsSectionHeader(title = "Ogólne")
            }
            item {
//                SettingSwitchItem(
//                    title = "Włącz powiadomienia",
//                    checked = notificationsEnabled,
//                    onCheckedChange = {
//                        notificationsEnabled = it
//                        // viewModel.saveNotificationSetting(it)
//                    },
//                    icon = Icons.Default.Notifications
//                )
            }
            item {
//                SettingClickableItem(
//                    title = "Motyw aplikacji",
//                    onClick = {
//                        // TODO: Pokaż dialog wyboru motywu lub nawiguj do ekranu motywów
//                        // np. navController.navigate("theme_selection_screen")
//                    },
//                    icon = Icons.Default.Palette
//                )
            }

            item {
                //SettingsSectionHeader(title = "Informacje")
            }
            item {
//                SettingClickableItem(
//                    title = "O aplikacji",
//                    onClick = {
//                        // TODO: Nawiguj do ekranu "O aplikacji"
//                        // np. navController.navigate("about_screen")
//                    },
//                    icon = Icons.Default.Info
//                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Wersja aplikacji: 1.0.0", // TODO: Pobierz dynamicznie
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}