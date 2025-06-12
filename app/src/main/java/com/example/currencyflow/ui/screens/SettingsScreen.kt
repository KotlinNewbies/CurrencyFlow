package com.example.currencyflow.ui.screens

import android.content.Context // Potrzebne dla getAppVersion
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column // Dodane dla elastyczności, jeśli potrzebne
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer // Dodane
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height // Dodane
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Dodane
import androidx.compose.material.icons.Icons // Dodane
import androidx.compose.material.icons.filled.Check // Dodane
import androidx.compose.material3.AlertDialog // Dodane
import androidx.compose.material3.Card // Dodane
import androidx.compose.material3.CardDefaults // Dodane
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Dodane
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Dodane
import androidx.compose.runtime.mutableStateOf // Dodane
import androidx.compose.runtime.remember // Dodane
import androidx.compose.runtime.setValue // Dodane
import androidx.compose.runtime.collectAsState // Dodane
import androidx.compose.ui.Alignment // Dodane
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // Dodane
import androidx.compose.ui.res.stringResource // Dodane
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // Dodane
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.currencyflow.R
import com.example.currencyflow.data.LanguageOption // Upewnij się, że ścieżka jest poprawna
// Poprawiona ścieżka do SettingsViewModel, jeśli wcześniej była inna
import com.example.currencyflow.ui.components.SettingsScreenBottomBar // Zakładam, że ten komponent istnieje
import com.example.currencyflow.viewmodel.SettingsViewModel

private val czcionkaQuicksand = FontFamily(
    Font(R.font.quicksand_variable, FontWeight.Normal)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    // Upewnij się, że typ ViewModelu jest poprawny i że Hilt go dostarcza
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val availableLanguages = viewModel.availableLanguages
    val currentLanguageTag by viewModel.currentLanguageTag.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        // Użyj stringResource do pobrania tłumaczenia
                        text = stringResource(id = R.string.settings_title),
                        fontFamily = czcionkaQuicksand,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 35.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.rounded_arrow_back_24),
                            // Użyj stringResource dla contentDescription
                            contentDescription = stringResource(id = R.string.action_back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Zakładam, że SettingsScreenBottomBar nie wymaga zmian związanych z językiem
            // Jeśli tak, musisz również tam wprowadzić stringResource
            SettingsScreenBottomBar(
                navController = navController
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues) // Dodaj oryginalne paddingValues z Scaffold
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Dodatkowy padding dla zawartości listy
            verticalArrangement = Arrangement.spacedBy(12.dp) // Odstęp między elementami listy
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp)) // Mały odstęp od góry
                SettingItem(
                    title = stringResource(id = R.string.language_setting_title),
                    currentValue = availableLanguages.find { it.tag == currentLanguageTag }
                        ?.let { stringResource(id = it.displayNameResId) }
                        ?: stringResource(id = R.string.language_system_default),
                    onClick = { showLanguageDialog = true }
                )
            }

            // Tutaj możesz dodać inne elementy ustawień w przyszłości
            // item {
            //     SettingItem(title = "Inne Ustawienie", currentValue = "Wartość", onClick = { /* ... */ })
            // }

            item {
                // Informacja o wersji aplikacji na dole
                Column( // Użyj Column, aby wycentrować tekst i dać mu pełną szerokość
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp), // Większy padding na dole
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${stringResource(id = R.string.version_info_prefix)} ${getAppVersion(context)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            availableLanguages = availableLanguages,
            currentLanguageTag = currentLanguageTag,
            onLanguageSelected = { languageTag ->
                viewModel.changeLanguage(languageTag)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

// Ten komponent można przenieść do osobnego pliku, jeśli będzie reużywany
@Composable
fun SettingItem(
    title: String,
    currentValue: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Mniejsza elewacja dla subtelniejszego wyglądu
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Dopasuj kolory
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp), // Zwiększony vertical padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = currentValue, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    availableLanguages: List<LanguageOption>,
    currentLanguageTag: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.language_selection_dialog_title)) },
        text = {
            // Używamy LazyColumn, jeśli lista języków mogłaby być długa
            LazyColumn {
                items(availableLanguages) { langOption ->
                    LanguageDialogRow(
                        languageName = stringResource(id = langOption.displayNameResId),
                        isSelected = langOption.tag == currentLanguageTag,
                        onClick = { onLanguageSelected(langOption.tag) }
                    )
                }
            }
        },
        confirmButton = {
            // W tym przypadku nie potrzebujemy przycisku "Potwierdź",
            // bo wybór języka od razu zamyka dialog.
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_cancel).uppercase()) // Można dać uppercase dla standardowego wyglądu przycisku
            }
        }
    )
}

@Composable
fun LanguageDialogRow(
    languageName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 24.dp), // Zwiększony padding dla lepszej klikalności
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = languageName,
            modifier = Modifier.weight(1f),
            style = if (isSelected) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(id = R.string.selected_language_indicator),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// Pomocnicza funkcja do pobrania wersji aplikacji
fun getAppVersion(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "N/A" // <<<< ZMIANA TUTAJ
    } catch (e: Exception) {
        // Możesz tutaj dodać logowanie błędu, np. Log.e("SettingsScreen", "Error getting app version", e)
        "N/A" // Zwróć "N/A" lub pusty string w przypadku błędu
    }
}