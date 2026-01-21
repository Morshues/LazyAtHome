package com.morshues.lazyathome.data.network

import com.morshues.lazyathome.settings.SettingsManager
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class DynamicUrlInterceptor(private val settingsManager: SettingsManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val currentBaseUrl = settingsManager.getServerPath().let {
            if (it.endsWith("/")) it else "$it/"
        }

        val newUrl = originalRequest.url.newBuilder()
            .scheme(currentBaseUrl.toHttpUrl().scheme)
            .host(currentBaseUrl.toHttpUrl().host)
            .port(currentBaseUrl.toHttpUrl().port)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
