package com.fluida.currencyflow.interfejs_uzytkownika

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fluida.currencyflow.CurrencyFlowApplication
import com.fluida.currencyflow.data.LanguageManager
import com.fluida.currencyflow.ui.navigation.Nawigacja
import com.fluida.currencyflow.viewmodel.FavoriteCurrenciesViewModel
import com.fluida.currencyflow.viewmodel.HomeViewModel
import com.fluida.currencyflow.interfejs_uzytkownika.theme.CurrencyFlowTheme
import com.fluida.currencyflow.ui.screens.GlownyEkran
import com.fluida.currencyflow.ui.screens.UlubioneWaluty
import com.fluida.currencyflow.ui.screens.SettingsScreen
import com.fluida.currencyflow.ui.screens.LoginScreen
import com.fluida.currencyflow.ui.screens.RegisterScreen
import com.fluida.currencyflow.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject

private const val ADMOB_TAG_MAIN = "AdMobMainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG_LIFECYCLE = "MainActivityLifecycle"

    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        Log.d(TAG_LIFECYCLE, "onCreate CALLED - Instance: $this")
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        setContent {
            // Obserwuj stan załadowania języka
            val isLanguageLoaded by languageManager.initialLanguageLoaded.collectAsState()
            var isAdSdkInitialized by remember { mutableStateOf(false) }
            val currentContext = LocalContext.current
            LaunchedEffect(isLanguageLoaded) {
                if (isLanguageLoaded) {
                    Log.d(TAG_LIFECYCLE, "Language state is stable. Initializing AdMob.")
                    
                    delay(800.milliseconds) // Zwiększone opóźnienie dla stabilności

                    CurrencyFlowApplication.initializeMobileAdsSdk(currentContext.applicationContext) {
                        Log.d(ADMOB_TAG_MAIN, "AdMob SDK initialized callback in MainActivity.")
                        isAdSdkInitialized = true
                    }
                }
            }

            if (isLanguageLoaded) {
                // Jeśli język jest załadowany, wyświetl główne UI aplikacji
                CurrencyFlowTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = Nawigacja.Dom.route
                        ) {
                            composable(Nawigacja.Dom.route) {
                                val homeViewModel: HomeViewModel = hiltViewModel()
                                GlownyEkran(
                                    aktywnosc = this@MainActivity,
                                    kontrolerNawigacji = navController,
                                    homeViewModel = homeViewModel
                                )
                            }
                            composable(Nawigacja.UlubioneWaluty.route) {
                                val favoriteCurrenciesViewModel: FavoriteCurrenciesViewModel = hiltViewModel()
                                UlubioneWaluty(
                                    navController = navController,
                                    viewModel = favoriteCurrenciesViewModel
                                )
                            }
                            composable(Nawigacja.Ustawenia.route) {
                                val settingsViewModel: SettingsViewModel = hiltViewModel()
                                SettingsScreen(
                                    navController = navController,
                                    viewModel = settingsViewModel
                                )
                            }
                            composable(Nawigacja.Login.route) {
                                LoginScreen(navController = navController)
                            }
                            composable(Nawigacja.Register.route) {
                                RegisterScreen(navController = navController)
                            }
                        }
                    }
                }
            } else {
                CurrencyFlowTheme { // Użyj motywu, aby ekran ładowania wyglądał spójnie
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
