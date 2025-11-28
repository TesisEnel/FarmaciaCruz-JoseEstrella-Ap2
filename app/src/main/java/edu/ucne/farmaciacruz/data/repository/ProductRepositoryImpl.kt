package edu.ucne.farmaciacruz.data.repository

import edu.ucne.farmaciacruz.core.common.ErrorMessages
import edu.ucne.farmaciacruz.core.common.ProductMessages
import edu.ucne.farmaciacruz.data.remote.ApiService
import edu.ucne.farmaciacruz.data.remote.dto.ProductoDto
import edu.ucne.farmaciacruz.data.remote.dto.CreateProductoRequest
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProductRepository {

    override fun getProductos(): Flow<Resource<List<Producto>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getProductos()

            if (response.isSuccessful && response.body() != null) {
                val productos = response.body()!!.map { it.toDomain() }
                emit(Resource.Success(productos))
            } else {
                val message = when (response.code()) {
                    401 -> ProductMessages.NO_AUTORIZADO
                    403 -> ProductMessages.NO_PERMISOS
                    404 -> ProductMessages.NO_PRODUCTOS
                    else -> ProductMessages.ERROR_CARGAR
                }
                emit(Resource.Error(message))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun getProducto(id: Int): Flow<Resource<Producto>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getProducto(id)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
            } else {
                val message = when (response.code()) {
                    404 -> ProductMessages.PRODUCTO_NO_ENCONTRADO
                    401 -> ProductMessages.NO_AUTORIZADO
                    else -> ProductMessages.ERROR_CARGAR
                }
                emit(Resource.Error(message))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun getProductosPorCategoria(categoria: String): Flow<Resource<List<Producto>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getProductosPorCategoria(categoria)

            if (response.isSuccessful && response.body() != null) {
                val productos = response.body()!!.map { it.toDomain() }
                emit(Resource.Success(productos))
            } else {
                emit(Resource.Error(ProductMessages.ERROR_CARGAR))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun searchProductos(query: String): Flow<Resource<List<Producto>>> = flow {
        emit(Resource.Loading())

        try {
            if (query.isBlank()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val response = apiService.getProductos()

            if (response.isSuccessful && response.body() != null) {
                val filtered = response.body()!!
                    .filter { producto ->
                        producto.nombre.contains(query, ignoreCase = true) ||
                                producto.descripcion.contains(query, ignoreCase = true) ||
                                producto.categoria.contains(query, ignoreCase = true)
                    }
                    .map { it.toDomain() }

                emit(Resource.Success(filtered))
            } else {
                emit(Resource.Error(ProductMessages.NO_COINCIDENCIAS))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun getCategorias(): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getProductos()

            if (response.isSuccessful && response.body() != null) {
                val categorias = response.body()!!
                    .map { it.categoria }
                    .distinct()
                    .sorted()

                emit(Resource.Success(categorias))
            } else {
                emit(Resource.Error(ProductMessages.NO_PRODUCTOS))
            }

        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun createProducto(
        nombre: String,
        categoria: String,
        descripcion: String,
        precio: Double,
        imagenUrl: String
    ): Flow<Resource<Producto>> = flow {

        emit(Resource.Loading())

        try {
            val request = CreateProductoRequest(
                nombre = nombre,
                categoria = categoria,
                descripcion = descripcion,
                precio = precio,
                imagenUrl = imagenUrl
            )

            val response = apiService.createProducto(request)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                val msg = when (response.code()) {
                    401 -> ProductMessages.NO_AUTORIZADO
                    403 -> ProductMessages.NO_PERMISOS
                    409 -> "Ya existe un producto con ese nombre"
                    else -> ProductMessages.ERROR_CREAR
                }
                emit(Resource.Error(msg))
            }

        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun updateProducto(producto: Producto): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val dto = ProductoDto(
                productoId = producto.id,
                nombre = producto.nombre,
                categoria = producto.categoria,
                descripcion = producto.descripcion,
                precio = producto.precio,
                imagenUrl = producto.imagenUrl
            )

            val response = apiService.updateProducto(producto.id, dto)

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(ProductMessages.ERROR_ACTUALIZAR))
            }

        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun deleteProducto(id: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.deleteProducto(id)

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val msg = when (response.code()) {
                    401 -> ProductMessages.NO_AUTORIZADO
                    403 -> ProductMessages.NO_PERMISOS
                    404 -> ProductMessages.PRODUCTO_NO_ENCONTRADO
                    else -> ProductMessages.ERROR_ELIMINAR
                }
                emit(Resource.Error(msg))
            }

        } catch (e: Exception) {
            emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
        }
    }
}

private fun ProductoDto.toDomain(): Producto {
    return Producto(
        id = this.productoId,
        nombre = this.nombre,
        categoria = this.categoria,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.imagenUrl
    )
}
