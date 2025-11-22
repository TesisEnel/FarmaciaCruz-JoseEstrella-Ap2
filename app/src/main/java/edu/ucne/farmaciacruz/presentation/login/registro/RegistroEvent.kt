package edu.ucne.farmaciacruz.presentation.login.registro

sealed class RegistroEvent {
    data class NombreChanged(val nombre: String) : RegistroEvent()
    data class ApellidoChanged(val apellido: String) : RegistroEvent()
    data class EmailChanged(val email: String) : RegistroEvent()
    data class TelefonoChanged(val telefono: String) : RegistroEvent()
    data class PasswordChanged(val password: String) : RegistroEvent()
    data class ConfirmarPasswordChanged(val confirmarPassword: String) : RegistroEvent()
    data class TerminosChanged(val aceptado: Boolean) : RegistroEvent()
    data object RegistrarClicked : RegistroEvent()
    data object ClearError : RegistroEvent()
}

