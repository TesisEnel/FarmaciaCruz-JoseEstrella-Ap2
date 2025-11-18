package edu.ucne.farmaciacruz.presentation.login.recoverypassword

data class RecuperarPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailEnviado: Boolean = false,
    val error: String? = null
)
