package com.morshues.lazyathome.data.network

import android.content.Context
import com.google.gson.GsonBuilder
import com.morshues.lazyathome.data.api.ApiService
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.data.model.LibraryItemDeserializer
import com.morshues.lazyathome.settings.SettingsManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var cachedBaseUrl: String? = null
    val baseUrl
        get() = cachedBaseUrl
    private var cachedService: ApiService? = null

    private val gson = GsonBuilder()
        .registerTypeAdapter(LibraryItem::class.java, LibraryItemDeserializer())
        .create()

    private class AuthInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val token = SettingsManager.getAccessToken(context)

            val request = if (token != null) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                originalRequest
            }

            return chain.proceed(request)
        }
    }

    fun getService(context: Context): ApiService {
        var currentBaseUrl = SettingsManager.getServerPath(context).trim()
        if (!currentBaseUrl.endsWith("/")) {
            currentBaseUrl += '/'
        }

        if (cachedService == null || cachedBaseUrl != currentBaseUrl) {
            cachedBaseUrl = currentBaseUrl

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context.applicationContext))
                .build()

            cachedService = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
        }

        return cachedService!!
    }

    fun reset() {
        cachedBaseUrl = null
        cachedService = null
    }
}