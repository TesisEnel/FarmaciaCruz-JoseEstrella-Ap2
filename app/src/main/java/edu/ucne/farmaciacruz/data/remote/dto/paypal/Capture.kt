package edu.ucne.farmaciacruz.data.remote.dto.paypal

data class Capture(
    val id: String,
    val status: String,
    val amount: Amount
)