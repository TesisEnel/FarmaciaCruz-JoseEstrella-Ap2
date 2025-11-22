package edu.ucne.farmaciacruz.data.repository

import edu.ucne.farmaciacruz.data.local.dao.CarritoDao
import edu.ucne.farmaciacruz.data.local.entity.CarritoEntity
import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.repository.CarritoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarritoRepositoryImpl @Inject constructor(
    private val carritoDao: CarritoDao
) : CarritoRepository {

    override fun getCarrito(usuarioId: Int): Flow<List<CarritoItem>> {
        return carritoDao.getCarritoByUsuario(usuarioId).map { entities ->
            entities.map { it.toCarritoItem() }
        }
    }

    override suspend fun addToCarrito(usuarioId: Int, producto: Producto) {
        val existingItem = carritoDao.getCarritoItem(usuarioId, producto.id)

        if (existingItem != null) {
            carritoDao.updateCarritoItem(
                existingItem.copy(cantidad = existingItem.cantidad + 1)
            )
        } else {
            carritoDao.insertCarritoItem(
                CarritoEntity(
                    usuarioId = usuarioId,
                    productoId = producto.id,
                    cantidad = 1,
                    nombre = producto.nombre,
                    categoria = producto.categoria,
                    descripcion = producto.descripcion,
                    precio = producto.precio,
                    imagenUrl = producto.imagenUrl
                )
            )
        }
    }

    override suspend fun updateQuantity(usuarioId: Int, productoId: Int, cantidad: Int) {
        val item = carritoDao.getCarritoItem(usuarioId, productoId)
        if (item != null) {
            if (cantidad <= 0) {
                carritoDao.deleteCarritoItem(item)
            } else {
                carritoDao.updateCarritoItem(item.copy(cantidad = cantidad))
            }
        }
    }

    override suspend fun removeFromCarrito(usuarioId: Int, productoId: Int) {
        carritoDao.deleteByProductoId(usuarioId, productoId)
    }

    override suspend fun clearCarrito(usuarioId: Int) {
        carritoDao.clearCarrito(usuarioId)
    }

    override fun getTotalItems(usuarioId: Int): Flow<Int> {
        return carritoDao.getTotalItems(usuarioId).map { it ?: 0 }
    }

    private fun CarritoEntity.toCarritoItem(): CarritoItem {
        return CarritoItem(
            producto = Producto(
                id = productoId,
                nombre = nombre,
                categoria = categoria,
                descripcion = descripcion,
                precio = precio,
                imagenUrl = imagenUrl
            ),
            cantidad = cantidad
        )
    }
}