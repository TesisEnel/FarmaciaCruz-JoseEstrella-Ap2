package edu.ucne.farmaciacruz.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.farmaciacruz.R
import edu.ucne.farmaciacruz.ui.theme.onPrimaryContainerDarkHighContrast

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegistroClick: () -> Unit = {},
    onOlvidoPasswordClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUiEvent.NavigateToHome -> onLoginSuccess()
            }
        }
    }

    LoginContent(
        state = state,
        passwordVisible = passwordVisible,
        onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
        onEvent = viewModel::onEvent,
        onRegistroClick = onRegistroClick,
        onOlvidoPasswordClick = onOlvidoPasswordClick
    )
}

@Composable
private fun LoginContent(
    state: LoginState,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    onEvent: (LoginEvent) -> Unit,
    onRegistroClick: () -> Unit,
    onOlvidoPasswordClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(onPrimaryContainerDarkHighContrast)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginLogo()

            Spacer(modifier = Modifier.height(28.dp))

            LoginCard(
                state = state,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = onPasswordVisibilityChange,
                onEvent = onEvent,
                onOlvidoPasswordClick = onOlvidoPasswordClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginFooter(onRegistroClick)

            LoginError(state.error, onEvent)
        }
    }
}

@Composable
private fun LoginLogo() {
    Image(
        painter = painterResource(id = R.drawable.logo_farmacia_cruz),
        contentDescription = null,
        modifier = Modifier.size(250.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Tu salud es nuestra prioridad",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
        fontSize = 12.sp
    )
}

@Composable
private fun LoginCard(
    state: LoginState,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    onEvent: (LoginEvent) -> Unit,
    onOlvidoPasswordClick: () -> Unit
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
                .padding(28.dp)
        ) {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(28.dp))

            LoginEmailField(state, onEvent)

            Spacer(modifier = Modifier.height(20.dp))

            LoginPasswordField(state, passwordVisible, onPasswordVisibilityChange, onEvent)

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onOlvidoPasswordClick,
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Forgot Password?",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LoginButton(state, onEvent)
        }
    }
}

@Composable
private fun LoginEmailField(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit
) {
    Text(
        text = "Email",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = state.email,
        onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("example@email.com") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        enabled = !state.isLoading,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun LoginPasswordField(
    state: LoginState,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    onEvent: (LoginEvent) -> Unit
) {
    Text(
        text = "Password",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = state.password,
        onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("At least 8 characters") },
        visualTransformation = if (passwordVisible) VisualTransformation.None
        else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onPasswordVisibilityChange) {
                Icon(
                    if (passwordVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        enabled = !state.isLoading,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun LoginButton(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit
) {
    Button(
        onClick = { onEvent(LoginEvent.LoginClicked) },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = !state.isLoading &&
                state.email.isNotBlank() &&
                state.password.isNotBlank(),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                "Sign In",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun LoginFooter(onRegistroClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Don't have an account?",
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.width(4.dp))
        TextButton(onClick = onRegistroClick) {
            Text(
                text = "Sign up",
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoginError(
    error: String?,
    onEvent: (LoginEvent) -> Unit
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
                    onClick = { onEvent(LoginEvent.ClearError) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Text("✕", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun LoginPreview() {
    MaterialTheme {
        LoginContent(
            state = LoginState("", "", false, null),
            passwordVisible = false,
            onPasswordVisibilityChange = {},
            onEvent = {},
            onRegistroClick = {},
            onOlvidoPasswordClick = {}
        )
    }
}
