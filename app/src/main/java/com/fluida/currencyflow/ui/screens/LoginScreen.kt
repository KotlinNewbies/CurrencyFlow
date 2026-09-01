package com.fluida.currencyflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.fluida.currencyflow.R
import com.fluida.currencyflow.ui.navigation.Nawigacja
import com.fluida.currencyflow.util.UiText
import com.fluida.currencyflow.viewmodel.AuthUiState
import com.fluida.currencyflow.viewmodel.AuthViewModel

private val czcionkaQuicksand = FontFamily(
    Font(R.font.quicksand_variable, FontWeight.Normal)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Nasłuchiwanie na sukces rejestracji z poprzedniego ekranu
    val regSuccessState = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<Boolean?>("registration_success", null)
        ?.collectAsState()

    var showRegSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(regSuccessState?.value) {
        if (regSuccessState?.value == true) {
            showRegSuccessDialog = true
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("registration_success")
        }
    }

    val blurRadius by animateDpAsState(
        targetValue = if (showRegSuccessDialog) 4.dp else 0.dp,
        label = "blurAnimation"
    )

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                android.widget.Toast.makeText(context, state.message.asString(context), android.widget.Toast.LENGTH_SHORT).show()
                navController.navigateUp()
                viewModel.resetState()
            }
            is AuthUiState.Error -> {
                snackbarHostState.showSnackbar(state.message.asString(context))
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.blur(blurRadius),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = UiText.StringResource(R.string.login_title).asString(),
                        fontFamily = czcionkaQuicksand,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.rounded_arrow_back_24),
                            contentDescription = UiText.StringResource(R.string.action_back).asString(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(UiText.StringResource(R.string.login_username_label).asString(), fontFamily = czcionkaQuicksand) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(UiText.StringResource(R.string.login_password_label).asString(), fontFamily = czcionkaQuicksand) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    Text(
                        text = UiText.StringResource(R.string.btn_login).asString(),
                        fontFamily = czcionkaQuicksand,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        navController.navigate(Nawigacja.Register.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    Text(
                        text = UiText.StringResource(R.string.btn_signup).asString(),
                        fontFamily = czcionkaQuicksand,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (showRegSuccessDialog) {
        RegistrationSuccessDialog(
            onDismiss = { showRegSuccessDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationSuccessDialog(onDismiss: () -> Unit) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_account_circle_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Weryfikacja e-mail",
                    fontFamily = czcionkaQuicksand,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Konto zostało utworzone. Proszę teraz zweryfikować e-mail w celu dokończenia rejestracji konta.",
                    fontFamily = czcionkaQuicksand,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "OK",
                        fontFamily = czcionkaQuicksand,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
