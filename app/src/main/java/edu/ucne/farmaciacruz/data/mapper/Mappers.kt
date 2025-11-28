package edu.ucne.farmaciacruz.data.mapper

import edu.ucne.farmaciacruz.data.local.entity.CarritoEntity
import edu.ucne.farmaciacruz.data.remote.dto.AuthResponseDto
import edu.ucne.farmaciacruz.data.remote.dto.CreateOrderDto
import edu.ucne.farmaciacruz.data.remote.dto.OrderProductDto
import edu.ucne.farmaciacruz.data.remote.dto.OrderResponseDto
import edu.ucne.farmaciacruz.data.remote.dto.ProductoDto
import edu.ucne.farmaciacruz.data.remote.dto.UsuarioDto
import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Order
import edu.ucne.farmaciacruz.domain.model.OrderProduct
import edu.ucne.farmaciacruz.domain.model.OrderStatus
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.User

fun UsuarioDto.toDomain(): User {
    return User(
        id = this.usuarioId,
        email = this.email,
        nombre = this.nombre,
        apellido = this.apellido,
        telefono = this.telefono,
        rol = this.rol
    )
}

fun AuthResponseDto.toUserDomain(): User {
    return this.usuario.toDomain()
}

fun ProductoDto.toDomain(): Producto {
    return Producto(
        id = this.productoId,
        nombre = this.nombre,
        categoria = this.categoria,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.imagenUrl
    )
}

fun Producto.toDto(): ProductoDto {
    return ProductoDto(
        productoId = this.id,
        nombre = this.nombre,
        categoria = this.categoria,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.imagenUrl
    )
}

fun List<ProductoDto>.toDomain(): List<Producto> {
    return this.map { it.toDomain() }
}

fun List<Producto>.toDto(): List<ProductoDto> {
    return this.map { it.toDto() }
}

fun CarritoEntity.toDomain(): CarritoItem {
    return CarritoItem(
        producto = Producto(
            id = this.productoId,
            nombre = this.nombre,
            categoria = this.categoria,
            descripcion = this.descripcion,
            precio = this.precio,
            imagenUrl = this.imagenUrl
        ),
        cantidad = this.cantidad
    )
}

fun Producto.toCarritoEntity(
    usuarioId: Int,
    cantidad: Int = 1
): CarritoEntity {
    return CarritoEntity(
        id = 0,
        usuarioId = usuarioId,
        productoId = this.id,
        cantidad = cantidad,
        nombre = this.nombre,
        categoria = this.categoria,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.imagenUrl
    )
}

fun OrderResponseDto.toDomain(): Order {
    return Order(
        orderId = this.orderId,
        usuarioId = this.usuarioId,
        total = this.total,
        estado = parseOrderStatus(this.estado),
        productos = this.productos.map { it.toDomain() },
        paypalOrderId = this.paypalOrderId,
        paypalPayerId = this.paypalPayerId,
        fechaCreacion = this.fechaCreacion,
        fechaActualizacion = this.fechaActualizacion
    )
}

fun OrderProductDto.toDomain(): OrderProduct {
    return OrderProduct(
        productoId = this.productoId,
        nombre = this.nombre,
        cantidad = this.cantidad,
        precio = this.precio
    )
}

fun CarritoItem.toOrderProductDto(): OrderProductDto {
    return OrderProductDto(
        productoId = this.producto.id,
        nombre = this.producto.nombre,
        cantidad = this.cantidad,
        precio = this.producto.precio
    )
}

fun createOrderDto(
    usuarioId: Int,
    carrito: List<CarritoItem>,
    total: Double,
    paypalOrderId: String,
    paypalPayerId: String? = null
): CreateOrderDto {
    return CreateOrderDto(
        usuarioId = usuarioId,
        total = total,
        productos = carrito.map { it.toOrderProductDto() },
        paypalOrderId = paypalOrderId,
        paypalPayerId = paypalPayerId
    )
}

private fun parseOrderStatus(status: String): OrderStatus {
    return try {
        OrderStatus.valueOf(status.uppercase())
    } catch (e: Exception) {
        OrderStatus.PENDIENTE
    }
}