package edu.ucne.farmaciacruz.presentation.producto

import edu.ucne.farmaciacruz.domain.model.Producto

sealed class ProductosEvent {
    data object LoadProductos : ProductosEvent()
    data class SearchQueryChanged(val query: String) : ProductosEvent()
    data class CategoriaSelected(val categoria: String?) : ProductosEvent()
    data class ProductoClicked(val productoId: Int) : ProductosEvent()
    data class AddToCart(val producto: Producto) : ProductosEvent()
    data class RemoveFromCart(val productoId: Int) : ProductosEvent()
    data class UpdateQuantity(val productoId: Int, val cantidad: Int) : ProductosEvent()
    data object ClearError : ProductosEvent()
}

sealed class ProductosUiEvent {
    data class ShowError(val message: String) : ProductosUiEvent()
    data class ShowSuccess(val message: String) : ProductosUiEvent()
    data class NavigateToDetail(val productoId: Int) : ProductosUiEvent()
}