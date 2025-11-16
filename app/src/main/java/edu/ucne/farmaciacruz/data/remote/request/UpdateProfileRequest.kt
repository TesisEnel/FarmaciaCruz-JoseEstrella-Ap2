package edu.ucne.farmaciacruz.data.remote.request

data class UpdateProfileRequest(
    val nombre: String,
    val apellido: String,
    val telefono: String?
)