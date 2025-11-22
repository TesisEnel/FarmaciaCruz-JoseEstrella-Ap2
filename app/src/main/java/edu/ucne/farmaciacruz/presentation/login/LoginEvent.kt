package edu.ucne.farmaciacruz.presentation.login

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    data object LoginClicked : LoginEvent()
    data object ClearError : LoginEvent()
}