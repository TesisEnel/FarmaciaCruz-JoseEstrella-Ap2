package edu.ucne.farmaciacruz.presentation.login.recoverypassword

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.data.remote.api.ApiService
import edu.ucne.farmaciacruz.data.remote.request.RecoveryRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecuperarPasswordViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(RecuperarPasswordState())
    val state: StateFlow<RecuperarPasswordState> = _state.asStateFlow()

    private val _event = Channel<RecuperarPasswordEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun processIntent(intent: RecuperarPasswordIntent) {
        when (intent) {
            is RecuperarPasswordIntent.EmailChanged -> handleEmailChanged(intent.email)
            is RecuperarPasswordIntent.EnviarClicked -> handleEnviarEmail()
            is RecuperarPasswordIntent.VolverLogin -> handleVolverLogin()
            is RecuperarPasswordIntent.ClearError -> handleClearError()
        }
    }

    private fun handleEmailChanged(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    private fun handleEnviarEmail() {
        val currentState = _state.value

        // Validación básica
        if (currentState.email.isBlank()) {
            _state.update { it.copy(error = "Por favor ingresa tu correo electrónico") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(error = "Por favor ingresa un correo válido") }
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                val response = apiService.RecoveryPassword(
                    RecoveryRequest(email = currentState.email)
                )

                if (response.isSuccessful) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            emailEnviado = true,
                            error = null
                        )
                    }
                    _event.send(
                        RecuperarPasswordEvent.ShowSuccess(
                            "Te enviamos un enlace para restablecer tu contraseña. Revisa tu correo."
                        )
                    )
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "No se pudo enviar el correo. Intenta nuevamente."
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error de conexión. Verifica tu internet."
                    )
                }
                _event.send(
                    RecuperarPasswordEvent.ShowError(
                        e.message ?: "Error desconocido"
                    )
                )
            }
        }
    }

    private fun handleVolverLogin() {
        viewModelScope.launch {
            _event.send(RecuperarPasswordEvent.NavigateToLogin)
        }
    }

    private fun handleClearError() {
        _state.update { it.copy(error = null) }
    }
}

data class SolicitarRecuperacionRequest(
    val email: String
)