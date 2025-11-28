package edu.ucne.farmaciacruz.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderResponseDto(
    @SerializedName("orderId")
    val orderId: Int,

    @SerializedName("usuarioId")
    val usuarioId: Int,

    @SerializedName("total")
    val total: Double,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("productos")
    val productos: List<OrderProductDto>,

    @SerializedName("paypalOrderId")
    val paypalOrderId: String?,

    @SerializedName("paypalPayerId")
    val paypalPayerId: String?,

    @SerializedName("fechaCreacion")
    val fechaCreacion: String,

    @SerializedName("fechaActualizacion")
    val fechaActualizacion: String
)