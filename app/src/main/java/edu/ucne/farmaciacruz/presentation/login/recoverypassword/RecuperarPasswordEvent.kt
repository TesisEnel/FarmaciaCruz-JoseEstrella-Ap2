package edu.ucne.farmaciacruz.presentation.login.recoverypassword

sealed class RecuperarPasswordEvent {
    data class ShowError(val message: String) : RecuperarPasswordEvent()
    data class ShowSuccess(val message: String) : RecuperarPasswordEvent()
    object NavigateToLogin : RecuperarPasswordEvent()
}