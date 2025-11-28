package com.morshues.lazyathome.data.network

import android.content.Context
import com.google.gson.GsonBuilder
import com.morshues.lazyathome.data.api.ApiService
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.data.model.LibraryItemDeserializer
import com.morshues.lazyathome.di.AppModule
import com.morshues.lazyathome.settings.SettingsManager
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

    fun getService(context: Context): ApiService {
        var currentBaseUrl = SettingsManager.getServerPath(context).trim()
        if (!currentBaseUrl.endsWith("/")) {
            currentBaseUrl += '/'
        }

        if (cachedService == null || cachedBaseUrl != currentBaseUrl) {
            cachedBaseUrl = currentBaseUrl

            cachedService = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(AppModule.authenticatedOkHttpClient)
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