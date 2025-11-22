package edu.ucne.farmaciacruz.presentation.producto.detalle

import edu.ucne.farmaciacruz.domain.model.Producto

data class ProductoDetalleState(
    val isLoading: Boolean = false,
    val producto: Producto? = null,
    val cantidad: Int = 1,
    val error: String? = null
)