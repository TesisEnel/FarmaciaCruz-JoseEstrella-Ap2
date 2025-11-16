package edu.ucne.farmaciacruz.domain.usecase.producto

import edu.ucne.farmaciacruz.data.repository.ProductRepositoryImpl
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductosUseCase @Inject constructor(
    private val productRepositoryImpl: ProductRepositoryImpl
) {
    suspend operator fun invoke(): Flow<Resource<List<Producto>>> {
        return productRepositoryImpl.getProductos()
    }
}