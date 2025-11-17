package edu.ucne.farmaciacruz.presentation.login.registro

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.data.local.PreferencesManager
import edu.ucne.farmaciacruz.data.remote.api.ApiService
import edu.ucne.farmaciacruz.data.remote.request.RegisterRequest
import edu.ucne.farmaciacruz.domain.model.User
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.compareTo

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(RegistroState())
    val state: StateFlow<RegistroState> = _state.asStateFlow()

    private val _event = Channel<RegistroEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun processIntent(intent: RegistroIntent) {
        when (intent) {
            is RegistroIntent.NombreChanged -> handleNombreChanged(intent.nombre)
            is RegistroIntent.ApellidoChanged -> handleApellidoChanged(intent.apellido)
            is RegistroIntent.EmailChanged -> handleEmailChanged(intent.email)
            is RegistroIntent.TelefonoChanged -> handleTelefonoChanged(intent.telefono)
            is RegistroIntent.PasswordChanged -> handlePasswordChanged(intent.password)
            is RegistroIntent.ConfirmarPasswordChanged -> handleConfirmarPasswordChanged(intent.confirmarPassword)
            is RegistroIntent.TerminosChanged -> handleTerminosChanged(intent.aceptado)
            is RegistroIntent.RegistrarClicked -> handleRegistrar()
            is RegistroIntent.ClearError -> handleClearError()
        }
    }

    private fun handleNombreChanged(nombre: String) {
        _state.update { it.copy(nombre = nombre) }
    }

    private fun handleApellidoChanged(apellido: String) {
        _state.update { it.copy(apellido = apellido) }
    }

    private fun handleEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    private fun handleTelefonoChanged(telefono: String) {
        _state.update { it.copy(telefono = telefono) }
    }

    private fun handlePasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    private fun handleConfirmarPasswordChanged(confirmarPassword: String) {
        _state.update { it.copy(confirmarPassword = confirmarPassword) }
    }

    private fun handleTerminosChanged(aceptado: Boolean) {
        _state.update { it.copy(aceptaTerminos = aceptado) }
    }

    private fun handleRegistrar() {
        val currentState = _state.value

        // Validaciones
        if (currentState.nombre.isBlank()) {
            _state.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        if (currentState.apellido.isBlank()) {
            _state.update { it.copy(error = "El apellido es obligatorio") }
            return
        }

        if (currentState.email.isBlank()) {
            _state.update { it.copy(error = "El email es obligatorio") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(error = "Email inválido") }
            return
        }

        if (currentState.password.isBlank()) {
            _state.update { it.copy(error = "La contraseña es obligatoria") }
            return
        }

        if (currentState.password.length < 8) {
            _state.update { it.copy(error = "La contraseña debe tener al menos 8 caracteres") }
            return
        }

        if (currentState.password != currentState.confirmarPassword) {
            _state.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        if (!currentState.aceptaTerminos) {
            _state.update { it.copy(error = "Debes aceptar los términos y condiciones") }
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                val request = RegisterRequest(
                    email = currentState.email,
                    password = currentState.password,
                    nombre = currentState.nombre,
                    apellido = currentState.apellido,
                    telefono = currentState.telefono.ifBlank { null }
                )

                val response = apiService.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!.data!!

                    preferencesManager.saveToken(authResponse.token)
                    preferencesManager.saveRefreshToken(authResponse.refreshToken)

                    preferencesManager.saveUserData(
                        userId = authResponse.usuario.usuarioId,
                        email = authResponse.usuario.email,
                        name = "${authResponse.usuario.nombre} ${authResponse.usuario.apellido}",
                        role = authResponse.usuario.rol
                    )

                    val user = User(
                        id = authResponse.usuario.usuarioId,
                        email = authResponse.usuario.email,
                        nombre = authResponse.usuario.nombre,
                        apellido = authResponse.usuario.apellido,
                        telefono = authResponse.usuario.telefono,
                        rol = authResponse.usuario.rol
                    )

                    _state.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            error = null
                        )
                    }

                    _event.send(RegistroEvent.ShowSuccess("Registro exitoso"))
                    _event.send(RegistroEvent.NavigateToHome)
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Datos inválidos"
                        409 -> "El email ya está registrado"
                        else -> response.body()?.mensaje ?: "Error al registrarse"
                    }
                    _state.update { it.copy(isLoading = false, error = errorMessage) }
                    _event.send(RegistroEvent.ShowError(errorMessage))
                }
            } catch (e: Exception) {
                val errorMessage = "Error de conexión. Verifica tu internet"
                _state.update { it.copy(isLoading = false, error = errorMessage) }
                _event.send(RegistroEvent.ShowError(errorMessage))
            }
        }
    }

    private fun handleClearError() {
        _state.update { it.copy(error = null) }
    }
}