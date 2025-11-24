package edu.ucne.farmaciacruz.data.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class PayPalOrderResponse(
    val id: String,
    val status: String,
    val links: List<Link>,
    @SerializedName("create_time")
    val createTime: String? = null
)