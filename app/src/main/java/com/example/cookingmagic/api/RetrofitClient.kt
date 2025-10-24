package com.example.cookingmagic.api

import android.R.attr.level
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.getValue

object RetrofitClient {
    private const val BASE_URL = "https://api.api-ninjas.com/v1/"
    const val API_KEY = "dV8oKWhlfZoVmzRrfv2HSw==jnlzIoSes6E0Oqgc"

    // Add logging and proper timeouts
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: RecipeApiService by lazy {
        retrofit.create(RecipeApiService::class.java)
    }
}