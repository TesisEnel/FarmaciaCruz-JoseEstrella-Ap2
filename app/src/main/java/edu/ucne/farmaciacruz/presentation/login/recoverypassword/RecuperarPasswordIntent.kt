package edu.ucne.farmaciacruz.presentation.login.recoverypassword

sealed class RecuperarPasswordIntent {
    data class EmailChanged(val email: String) : RecuperarPasswordIntent()
    object EnviarClicked : RecuperarPasswordIntent()
    object VolverLogin : RecuperarPasswordIntent()
    object ClearError : RecuperarPasswordIntent()
}