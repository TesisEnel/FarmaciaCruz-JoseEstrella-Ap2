package edu.ucne.farmaciacruz.data.repository

import edu.ucne.farmaciacruz.core.common.ErrorMessages
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
        emit(Resource.Loading())

        try {
            val dto = createOrderDto(
                usuarioId = usuarioId,
                carrito = carrito,
                total = total,
                paypalOrderId = paypalOrderId,
                paypalPayerId = paypalPayerId
            )

            val response = apiService.createOrder(dto)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                val errorMsg = when (response.code()) {
                    401 -> ErrorMessages.NO_AUTORIZADO
                    400 -> "Datos de orden inválidos"
                    404 -> "Orden no encontrada"
                    else -> ErrorMessages.ERROR_DESCONOCIDO
                }
                emit(Resource.Error(errorMsg))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun getOrder(orderId: Int): Flow<Resource<Order>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getOrder(orderId)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                val msg = when (response.code()) {
                    404 -> "Orden no encontrada"
                    401 -> ErrorMessages.NO_AUTORIZADO
                    else -> ErrorMessages.ERROR_DESCONOCIDO
                }
                emit(Resource.Error(msg))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun getUserOrders(usuarioId: Int): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getUserOrders(usuarioId)

            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                val msg = when (response.code()) {
                    401 -> ErrorMessages.NO_AUTORIZADO
                    403 -> "No tienes permisos"
                    404 -> "No se encontraron órdenes"
                    else -> ErrorMessages.ERROR_DESCONOCIDO
                }
                emit(Resource.Error(msg))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun updateOrderStatus(
        orderId: Int,
        status: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.updateOrderStatus(orderId, UpdateOrderStatusRequest(status))

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val msg = when (response.code()) {
                    401 -> ErrorMessages.NO_AUTORIZADO
                    403 -> "Solo administradores pueden actualizar órdenes"
                    404 -> "Orden no encontrada"
                    else -> ErrorMessages.ERROR_DESCONOCIDO
                }
                emit(Resource.Error(msg))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }
}
