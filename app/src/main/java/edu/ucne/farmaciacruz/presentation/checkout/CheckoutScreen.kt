package edu.ucne.farmaciacruz.presentation.checkout

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import edu.ucne.farmaciacruz.domain.model.CarritoItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onNavigateToOrders: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPayPalWebView by remember { mutableStateOf(false) }
    var paypalUrl by remember { mutableStateOf("") }
    var currentOrderId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CheckoutUiEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)

                is CheckoutUiEvent.ShowSuccess ->
                    snackbarHostState.showSnackbar(event.message)

                is CheckoutUiEvent.OpenPayPalWebView -> {
                    paypalUrl = event.approvalUrl
                    currentOrderId = event.orderId
                    showPayPalWebView = true
                }

                CheckoutUiEvent.NavigateBack -> onBack()
                CheckoutUiEvent.NavigateToOrders -> onNavigateToOrders()
            }
        }
    }

    if (showPayPalWebView) {
        PayPalWebViewScreen(
            url = paypalUrl,
            orderId = currentOrderId,
            onPaymentCompleted = {
                showPayPalWebView = false
                viewModel.onEvent(CheckoutEvent.PaymentCompleted(it))
            },
            onPaymentCancelled = {
                showPayPalWebView = false
                viewModel.onEvent(CheckoutEvent.PaymentCancelled)
            },
            onClose = { showPayPalWebView = false }
        )
    } else {
        CheckoutScaffold(
            snackbarHostState = snackbarHostState,
            state = state,
            onPayWithPayPal = { viewModel.onEvent(CheckoutEvent.CreatePayPalOrder) },
            onBack = { viewModel.onEvent(CheckoutEvent.NavigateBack) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutScaffold(
    snackbarHostState: SnackbarHostState,
    state: CheckoutState,
    onPayWithPayPal: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                CheckoutContent(
                    state = state,
                    onPayWithPayPal = onPayWithPayPal
                )
            }
        }
    }
}

@Composable
private fun CheckoutContent(
    state: CheckoutState,
    onPayWithPayPal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Resumen de tu pedido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        ProductsCard(state)
        Spacer(Modifier.height(16.dp))
        SummaryCard(state)
        Spacer(Modifier.height(16.dp))
        PayPalButton(onPayWithPayPal)
        Spacer(Modifier.height(8.dp))
        SecurePaymentInfo()
    }
}

@Composable
private fun ProductsCard(
    state: CheckoutState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.carrito) { item ->
                CheckoutItemCard(item)
            }
        }
    }
}

@Composable
private fun SummaryCard(state: CheckoutState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryRow("Subtotal", state.totalFormateado)
            SummaryRow("Envío", "GRATIS", highlight = true)
            Divider()
            SummaryRow(
                label = "Total",
                value = state.totalFormateado,
                large = true
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, highlight: Boolean = false, large: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            value,
            style = if (large) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.bodyLarge,
            color = if (highlight) MaterialTheme.colorScheme.primary else LocalContentColor.current,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PayPalButton(onPayWithPayPal: () -> Unit) {
    Button(
        onClick = onPayWithPayPal,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Icon(Icons.Default.Payment, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Pagar con PayPal")
    }
}

@Composable
private fun SecurePaymentInfo() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Pago seguro con PayPal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CheckoutItemCard(item: CarritoItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            AsyncImage(
                model = item.producto.imagenUrl,
                contentDescription = item.producto.nombre,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.producto.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cant: ${item.cantidad}", style = MaterialTheme.typography.bodySmall)
                Text(
                    item.subtotalFormateado,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayPalWebViewScreen(
    url: String,
    orderId: String,
    onPaymentCompleted: (String) -> Unit,
    onPaymentCancelled: () -> Unit,
    onClose: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PayPal") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Cerrar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                url?.let {
                                    when {
                                        it.contains("success") || it.contains("approved") ->
                                            onPaymentCompleted(orderId)

                                        it.contains("cancel") || it.contains("cancelled") ->
                                            onPaymentCancelled()
                                    }
                                }
                            }
                        }

                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
