package edu.ucne.farmaciacruz.presentation.admin.ordenes

sealed class AdminOrdenesEffect {
    data class ShowError(val message: String) : AdminOrdenesEffect()
    data class ShowSuccess(val message: String) : AdminOrdenesEffect()
}