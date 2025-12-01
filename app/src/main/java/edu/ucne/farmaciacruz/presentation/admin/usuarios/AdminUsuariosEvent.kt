package edu.ucne.farmaciacruz.presentation.admin.usuarios

import edu.ucne.farmaciacruz.domain.model.UsuarioAdmin

sealed class AdminUsuariosEvent {
    data object LoadUsuarios : AdminUsuariosEvent()
    data class SearchQueryChanged(val query: String) : AdminUsuariosEvent()
    data class RolFilterSelected(val rol: String?) : AdminUsuariosEvent()
    data class EstadoFilterSelected(val activo: Boolean?) : AdminUsuariosEvent()
    data class UsuarioSelected(val usuario: UsuarioAdmin) : AdminUsuariosEvent()
    data object DismissDialogs : AdminUsuariosEvent()
    data class ShowCambiarRolDialog(val usuario: UsuarioAdmin) : AdminUsuariosEvent()
    data class CambiarRol(val usuarioId: Int, val nuevoRol: String) : AdminUsuariosEvent()
    data class ShowToggleEstadoDialog(val usuario: UsuarioAdmin) : AdminUsuariosEvent()
    data class ToggleEstado(val usuarioId: Int, val activo: Boolean) : AdminUsuariosEvent()
    data class ShowDeleteDialog(val usuario: UsuarioAdmin) : AdminUsuariosEvent()
    data object ConfirmDelete : AdminUsuariosEvent()
    data object Refresh : AdminUsuariosEvent()
}