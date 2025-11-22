package edu.ucne.farmaciacruz.presentation.login.registro

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.registro.RegisterUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegistroState())
    val state: StateFlow<RegistroState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<RegistroUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: RegistroEvent) {
        when (event) {
            is RegistroEvent.NombreChanged -> handleNombreChanged(event.nombre)
            is RegistroEvent.ApellidoChanged -> handleApellidoChanged(event.apellido)
            is RegistroEvent.EmailChanged -> handleEmailChanged(event.email)
            is RegistroEvent.TelefonoChanged -> handleTelefonoChanged(event.telefono)
            is RegistroEvent.PasswordChanged -> handlePasswordChanged(event.password)
            is RegistroEvent.ConfirmarPasswordChanged -> handleConfirmarPasswordChanged(event.confirmarPassword)
            is RegistroEvent.TerminosChanged -> handleTerminosChanged(event.aceptado)
            is RegistroEvent.RegistrarClicked -> handleRegistrar()
            is RegistroEvent.ClearError -> handleClearError()
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
            registerUseCase(
                email = currentState.email,
                password = currentState.password,
                nombre = currentState.nombre,
                apellido = currentState.apellido,
                telefono = currentState.telefono.ifBlank { null }
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }

                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                user = result.data,
                                error = null
                            )
                        }
                        _uiEvent.emit(RegistroUiEvent.NavigateToHome)
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleClearError() {
        _state.update { it.copy(error = null) }
    }
}