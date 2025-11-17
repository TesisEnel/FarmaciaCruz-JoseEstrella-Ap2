package edu.ucne.farmaciacruz.presentation.login

sealed class LoginEvent {
    data class ShowError(val message: String) : LoginEvent()
    object NavigateToHome : LoginEvent()
}