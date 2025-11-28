package edu.ucne.farmaciacruz.presentation.configuracion

sealed class ConfiguracionUiEvent {
    data class ShowError(val message: String) : ConfiguracionUiEvent()
    data class ShowSuccess(val message: String) : ConfiguracionUiEvent()
    data object NavigateToLogin : ConfiguracionUiEvent()
}
