package edu.ucne.farmaciacruz.data.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class PurchaseUnit(
    val amount: Amount,
    val description: String? = null,
    @SerializedName("reference_id")
    val referenceId: String? = null
)
