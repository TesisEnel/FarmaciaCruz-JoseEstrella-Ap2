package edu.ucne.farmaciacruz.data.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class ApplicationContext(
    @SerializedName("return_url")
    val returnUrl: String? = null,
    @SerializedName("cancel_url")
    val cancelUrl: String? = null,
    @SerializedName("brand_name")
    val brandName: String = "Farmacia Cruz",
    @SerializedName("landing_page")
    val landingPage: String = "BILLING",
    @SerializedName("shipping_preference")
    val shippingPreference: String = "NO_SHIPPING",
    @SerializedName("user_action")
    val userAction: String = "PAY_NOW"
)