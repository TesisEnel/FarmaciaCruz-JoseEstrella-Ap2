package edu.ucne.farmaciacruz.data.repository

import edu.ucne.farmaciacruz.data.mapper.createOrderDto
import edu.ucne.farmaciacruz.data.mapper.toDomain
import edu.ucne.farmaciacruz.data.remote.ApiService
import edu.ucne.farmaciacruz.data.remote.dto.UpdateOrderStatusRequest
import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Order
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : OrderRepository {

    override fun createOrder(
        usuarioId: Int,
        carrito: List<CarritoItem>,
        total: Double,
        paypalOrderId: String,
        paypalPayerId: String?
    ): Flow<Resource<Order>> = flow {
        try {
            emit(Resource.Loading())

            val dto = createOrderDto(
                usuarioId = usuarioId,
                carrito = carrito,
                total = total,
                paypalOrderId = paypalOrderId,
                paypalPayerId = paypalPayerId
            )

            val response = apiService.createOrder(dto)

            if (response.isSuccessful && response.body() != null) {
                val orderResponse = response.body()!!.data!!
                emit(Resource.Success(orderResponse.toDomain()))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos de orden inválidos"
                    401 -> "No autorizado"
                    404 -> "Endpoint no encontrado"
                    else -> "Error al crear la orden: ${response.message()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión. Verifica tu internet"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }

    override suspend fun getOrder(orderId: Int): Flow<Resource<Order>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getOrder(orderId)

            if (response.isSuccessful && response.body() != null) {
                val order = response.body()!!.data!!.toDomain()
                emit(Resource.Success(order))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Orden no encontrada"
                    401 -> "No autorizado"
                    403 -> "No tienes permisos para ver esta orden"
                    else -> "Error al obtener la orden"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }

    override fun getUserOrders(usuarioId: Int): Flow<Resource<List<Order>>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getUserOrders(usuarioId)

            if (response.isSuccessful && response.body() != null) {
                val orders = response.body()!!.map { it.toDomain() }
                emit(Resource.Success(orders))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "No autorizado"
                    403 -> "No tienes permisos"
                    404 -> "No se encontraron órdenes"
                    else -> "Error al obtener órdenes"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }

    override suspend fun updateOrderStatus(
        orderId: Int,
        status: String
    ): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.updateOrderStatus(
                orderId,
                UpdateOrderStatusRequest(status)
            )

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "No autorizado"
                    403 -> "Solo administradores pueden actualizar órdenes"
                    404 -> "Orden no encontrada"
                    else -> "Error al actualizar estado"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }
}