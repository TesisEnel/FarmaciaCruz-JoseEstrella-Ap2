package edu.ucne.farmaciacruz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_orders")
data class PaymentOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val localId: String,
    val usuarioId: Int,
    val total: Double,
    val productosJson: String,
    val estado: String,
    val metodoPago: String,
    val paypalOrderId: String?,
    val paypalPayerId: String?,
    val fechaCreacion: Long,
    val fechaActualizacion: Long,
    val sincronizado: Boolean = false,
    val errorMessage: String? = null
)