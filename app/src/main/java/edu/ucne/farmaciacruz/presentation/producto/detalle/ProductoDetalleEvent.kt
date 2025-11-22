package edu.ucne.farmaciacruz.presentation.producto.detalle

sealed class ProductoDetalleEvent {
    data class LoadProducto(val productoId: Int) : ProductoDetalleEvent()
    data object AddToCart : ProductoDetalleEvent()
    data class UpdateQuantity(val cantidad: Int) : ProductoDetalleEvent()
    data object NavigateBack : ProductoDetalleEvent()
}

sealed class ProductoDetalleUiEvent {
    data class ShowError(val message: String) : ProductoDetalleUiEvent()
    data class ShowSuccess(val message: String) : ProductoDetalleUiEvent()
    data object NavigateBack : ProductoDetalleUiEvent()
}