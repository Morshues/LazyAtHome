package com.morshues.lazyathome.di

import android.content.Context
import com.morshues.lazyathome.data.network.AuthRetrofitClient
import com.morshues.lazyathome.data.network.TokenAuthenticator
import com.morshues.lazyathome.data.network.TokenInterceptor
import com.morshues.lazyathome.data.repository.AuthRepository
import okhttp3.OkHttpClient

object AppModule {
    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    private val authApiService by lazy {
        AuthRetrofitClient.getService(applicationContext)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authApiService)
    }

    val authenticatedOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(applicationContext, authRepository))
            .authenticator(TokenAuthenticator(applicationContext, authRepository))
            .build()
    }

    val videoStreamingOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(applicationContext, authRepository))
            .authenticator(TokenAuthenticator(applicationContext, authRepository))
            // Longer timeouts for video streaming
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

}