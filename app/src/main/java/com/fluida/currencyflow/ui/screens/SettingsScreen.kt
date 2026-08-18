package com.fluida.currencyflow.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.fluida.currencyflow.util.UiText
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.fluida.currencyflow.R
import com.fluida.currencyflow.ui.components.LanguageSelectionDialog
import com.fluida.currencyflow.ui.components.SettingsScreenBottomBar
import com.fluida.currencyflow.viewmodel.SettingsViewModel

private fun Context.findActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}

private val czcionkaQuicksand = FontFamily(
    Font(R.font.quicksand_variable, FontWeight.Normal)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val availableLanguages = viewModel.availableLanguages
    val currentLanguageTag by viewModel.currentLanguageTag.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context.findActivity()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = UiText.StringResource(R.string.settings_title).asString(),
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
                            contentDescription = UiText.StringResource(R.string.action_back).asString(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            SettingsScreenBottomBar(
                navController = navController
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingItem(
                    title = UiText.StringResource(R.string.language_setting_title).asString(),
                    currentValue = availableLanguages.find { it.tag == currentLanguageTag }
                        ?.let { UiText.StringResource(it.displayNameResId).asString() }
                        ?: UiText.StringResource(R.string.language_system_default).asString(),
                    onClick = { showLanguageDialog = true }
                )
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            availableLanguages = availableLanguages,
            initiallySelectedTag = currentLanguageTag, // Przekaż aktualnie aktywny język jako początkowy wybór
            onApply = { selectedTag -> // Ten kod zostanie wykonany po kliknięciu "Zastosuj" w dialogu
                if (activity != null && selectedTag != currentLanguageTag) { // Zastosuj tylko jeśli jest faktyczna zmiana
                    viewModel.changeLanguage(selectedTag, activity)
                }
                showLanguageDialog = false // Zamknij dialog
            },
            onDismiss = { showLanguageDialog = false } // Zamknij dialog bez zmian
        )
    }
}

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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = currentValue, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

