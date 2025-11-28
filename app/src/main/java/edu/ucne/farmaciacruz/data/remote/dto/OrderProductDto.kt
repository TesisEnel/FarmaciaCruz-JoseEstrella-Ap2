package edu.ucne.farmaciacruz.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderProductDto(
    @SerializedName("productoId")
    val productoId: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("cantidad")
    val cantidad: Int,

    @SerializedName("precio")
    val precio: Double
)