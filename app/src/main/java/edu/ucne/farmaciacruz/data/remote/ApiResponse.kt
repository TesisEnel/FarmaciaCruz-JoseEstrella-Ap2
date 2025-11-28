package edu.ucne.farmaciacruz.data.remote

data class ApiResponse<T>(
    val mensaje: String? = null,
    val data: T? = null
)