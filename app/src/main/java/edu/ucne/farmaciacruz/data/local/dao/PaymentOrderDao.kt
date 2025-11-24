package edu.ucne.farmaciacruz.data.local.dao

import androidx.room.*
import edu.ucne.farmaciacruz.data.local.entity.PaymentOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentOrderDao {

    @Query("SELECT * FROM payment_orders WHERE usuarioId = :usuarioId ORDER BY fechaCreacion DESC")
    fun getOrdersByUsuario(usuarioId: Int): Flow<List<PaymentOrderEntity>>

    @Query("SELECT * FROM payment_orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Int): PaymentOrderEntity?

    @Query("SELECT * FROM payment_orders WHERE localId = :localId")
    suspend fun getOrderByLocalId(localId: String): PaymentOrderEntity?

    @Query("SELECT * FROM payment_orders WHERE paypalOrderId = :paypalOrderId")
    suspend fun getOrderByPayPalId(paypalOrderId: String): PaymentOrderEntity?

    @Query("SELECT * FROM payment_orders WHERE sincronizado = 0")
    suspend fun getUnsyncedOrders(): List<PaymentOrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: PaymentOrderEntity): Long

    @Update
    suspend fun updateOrder(order: PaymentOrderEntity)

    @Delete
    suspend fun deleteOrder(order: PaymentOrderEntity)

    @Query("UPDATE payment_orders SET estado = :estado, fechaActualizacion = :fecha WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, estado: String, fecha: Long)

    @Query("UPDATE payment_orders SET sincronizado = 1, fechaActualizacion = :fecha WHERE id = :orderId")
    suspend fun markAsSynced(orderId: Int, fecha: Long)

    @Query("DELETE FROM payment_orders WHERE usuarioId = :usuarioId AND estado = 'PENDIENTE'")
    suspend fun clearPendingOrders(usuarioId: Int)
}