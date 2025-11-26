package edu.ucne.farmaciacruz.data.remote.dto

data class UpdateProfileRequest(
    val nombre: String,
    val apellido: String,
    val telefono: String?
)