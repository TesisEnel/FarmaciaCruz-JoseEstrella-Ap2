package edu.ucne.farmaciacruz.presentation.admin.usuarios

sealed class AdminUsuariosEffect {
    data class ShowError(val message: String) : AdminUsuariosEffect()
    data class ShowSuccess(val message: String) : AdminUsuariosEffect()
}