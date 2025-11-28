package edu.ucne.farmaciacruz.presentation.login.registro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.farmaciacruz.R
import edu.ucne.farmaciacruz.ui.theme.onPrimaryContainerDarkHighContrast

sealed class RegistroUiEvent {
    data object NavigateToHome : RegistroUiEvent()
}

@Composable
fun RegistroScreen(
    onRegistroSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is RegistroUiEvent.NavigateToHome) {
                onRegistroSuccess()
            }
        }
    }

    RegistroContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackToLogin = onBackToLogin
    )
}

@Composable
private fun RegistroContent(
    state: RegistroState,
    onEvent: (RegistroEvent) -> Unit,
    onBackToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(onPrimaryContainerDarkHighContrast)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            LogoHeader()
            RegistroTitle()
            Spacer(Modifier.height(24.dp))

            RegistroForm(
                state = state,
                onEvent = onEvent,
                onBackToLogin = onBackToLogin
            )

            ErrorCardIfNeeded(
                error = state.error,
                onClear = { onEvent(RegistroEvent.ClearError) }
            )
        }
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
                contentDescription = "Farmacia Cruz Logo",
                modifier = Modifier.size(180.dp)
            )
        }
    }
}

@Composable
private fun RegistroTitle() {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Crear Cuenta",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Únete a Farmacia Cruz",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
    )
}

@Composable
private fun RegistroForm(
    state: RegistroState,
    onEvent: (RegistroEvent) -> Unit,
    onBackToLogin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {

        var passwordVisible by remember { mutableStateOf(false) }
        var confirmarPasswordVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            RegistroTextField(
                label = "Nombre",
                value = state.nombre,
                placeholder = "José Gabriel",
                icon = Icons.Default.Person,
                onChange = { onEvent(RegistroEvent.NombreChanged(it)) },
                enabled = !state.isLoading
            )

            RegistroTextField(
                label = "Apellido",
                value = state.apellido,
                placeholder = "Estrella",
                icon = Icons.Default.Person,
                onChange = { onEvent(RegistroEvent.ApellidoChanged(it)) },
                enabled = !state.isLoading
            )

            RegistroTextField(
                label = "Correo electrónico",
                value = state.email,
                placeholder = "ejemplo@correo.com",
                icon = Icons.Default.Email,
                onChange = { onEvent(RegistroEvent.EmailChanged(it)) },
                keyboardType = KeyboardType.Email,
                enabled = !state.isLoading
            )

            RegistroTextField(
                label = "Teléfono",
                value = state.telefono,
                placeholder = "+1 829 230 1111",
                icon = Icons.Default.Phone,
                onChange = { onEvent(RegistroEvent.TelefonoChanged(it)) },
                keyboardType = KeyboardType.Phone,
                enabled = !state.isLoading
            )

            RegistroPasswordField(
                label = "Contraseña",
                value = state.password,
                visible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                onChange = { onEvent(RegistroEvent.PasswordChanged(it)) },
                enabled = !state.isLoading
            )

            RegistroPasswordField(
                label = "Confirmar contraseña",
                value = state.confirmarPassword,
                visible = confirmarPasswordVisible,
                onToggleVisibility = { confirmarPasswordVisible = !confirmarPasswordVisible },
                onChange = { onEvent(RegistroEvent.ConfirmarPasswordChanged(it)) },
                enabled = !state.isLoading
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.aceptaTerminos,
                    onCheckedChange = { onEvent(RegistroEvent.TerminosChanged(it)) },
                    enabled = !state.isLoading
                )
                Text(
                    text = "He leído y acepto los términos y condiciones de Farmacia Cruz",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            RegistroSubmitButton(
                isLoading = state.isLoading,
                onClick = { onEvent(RegistroEvent.RegistrarClicked) }
            )

            AlreadyHaveAccount(onBackToLogin)
        }
    }
}

@Composable
private fun RegistroTextField(
    label: String,
    value: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onChange: (String) -> Unit,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Text(label, fontWeight = FontWeight.Medium)

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, null) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun RegistroPasswordField(
    label: String,
    value: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    onChange: (String) -> Unit,
    enabled: Boolean
) {
    Text(label, fontWeight = FontWeight.Medium)

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("········") },
        leadingIcon = { Icon(Icons.Default.Lock, null) },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun RegistroSubmitButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                "Registrarse",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AlreadyHaveAccount(onBackToLogin: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text("¿Ya tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onBackToLogin) {
            Text("Inicia sesión", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ErrorCardIfNeeded(error: String?, onClear: () -> Unit) {
    error?.let {
        Spacer(modifier = Modifier.height(16.dp))

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClear) {
                    Text("✕", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistroScreenPreview() {
    MaterialTheme {
        RegistroContent(
            state = RegistroState(
                nombre = "",
                apellido = "",
                email = "",
                telefono = "",
                password = "",
                confirmarPassword = "",
                aceptaTerminos = false,
                isLoading = false,
                error = null
            ),
            onEvent = {},
            onBackToLogin = {}
        )
    }
}
