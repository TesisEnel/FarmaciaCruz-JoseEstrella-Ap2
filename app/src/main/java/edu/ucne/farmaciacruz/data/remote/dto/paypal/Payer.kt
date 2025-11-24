package edu.ucne.farmaciacruz.data.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class Payer(
    @SerializedName("payer_id")
    val payerId: String,
    @SerializedName("email_address")
    val emailAddress: String?,
    val name: PayerName?
)