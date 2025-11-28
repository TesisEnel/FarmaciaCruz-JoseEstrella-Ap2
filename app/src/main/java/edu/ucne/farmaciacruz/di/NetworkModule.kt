package edu.ucne.farmaciacruz.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.ucne.farmaciacruz.BuildConfig
import edu.ucne.farmaciacruz.data.local.PreferencesManager
import edu.ucne.farmaciacruz.data.local.dao.CarritoDao
import edu.ucne.farmaciacruz.data.remote.ApiService
import edu.ucne.farmaciacruz.data.remote.AuthInterceptor
import edu.ucne.farmaciacruz.data.remote.PayPalApiService
import edu.ucne.farmaciacruz.data.repository.CarritoRepositoryImpl
import edu.ucne.farmaciacruz.data.repository.OrderRepositoryImpl
import edu.ucne.farmaciacruz.data.repository.PreferencesRepositoryImpl
import edu.ucne.farmaciacruz.domain.repository.CarritoRepository
import edu.ucne.farmaciacruz.domain.repository.OrderRepository
import edu.ucne.farmaciacruz.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    @Named("app")
    fun provideAppOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("paypal")
    fun providePayPalOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("app")
    fun provideAppRetrofit(
        @Named("app") okHttpClient: OkHttpClient,
        preferencesManager: PreferencesManager,
        gson: Gson
    ): Retrofit {
        val baseUrl = runBlocking {
            preferencesManager.getApiUrl().first()
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("paypal")
    fun providePayPalRetrofit(
        @Named("paypal") okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        val baseUrl = if (BuildConfig.PAYPAL_ENVIRONMENT == "sandbox") {
            "https://api-m.sandbox.paypal.com/"
        } else {
            "https://api-m.paypal.com/"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@Named("app") retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePayPalApiService(@Named("paypal") retrofit: Retrofit): PayPalApiService {
        return retrofit.create(PayPalApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCarritoRepository(
        carritoDao: CarritoDao
    ): CarritoRepository {
        return CarritoRepositoryImpl(carritoDao)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        prefs: PreferencesManager
    ): PreferencesRepository = PreferencesRepositoryImpl(prefs)

    @Provides
    @Singleton
    fun provideOrderRepository(
        apiService: ApiService
    ): OrderRepository {
        return OrderRepositoryImpl(apiService)
    }
}