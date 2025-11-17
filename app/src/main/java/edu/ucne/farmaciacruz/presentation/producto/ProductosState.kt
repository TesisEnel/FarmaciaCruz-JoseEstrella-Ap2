package edu.ucne.farmaciacruz.presentation.producto

import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Producto

data class ProductosState(
    val isLoading: Boolean = false,
    val productos: List<Producto> = emptyList(),
    val productosFiltrados: List<Producto> = emptyList(),
    val categorias: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoria: String? = null,
    val carrito: List<CarritoItem> = emptyList(),
    val error: String? = null
)