package edu.ucne.farmaciacruz.data.remote

import edu.ucne.farmaciacruz.data.remote.dto.paypal.PayPalCaptureResponse
import edu.ucne.farmaciacruz.data.remote.dto.paypal.PayPalOrderRequest
import edu.ucne.farmaciacruz.data.remote.dto.paypal.PayPalOrderResponse
import edu.ucne.farmaciacruz.data.remote.dto.paypal.PayPalTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PayPalApiService {

    @FormUrlEncoded
    @POST("v1/oauth2/token")
    suspend fun getAccessToken(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Response<PayPalTokenResponse>

    @POST("v2/checkout/orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body order: PayPalOrderRequest
    ): Response<PayPalOrderResponse>

    @POST("v2/checkout/orders/{orderId}/capture")
    suspend fun captureOrder(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Response<PayPalCaptureResponse>

    @GET("v2/checkout/orders/{orderId}")
    suspend fun getOrderDetails(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Response<PayPalOrderResponse>
}