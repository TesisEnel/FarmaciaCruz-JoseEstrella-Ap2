package edu.ucne.farmaciacruz.data.remote.dto

data class ChangePasswordRequest(
    val passwordActual: String,
    val passwordNuevo: String
)