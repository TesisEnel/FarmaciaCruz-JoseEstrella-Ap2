package edu.ucne.farmaciacruz.data.remote.dto.paypal

import com.google.gson.annotations.SerializedName


data class PayPalOrderRequest(
    val intent: String = "CAPTURE",
    @SerializedName("purchase_units")
    val purchaseUnits: List<PurchaseUnit>,
    @SerializedName("application_context")
    val applicationContext: ApplicationContext? = null
)