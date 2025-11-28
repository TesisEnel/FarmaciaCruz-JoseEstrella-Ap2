package edu.ucne.farmaciacruz.domain.repository

import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Order
import edu.ucne.farmaciacruz.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface OrderRepository {

    fun createOrder(
        usuarioId: Int,
        carrito: List<CarritoItem>,
        total: Double,
        paypalOrderId: String,
        paypalPayerId: String? = null
    ): Flow<Resource<Order>>

    fun getOrder(orderId: Int): Flow<Resource<Order>>

    fun getUserOrders(usuarioId: Int): Flow<Resource<List<Order>>>

    fun updateOrderStatus(
        orderId: Int,
        status: String
    ): Flow<Resource<Unit>>
}