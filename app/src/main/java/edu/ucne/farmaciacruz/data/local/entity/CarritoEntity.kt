package edu.ucne.farmaciacruz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrito")
data class CarritoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val productoId: Int,
    val cantidad: Int,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String,
    val fechaAgregado: Long = System.currentTimeMillis()
)