package edu.ucne.farmaciacruz.data.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class Amount(
    @SerializedName("currency_code")
    val currencyCode: String = "USD",
    val value: String
)
