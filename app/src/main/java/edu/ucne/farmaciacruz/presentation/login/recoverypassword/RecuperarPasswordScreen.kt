package edu.ucne.farmaciacruz.presentation.login.recoverypassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.farmaciacruz.R
import edu.ucne.farmaciacruz.ui.theme.onPrimaryContainerDarkHighContrast

@Composable
fun RecuperarPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: RecuperarPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is RecuperarPasswordUiEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)

                is RecuperarPasswordUiEvent.ShowSuccess ->
                    snackbarHostState.showSnackbar(event.message)

                is RecuperarPasswordUiEvent.NavigateToLogin ->
                    onBackToLogin()
            }
        }
    }

    RecuperarPasswordContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun RecuperarPasswordContent(
    state: RecuperarPasswordState,
    snackbarHostState: SnackbarHostState,
    onEvent: (RecuperarPasswordEvent) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(onPrimaryContainerDarkHighContrast)
                .padding(padding)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            BackButton(
                modifier = Modifier.align(Alignment.TopStart),
                onClick = { onEvent(RecuperarPasswordEvent.VolverLogin) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LogoHeader()
                RecoveryDescription()
                Spacer(Modifier.height(32.dp))
                RecoveryCard(state, onEvent)
                RecoveryError(state.error, onEvent)
            }
        }
    }
}

@Composable
private fun BackButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.padding(16.dp)
    ) {
        Icon(
            Icons.Default.ArrowBack,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun LogoHeader() {
    Surface(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.logo_farmacia_cruz),
                contentDescription = null,
                modifier = Modifier.size(180.dp)
            )
        }
    }
}

@Composable
private fun RecoveryDescription() {
    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "¿Olvidaste tu contraseña?",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Ingresa tu correo para recuperarla",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun RecoveryCard(
    state: RecuperarPasswordState,
    onEvent: (RecuperarPasswordEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmailField(state, onEvent)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Te enviaremos un enlace para\nrestablecer tu contraseña.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            SendButton(state, onEvent)
            Spacer(Modifier.height(16.dp))
            BackToLoginButton(state, onEvent)
        }
    }
}

@Composable
private fun EmailField(
    state: RecuperarPasswordState,
    onEvent: (RecuperarPasswordEvent) -> Unit
) {
    Text(
        text = "Correo electrónico",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )

    OutlinedTextField(
        value = state.email,
        onValueChange = { onEvent(RecuperarPasswordEvent.EmailChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("ejemplo@correo.com") },
        leadingIcon = {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        enabled = !state.isLoading && !state.emailEnviado,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun SendButton(
    state: RecuperarPasswordState,
    onEvent: (RecuperarPasswordEvent) -> Unit
) {
    Button(
        onClick = { onEvent(RecuperarPasswordEvent.EnviarClicked) },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = !state.isLoading && !state.emailEnviado && state.email.isNotBlank(),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = if (state.emailEnviado) "Enlace enviado ✓" else "Enviar enlace",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BackToLoginButton(
    state: RecuperarPasswordState,
    onEvent: (RecuperarPasswordEvent) -> Unit
) {
    TextButton(
        onClick = { onEvent(RecuperarPasswordEvent.VolverLogin) },
        enabled = !state.isLoading
    ) {
        Text(
            text = "Volver al inicio de sesión",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RecoveryError(
    error: String?,
    onEvent: (RecuperarPasswordEvent) -> Unit
) {
    error?.let {
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onEvent(RecuperarPasswordEvent.ClearError) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Text("✕", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}
