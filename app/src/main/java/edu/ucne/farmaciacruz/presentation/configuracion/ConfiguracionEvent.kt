package edu.ucne.farmaciacruz.presentation.configuracion

sealed class ConfiguracionEvent {
    data object LoadUserData : ConfiguracionEvent()
    data object LoadPreferences : ConfiguracionEvent()
    data class ApiUrlChanged(val newUrl: String) : ConfiguracionEvent()
    data object ThemeToggled : ConfiguracionEvent()
    data object ShowLogoutDialog : ConfiguracionEvent()
    data object DismissLogoutDialog : ConfiguracionEvent()
    data object Logout : ConfiguracionEvent()
}

