package edu.ucne.farmaciacruz.data.remote.dto

data class CreateOrderDto(
    val usuarioId: Int,
    val total: Double,
    val productos: List<OrderProductDto>,
    val paypalOrderId: String,
    val paypalPayerId: String? = null
)