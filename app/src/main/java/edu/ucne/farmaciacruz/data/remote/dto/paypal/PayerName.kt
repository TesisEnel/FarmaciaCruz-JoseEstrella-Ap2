package edu.ucne.farmaciacruz.data.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class PayerName(
    @SerializedName("given_name")
    val givenName: String?,
    val surname: String?
)