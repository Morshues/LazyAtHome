package com.morshues.lazyathome.data.network

import android.content.Context
import com.morshues.lazyathome.data.api.AuthApiService
import com.morshues.lazyathome.settings.SettingsManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AuthRetrofitClient {
    private var cachedBaseUrl: String? = null
    private var cachedService: AuthApiService? = null

    fun getService(context: Context): AuthApiService {
        var currentBaseUrl = SettingsManager.getServerPath(context).trim()
        if (!currentBaseUrl.endsWith("/")) {
            currentBaseUrl += '/'
        }

        if (cachedService == null || cachedBaseUrl != currentBaseUrl) {
            cachedBaseUrl = currentBaseUrl
            cachedService = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApiService::class.java)
        }

        return cachedService!!
    }

    fun reset() {
        cachedBaseUrl = null
        cachedService = null
    }
}
