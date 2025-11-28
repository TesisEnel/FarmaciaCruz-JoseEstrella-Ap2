package edu.ucne.farmaciacruz.presentation.producto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.presentation.carrito.CarritoBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    onProductoClick: (Int) -> Unit,
    onConfigClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    viewModel: ProductosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCarritoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProductosUiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is ProductosUiEvent.ShowSuccess -> snackbarHostState.showSnackbar(event.message)
                is ProductosUiEvent.NavigateToDetail -> onProductoClick(event.productoId)
            }
        }
    }

    if (showCarritoSheet) {
        CarritoBottomSheet(
            carrito = state.carrito,
            total = state.carrito.sumOf { it.producto.precio * it.cantidad },
            onDismiss = { showCarritoSheet = false },
            onUpdateQuantity = { productoId, cantidad ->
                viewModel.onEvent(ProductosEvent.UpdateQuantity(productoId, cantidad))
            },
            onRemoveItem = { productoId ->
                viewModel.onEvent(ProductosEvent.RemoveFromCart(productoId))
            },
            onProceedToCheckout = {
                showCarritoSheet = false
                onCheckoutClick()
            }
        )
    }

    ProductosScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        cantidadCarrito = state.carrito.sumOf { it.cantidad },
        onEvent = viewModel::onEvent,
        onCarritoClick = { showCarritoSheet = true },
        onConfigClick = onConfigClick
    )
}

@Composable
private fun ProductosScreenContent(
    state: ProductosState,
    snackbarHostState: SnackbarHostState,
    cantidadCarrito: Int,
    onEvent: (ProductosEvent) -> Unit,
    onCarritoClick: () -> Unit,
    onConfigClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ProductosTopBar(
                state = state,
                cantidadCarrito = cantidadCarrito,
                onEvent = onEvent,
                onCarritoClick = onCarritoClick
            )
        },
        bottomBar = {
            BottomBarManual(
                onHome = {},
                onSearch = {},
                onCart = onCarritoClick,
                onProfile = onConfigClick
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {

            when {
                state.isLoading -> ProductosLoadingState()

                state.error != null -> ProductosErrorState(
                    error = state.error,
                    onRetry = { onEvent(ProductosEvent.LoadProductos) }
                )

                else -> ProductosList(
                    state = state,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun ProductosTopBar(
    state: ProductosState,
    cantidadCarrito: Int,
    onEvent: (ProductosEvent) -> Unit,
    onCarritoClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Bienvenido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    BadgedBox(
                        badge = {
                            if (cantidadCarrito > 0) {
                                Badge { Text(cantidadCarrito.toString()) }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onCarritoClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Outlined.ShoppingCart,
                                contentDescription = "Carrito",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProductosSearchBar(
                searchQuery = state.searchQuery,
                onSearchChange = { onEvent(ProductosEvent.SearchQueryChanged(it)) }
            )
        }
    }
}

@Composable
private fun ProductosSearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar productos…") },
        leadingIcon = {
            Icon(Icons.Outlined.Search, contentDescription = null)
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun ProductosErrorState(error: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(error, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun ProductosLoadingState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ProductosList(
    state: ProductosState,
    onEvent: (ProductosEvent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickAccessCard(Icons.Outlined.MedicalServices, "Mis Pedidos") {}
                QuickAccessCard(Icons.Outlined.History, "Historial") {}
                QuickAccessCard(Icons.Outlined.FavoriteBorder, "Favoritos") {}
            }
        }

        if (state.categorias.isNotEmpty()) {
            item {
                Column {
                    Text(
                        "Categorías",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    ProductosCategorias(state, onEvent)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Productos Disponibles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${state.productosFiltrados.size} productos",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (state.productosFiltrados.isEmpty()) {
            item { ProductosEmptyState() }
        } else {
            items(state.productosFiltrados) { producto ->
                ProductoCard(
                    producto = producto,
                    onClick = { onEvent(ProductosEvent.ProductoClicked(producto.id)) },
                    onAddToCart = { onEvent(ProductosEvent.AddToCart(producto)) }
                )
            }
        }
    }
}

@Composable
private fun ProductosCategorias(
    state: ProductosState,
    onEvent: (ProductosEvent) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = state.selectedCategoria == null,
                onClick = { onEvent(ProductosEvent.CategoriaSelected(null)) },
                label = { Text("Todas") }
            )
        }

        items(state.categorias) { categoria ->
            FilterChip(
                selected = state.selectedCategoria == categoria,
                onClick = { onEvent(ProductosEvent.CategoriaSelected(categoria)) },
                label = { Text(categoria) }
            )
        }
    }
}

@Composable
private fun ProductosEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Text("No se encontraron productos")
        }
    }
}
@Composable
fun BottomBarManual(
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onCart: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarItem(Icons.Outlined.Home, "Inicio", true, onHome)
        BottomBarItem(Icons.Outlined.Search, "Buscar", false, onSearch)
        BottomBarItem(Icons.Outlined.ShoppingCart, "Carrito", false, onCart)
        BottomBarItem(Icons.Outlined.Person, "Perfil", false, onProfile)
    }
}

@Composable
fun BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(26.dp)
        )

        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickAccessCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(100.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        producto.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        producto.precioFormateado,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (producto.precio > 50) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                "-15%",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }

            FilledIconButton(
                onClick = onAddToCart,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar")
            }
        }
    }
}
