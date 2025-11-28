package edu.ucne.farmaciacruz.data.repository

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import edu.ucne.farmaciacruz.BuildConfig
import edu.ucne.farmaciacruz.core.common.ErrorMessages
import edu.ucne.farmaciacruz.data.local.dao.PaymentOrderDao
import edu.ucne.farmaciacruz.data.local.entity.PaymentOrderEntity
import edu.ucne.farmaciacruz.data.remote.PayPalApiService
import edu.ucne.farmaciacruz.data.remote.dto.paypal.Amount
import edu.ucne.farmaciacruz.data.remote.dto.paypal.ApplicationContext
import edu.ucne.farmaciacruz.data.remote.dto.paypal.PayPalOrderRequest
import edu.ucne.farmaciacruz.data.remote.dto.paypal.PurchaseUnit
import edu.ucne.farmaciacruz.domain.model.*
import edu.ucne.farmaciacruz.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val payPalApi: PayPalApiService,
    private val paymentOrderDao: PaymentOrderDao,
    private val gson: Gson
) : PaymentRepository {

    private var cachedAccessToken: String? = null
    private var tokenExpiry: Long = 0L

    companion object {
        private const val TAG = "PaymentRepository"
    }

    override fun createPayPalOrder(
        usuarioId: Int,
        items: List<CarritoItem>,
        total: Double
    ): Flow<Resource<String>> = flow {

        emit(Resource.Loading())

        try {
            val token = getAccessToken()

            val request = PayPalOrderRequest(
                intent = "CAPTURE",
                purchaseUnits = listOf(
                    PurchaseUnit(
                        amount = Amount(
                            currencyCode = "USD",
                            value = String.format("%.2f", total)
                        ),
                        description = "Compra en Farmacia Cruz - ${items.size} productos",
                        referenceId = UUID.randomUUID().toString()
                    )
                ),
                applicationContext = ApplicationContext(
                    brandName = "Farmacia Cruz",
                    landingPage = "BILLING",
                    shippingPreference = "NO_SHIPPING",
                    userAction = "PAY_NOW"
                )
            )

            val response = payPalApi.createOrder("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {

                val order = response.body()!!
                val localId = UUID.randomUUID().toString()

                paymentOrderDao.insertOrder(
                    PaymentOrderEntity(
                        localId = localId,
                        usuarioId = usuarioId,
                        total = total,
                        productosJson = gson.toJson(items),
                        estado = PaymentStatus.PROCESANDO.name,
                        metodoPago = "PayPal",
                        paypalOrderId = order.id,
                        paypalPayerId = null,
                        fechaCreacion = System.currentTimeMillis(),
                        fechaActualizacion = System.currentTimeMillis(),
                        sincronizado = false
                    )
                )

                emit(Resource.Success(order.id))

            } else {
                emit(Resource.Error("Error creando orden: ${response.code()}"))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))

        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))

        } catch (e: Exception) {
            Log.e(TAG, "Exception creating PayPal order", e)
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun capturePayPalPayment(
        paypalOrderId: String,
        localOrderId: String
    ): Flow<Resource<PaymentResult>> = flow {

        emit(Resource.Loading())

        try {
            val token = getAccessToken()

            val response = payPalApi.captureOrder("Bearer $token", paypalOrderId)

            if (response.isSuccessful && response.body() != null) {

                val data = response.body()!!

                if (data.status == "COMPLETED") {

                    val payerId = data.payer?.payerId
                    val amount = data.purchaseUnits
                        ?.firstOrNull()
                        ?.payments
                        ?.captures
                        ?.firstOrNull()
                        ?.amount
                        ?.value
                        ?.toDoubleOrNull() ?: 0.0

                    updateLocalOrder(paypalOrderId, PaymentStatus.COMPLETADO.name, payerId)

                    emit(
                        Resource.Success(
                            PaymentResult.Success(
                                orderId = paypalOrderId,
                                payerId = payerId ?: "",
                                amount = amount
                            )
                        )
                    )

                } else {
                    updateLocalOrder(paypalOrderId, PaymentStatus.FALLIDO.name)
                    emit(Resource.Error("Estado de pago: ${data.status}"))
                }

            } else {
                updateLocalOrder(paypalOrderId, PaymentStatus.FALLIDO.name)
                emit(Resource.Error("Error capturando pago: ${response.code()}"))
            }

        } catch (e: HttpException) {
            updateLocalOrder(paypalOrderId, PaymentStatus.FALLIDO.name)
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))

        } catch (e: IOException) {
            updateLocalOrder(paypalOrderId, PaymentStatus.FALLIDO.name)
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))

        } catch (e: Exception) {
            updateLocalOrder(paypalOrderId, PaymentStatus.FALLIDO.name)
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun createLocalOrder(
        usuarioId: Int,
        items: List<CarritoItem>,
        total: Double,
        paypalOrderId: String?
    ): Flow<Resource<PaymentOrder>> = flow {

        emit(Resource.Loading())

        try {

            val entity = PaymentOrderEntity(
                localId = UUID.randomUUID().toString(),
                usuarioId = usuarioId,
                total = total,
                productosJson = gson.toJson(items),
                estado = PaymentStatus.PENDIENTE.name,
                metodoPago = "PayPal",
                paypalOrderId = paypalOrderId,
                paypalPayerId = null,
                fechaCreacion = System.currentTimeMillis(),
                fechaActualizacion = System.currentTimeMillis(),
                sincronizado = false
            )

            val insertedId = paymentOrderDao.insertOrder(entity)
            val savedOrder = paymentOrderDao.getOrderById(insertedId.toInt())

            if (savedOrder != null) {
                emit(Resource.Success(savedOrder.toDomain()))
            } else {
                emit(Resource.Error("Error guardando orden local"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun getOrdersByUser(usuarioId: Int): Flow<List<PaymentOrder>> =
        paymentOrderDao.getOrdersByUsuario(usuarioId)
            .map { list ->
                list.map { it.toDomain() }
            }

    override suspend fun getOrderById(orderId: Int): PaymentOrder? {
        return paymentOrderDao.getOrderById(orderId)?.toDomain()
    }

    override suspend fun updateOrderStatus(
        orderId: Int,
        status: String,
        paypalPayerId: String?
    ) {
        val local = paymentOrderDao.getOrderById(orderId)
        if (local != null) {
            paymentOrderDao.updateOrder(
                local.copy(
                    estado = status,
                    paypalPayerId = paypalPayerId,
                    fechaActualizacion = System.currentTimeMillis()
                )
            )
        }
    }

    override fun syncOrders(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val unsynced = paymentOrderDao.getUnsyncedOrders()

            unsynced.forEach { order ->
                if (order.estado == PaymentStatus.COMPLETADO.name) {
                    paymentOrderDao.markAsSynced(order.id, System.currentTimeMillis())
                }
            }

            emit(Resource.Success(Unit))

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error de sincronización"))
        }
    }

    private suspend fun updateLocalOrder(
        paypalOrderId: String,
        status: String,
        payerId: String? = null
    ) {
        try {
            val order = paymentOrderDao.getOrderByPayPalId(paypalOrderId)
            if (order != null) {
                paymentOrderDao.updateOrder(
                    order.copy(
                        estado = status,
                        paypalPayerId = payerId,
                        fechaActualizacion = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order", e)
        }
    }

    private suspend fun getAccessToken(): String {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpiry) {
            return cachedAccessToken!!
        }

        val credentials =
            "${BuildConfig.PAYPAL_CLIENT_ID}:${BuildConfig.PAYPAL_SECRET}"

        val auth = "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"

        val response = payPalApi.getAccessToken(auth)

        if (response.isSuccessful && response.body() != null) {
            val token = response.body()!!
            cachedAccessToken = token.accessToken
            tokenExpiry = System.currentTimeMillis() + (token.expiresIn * 1000) - 60_000
            return token.accessToken
        } else {
            throw Exception("Error obteniendo token de acceso: ${response.code()}")
        }
    }

    private fun PaymentOrderEntity.toDomain(): PaymentOrder {
        val type = object : com.google.gson.reflect.TypeToken<List<CarritoItem>>() {}.type
        val items = try {
            gson.fromJson<List<CarritoItem>>(productosJson, type)
        } catch (e: Exception) {
            emptyList()
        }

        return PaymentOrder(
            id = localId,
            usuarioId = usuarioId,
            total = total,
            productos = items,
            estado = PaymentStatus.valueOf(estado),
            metodoPago = metodoPago,
            paypalOrderId = paypalOrderId,
            paypalPayerId = paypalPayerId,
            fechaCreacion = fechaCreacion,
            fechaActualizacion = fechaActualizacion,
            sincronizado = sincronizado,
            errorMessage = errorMessage
        )
    }
}
